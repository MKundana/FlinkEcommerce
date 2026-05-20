package com.iceberg.test;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment; // Use the bridge wrapper
import org.apache.flink.table.catalog.hive.HiveCatalog;

public class HiveCatalogTest { // Tip: Capitalize class names in Java (PascalCase)
    public static void main(String[] args) {

    StreamExecutionEnvironment env =
            StreamExecutionEnvironment.getExecutionEnvironment();

    StreamTableEnvironment tableEnv =
            StreamTableEnvironment.create(env);

    HiveCatalog catalog = new HiveCatalog(
            "hive",
            "default",
            "/etc/hive/conf",
            "3.1.3000"
    );

    tableEnv.registerCatalog("hive", catalog);

    tableEnv.useCatalog("hive");

    tableEnv.executeSql(
        "CREATE DATABASE IF NOT EXISTS my_new_flink_db"
    );
    System.out.println("Database created successfully.");

    // Switch to the new database
    tableEnv.useDatabase("my_new_flink_db");

    // Create a table
    System.out.println("Creating table...");
    tableEnv.executeSql(
        "CREATE TABLE IF NOT EXISTS my_table (" +
        "  id INT," +
        "  name STRING" +
        ") WITH (" +
        "  'connector' = 'hive'" +
        ")"
    );
    System.out.println("Table created successfully.");

    // Insert some rows
    System.out.println("Inserting rows... This will launch a Flink job on YARN.");
    try {
        tableEnv.executeSql(
            "INSERT INTO my_table VALUES (1, 'Alice'), (2, 'Bob'), (3, 'Charlie')"
        ).await();
        System.out.println("Rows inserted successfully.");
    } catch (Exception e) {
        e.printStackTrace();
    }
}
}