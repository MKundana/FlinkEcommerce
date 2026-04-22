package FlinkCommerce;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.flink.CatalogLoader;
import org.apache.iceberg.flink.TableLoader;
import org.apache.iceberg.flink.sink.FlinkSink;

import java.util.HashMap;
import java.util.Map;

public class Operations {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(5000);

        // --- Kerberos Authentication for Cloudera ---
        System.out.println("Initializing Hadoop Configuration...");
        org.apache.hadoop.conf.Configuration hadoopConf = new org.apache.hadoop.conf.Configuration();
        hadoopConf.addResource(new org.apache.hadoop.fs.Path("/etc/hadoop/conf/core-site.xml"));
        hadoopConf.addResource(new org.apache.hadoop.fs.Path("/etc/hadoop/conf/hdfs-site.xml"));
        hadoopConf.addResource(new org.apache.hadoop.fs.Path("/etc/hive/conf/hive-site.xml"));
        hadoopConf.set("hive.metastore.sasl.enabled", "true");
        hadoopConf.set("hive.metastore.kerberos.principal", "hive/clouderaprodnode5.infra.alephys.com@ALEPHYS.COM");
        hadoopConf.set("hadoop.security.authentication", "kerberos");
        System.out.println("Hadoop Configuration initialized.");

        String principal = "kundana@ALEPHYS.COM";
        String keytabPath = "/root/saiFlink/FlinkCommerce/kundana.keytab";

        // org.apache.hadoop.security.UserGroupInformation.setConfiguration(hadoopConf);
        // org.apache.hadoop.security.UserGroupInformation.loginUserFromKeytab(principal, keytabPath);


        // --- Kafka Source ---
        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("10.1.38.155:9091")
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

        DataStream<RowData> rowStream = inputstream.map(new JsonToRowMapper());

        // --- Iceberg Sink Configuration ---
        Map<String, String> props = new HashMap<>();
        props.put("type", "iceberg");
        props.put("catalog-type", "hive");
        props.put("uri", "thrift://clouderaprodnode5.infra.alephys.com:9083");
        props.put("warehouse", "hdfs:///user/hive/warehouse");
        props.put("clients", "5");

        System.out.println("Loading Iceberg Catalog...");
        CatalogLoader catalogLoader = CatalogLoader.hive("hive_catalog", hadoopConf, props);
        System.out.println("Catalog Loader created. Loading Table...");
        TableLoader tableLoader = TableLoader.fromCatalog(catalogLoader, TableIdentifier.of("commerce_db", "transaction"));
        System.out.println("Table Loader created.");

        // Write to Iceberg
        FlinkSink.forRowData(rowStream)
                .tableLoader(tableLoader)
                .overwrite(false)
                .append();

        System.out.println("Starting Flink Job Execution...");
        env.execute("Flink-Commerce-Iceberg-Sink");
    }

    public static class JsonToRowMapper implements MapFunction<String, RowData> {
        private transient ObjectMapper objectMapper;

        @Override
        public RowData map(String value) throws Exception {
            if (objectMapper == null) {
                objectMapper = new ObjectMapper();
            }

            try {
                JsonNode json = objectMapper.readTree(value);
                GenericRowData row = new GenericRowData(12);

                row.setField(0, StringData.fromString(json.path("transactionId").asText("")));
                row.setField(1, StringData.fromString(json.path("productId").asText("")));
                row.setField(2, StringData.fromString(json.path("name").asText("")));
                row.setField(3, StringData.fromString(json.path("productCategory").asText("")));
                row.setField(4, json.path("productPrice").asDouble(0.0));
                row.setField(5, json.path("productQuantity").asInt(0));
                row.setField(6, StringData.fromString(json.path("productBrand").asText("")));
                row.setField(7, StringData.fromString(json.path("currency").asText("")));
                row.setField(8, StringData.fromString(json.path("customerId").asText("")));
                row.setField(9, StringData.fromString(json.path("transactionDate").asText("")));
                row.setField(10, StringData.fromString(json.path("paymentMethod").asText("")));
                row.setField(11, json.path("totalAmount").asDouble(0.0));

                return row;
            } catch (Exception e) {
                // Log and return null or handle dead-letter queue
                System.err.println("Failed to parse JSON: " + value);
                return new GenericRowData(12); 
            }
        }
    }
}