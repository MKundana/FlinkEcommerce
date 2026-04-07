package FlinkCommerce;

import Deserializer.JSONValueDeserializationSchema;
import Dto.Transaction;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public class DataStreamJob {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        String topic = "transaction";

        // Extract certificates from JAR to temporary local storage on the worker node
        //String truststorePath = extractResource("kafka_broker.truststore.jks");
        //String keystorePath = extractResource("kafka_broker.keystore.jks");

        //Properties sslProps = new Properties();
        //sslProps.setProperty("security.protocol", "SSL");
        
        // Use the temporary paths created at runtime
        //sslProps.setProperty("ssl.truststore.location", truststorePath);
        //sslProps.setProperty("ssl.truststore.password", "confluenttruststorepass");

        //sslProps.setProperty("ssl.keystore.location", keystorePath);
        //sslProps.setProperty("ssl.keystore.password", "confluentkeystorestorepass");
        //sslProps.setProperty("ssl.key.password", "confluentkeystorestorepass");

        // Disable hostname verification for internal cluster communication
        //sslProps.setProperty("ssl.endpoint.identification.algorithm", "");

        KafkaSource<Transaction> source = KafkaSource.<Transaction>builder()
                .setBootstrapServers("kundanatest4.infra.alephys.com:9092")
                .setTopics(topic)
                .setGroupId("flink-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new JSONValueDeserializationSchema())
          //      .setProperties(sslProps)
                .build();

        DataStream<Transaction> transactionStream = env.fromSource(
                source,
                WatermarkStrategy.noWatermarks(),
                "Kafka source"
        );

        transactionStream.print();

        env.execute("Kafka Job with Embedded SSL");
    }

    /**
     * Extracts a file from the JAR resources to a temporary file on the local disk.
     */
    private static String extractResource(String resourceName) throws IOException {
        Path tempFile = Files.createTempFile("flink-ssl-", "-" + resourceName);
        try (InputStream is = DataStreamJob.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IOException("Resource not found in JAR: " + resourceName);
            }
            Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        // Set permissions so the flink/spark user can definitely read it
        tempFile.toFile().setReadable(true, false);
        return tempFile.toAbsolutePath().toString();
    }
}
