package FlinkCommerce;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;


public class trans1 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        KafkaSource<String> source = KafkaSource.<String>builder()
                    .setBootstrapServers("kundanatest2.infra.alephys.com:9091")
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

        KafkaSink<String> sink =KafkaSink.<String>builder()
                    .setBootstrapServers("kundanatest2.infra.alephys.com:9091")
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


        sourcestream.sinkTo(sink);

        env.execute("test-sink");


    }
}