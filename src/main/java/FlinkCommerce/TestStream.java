package FlinkCommerce;
import Deserializer.JSONValueDeserializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.api.common.serialization.SimpleStringSchema;


public class TestStream {

	public static void main(String[] args) throws Exception {

		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.enableCheckpointing(5000);

		KafkaSource<String> source = KafkaSource.<String>builder()
  		  .setBootstrapServers("kundanatest1.infra.alephys.com:9091")
    		  .setTopics("transaction")
   	       	  .setGroupId("my-test-group")
                  .setStartingOffsets(OffsetsInitializer.earliest())
                  .setValueOnlyDeserializer(new SimpleStringSchema())
                  .setProperty("security.protocol", "SSL")
                  .setProperty("ssl.truststore.location", "/var/ssl/private/kafka_broker.truststore.jks")
                  .setProperty("ssl.truststore.password", "confluenttruststorepass")
                  .setProperty("ssl.keystore.location", "/var/ssl/private/kafka_broker.keystore.jks")
				  .setProperty("ssl.keystore.password", "confluentkeystorestorepass")
				  .setProperty("ssl.key.password", "confluentkeystorestorepass")
				  .setProperty("ssl.endpoint.identification.algorithm", "")
		  .build();


                DataStream<String> inputstream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");

		inputstream.print();

		env.execute("Test-topic");
	}
}
