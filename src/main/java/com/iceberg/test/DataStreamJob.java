package com.iceberg.test;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.TableResult;

public class DataStreamJob {
    public static void main(String[] args) {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(5000);
        EnvironmentSettings settings =
                EnvironmentSettings.newInstance().inStreamingMode().build();

        TableEnvironment tableEnv = TableEnvironment.create(settings);

        // 1. Create Iceberg Catalog (USE SQL - MOST STABLE)
        tableEnv.executeSql(
            "CREATE CATALOG iceberg_cat WITH (" +
            "  'type'='iceberg'," +
            "  'catalog-type'='hadoop'," +
            "  'warehouse'='hdfs://ns1/user/kundana/iceberg'," +
            "  'hadoop.conf.dir'='/etc/hadoop/conf'" +
            ")"
        );

        // 2. Use catalog
        tableEnv.executeSql("USE CATALOG iceberg_cat");

        // 3. Create DB + Table
        tableEnv.executeSql("CREATE DATABASE IF NOT EXISTS test_db");

        tableEnv.executeSql(
            "CREATE TABLE IF NOT EXISTS test_db.user_events (" +
            " id BIGINT," +
            " name STRING" +
            ")"
        );

        // 4. DIRECT INSERT (NO SOURCE STREAM NEEDED)
        TableResult result = tableEnv.executeSql(
            "INSERT INTO test_db.user_events VALUES " +
            "(1, 'A'), (2, 'B'), (3, 'C'),(4,'D'),(5,'E'),(6,'F'),(7,'G'),(8,'H'),(9,'I'),(10,'J'),(11,'K'),(12,'L'),(13,'M'),(14,'N'),(15,'O'),(16,'P'),(17,'Q'),(18,'R'),(19,'S'),(20,'T'),(21,'U'),(22,'V'),(23,'W'),(24,'X'),(25,'Y'),(26,'Z'),(27,'1'),(28,'2'),(29,'3'),(30,'4'),(31,'5'),(32,'6'),(33,'7'),(34,'8'),(35,'9'),(36,'0')"
        );
        TableResult result1 = tableEnv.executeSql("SELECT * FROM test_db.user_events");
        result1.print();

        result.getJobClient().ifPresent(job -> {
            System.out.println("Job submitted: " + job.getJobID());
        });
    }
}