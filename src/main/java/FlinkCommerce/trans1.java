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

public class trans1 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
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

        DataStream <String> sourcestream= env.fromSource(source,WatermarkStrategy.noWatermarks(),"kafka source");

        DataStream<Transaction> map1= sourcestream.map(new Splitter());
            
        DataStream<Transaction> map2=map1.filter(value-> value.totalAmount>1000);
        
        DataStream<Transaction> map3=map2.keyBy(value-> value.name).window(TumblingProcessingTimeWindows.of(Duration.ofSeconds(10))).sum("productQuantity");

        KafkaSink<String> sink =KafkaSink.<String>builder()
                    .setBootstrapServers("10.1.0.248:9091")
                    .setProperty("security.protocol", "SSL")
                    .setProperty("ssl.truststore.location", "/var/ssl/private/kafka_broker.truststore.jks")
                    .setProperty("ssl.truststore.password", "confluenttruststorepass")
                    .setProperty("ssl.endpoint.identification.algorithm", "")
                    .setProperty("ssl.keystore.location", "/var/ssl/private/kafka_broker.keystore.jks")
                    .setProperty("ssl.keystore.password", "confluentkeystorestorepass")
                    .setProperty("ssl.key.password", "confluentkeystorestorepass")
                    .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("test-topic-flink")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build())
                    .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                    .build();

       map3.map(value -> value.toString()).sinkTo(sink);

       env.execute("filter and keyBy transformation");


    }
}

