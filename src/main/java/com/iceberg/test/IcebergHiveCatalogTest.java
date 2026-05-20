package com.iceberg.test;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.TableResult;

public class IcebergHiveCatalogTest {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        EnvironmentSettings settings = EnvironmentSettings.newInstance().inBatchMode().build();
        TableEnvironment tableEnv = TableEnvironment.create(settings);

        System.out.println("Creating Iceberg Hive Catalog...");
        tableEnv.executeSql(
            "CREATE CATALOG iceberg_hive WITH (" +
            "  'type'='iceberg'," +
            "  'catalog-type'='hive'," +
            "  'hive-conf-dir'='/etc/hive/conf'," +
            "  'clients'='5'," +
            "  'property-version'='1'" +
            ")"
        );

        tableEnv.executeSql("USE CATALOG iceberg_hive");

        System.out.println("Creating database...");
        tableEnv.executeSql("CREATE DATABASE IF NOT EXISTS my_new_flink_db");
        tableEnv.executeSql("USE my_new_flink_db");

        System.out.println("Creating table...");
        tableEnv.executeSql(
            "CREATE TABLE IF NOT EXISTS my_kundana_table (" +
            " id INT," +
            " name STRING," +
            " salary DOUBLE" +
            ") WITH (" +
            "  'location'='hdfs://ns1/user/kundana/iceberg/my_kundana_table'" +
            ")"
        );
        System.out.println("Table created.");

        System.out.println("Inserting rows...");
        try {
            tableEnv.executeSql(
                "INSERT INTO my_kundana_table VALUES (1, 'John Doe', 50000.0), (2, 'Jane Smith', 75000.0)"
            ).await();
            System.out.println("Rows inserted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
