package FlinkCommerce;

import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import java.time.Duration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import java.sql.Timestamp;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;

public class trans2 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);
        env.enableCheckpointing(10000);
        env.getCheckpointConfig().setCheckpointStorage("hdfs:///user/flink/checkpoints");
        
        KafkaSource<String> source = KafkaSource.<String>builder()
                    .setBootstrapServers("10.1.0.248:9091")
                    .setProperty("security.protocol", "SSL")
                    .setProperty("ssl.truststore.location", "/var/ssl/private/kafka_broker.truststore.jks")
                    .setProperty("ssl.truststore.password", "confluenttruststorepass")
                    .setProperty("ssl.endpoint.identification.algorithm", "")
                    .setProperty("ssl.keystore.location", "/var/ssl/private/kafka_broker.keystore.jks")
                    .setProperty("ssl.keystore.password", "confluentkeystorestorepass")
                    .setProperty("ssl.key.password", "confluentkeystorestorepass")
                    .setTopics("transaction")
                    .setGroupId("flink-group")
                    .setStartingOffsets(OffsetsInitializer.earliest())
                    .setValueOnlyDeserializer(new SimpleStringSchema())
                    .build();

        DataStream <String> sourcestream= env.fromSource(source,WatermarkStrategy.noWatermarks(),"kafka source")
            .filter(value -> value != null && !value.trim().isEmpty());

        DataStream<Transaction> map1= sourcestream.map(new Splitter());
            
        //DataStream<Transaction> map2=map1.filter(value-> value.totalAmount>1000);
        
        OutputTag<Transaction> rejectTag=new OutputTag<Transaction>("Rejected-transactions"){};

        SingleOutputStreamOperator<Transaction> mainstream=map1.process(new ProcessFunction<Transaction, Transaction>(){

            @Override
            public void processElement(Transaction value, Context ctx, Collector<Transaction> out) {
              try{
                if (value.totalAmount <0){
                    ctx.output(rejectTag,value);
                }
                else{
                 out.collect(value); 
                }
              } 
              catch(Exception e){
                ctx.output(rejectTag,value);
              }  
            }
        });

        DataStream<Transaction> rejectedstream = mainstream.getSideOutput(rejectTag);

        rejectedstream.addSink(new EmailSink());
        rejectedstream.print();
        

        KafkaSink<Transaction> sink=KafkaSink.<Transaction>builder()
                    .setBootstrapServers("10.1.0.248:9091")
                    .setProperty("security.protocol", "SSL")
                    .setProperty("ssl.truststore.location", "/var/ssl/private/kafka_broker.truststore.jks")
                    .setProperty("ssl.truststore.password", "confluenttruststorepass")
                    .setProperty("ssl.endpoint.identification.algorithm", "")
                    .setProperty("ssl.keystore.location", "/var/ssl/private/kafka_broker.keystore.jks")
                    .setProperty("ssl.keystore.password", "confluentkeystorestorepass")
                    .setProperty("ssl.key.password", "confluentkeystorestorepass")
                    .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopicSelector((Transaction txn )->
                        {
                            if(txn.paymentMethod.equalsIgnoreCase("credit_card"))
                            {
                                return "creditcard";
                            }
                            else if(txn.paymentMethod.equalsIgnoreCase("debit_card"))
                            {
                                return "debitcard";
                            }
                            else
                            {
                                return "online";
                            }
                        })
                        .setValueSerializationSchema(new TransactionSerializer())
                        .build())
                    .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                    .build();

       mainstream.sinkTo(sink);

       env.execute("sending data to three different topics");


    }
}

