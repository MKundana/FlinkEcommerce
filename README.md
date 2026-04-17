# Flink

Kerberos Keytab & Flink YARN Configuration Guide
1. Generating a Valid Keytab (The Admin Way)

If you have admin access, always generate the keytab on the KDC/LDAP node to ensure the Salt and KVNO are perfect.

• Command: `kadmin.local -q "xst -k kundana.keytab kundana@ALEPHYS.COM"`

• Move to Worker: `scp kundana.keytab root@<worker-node>:/path/`

• Verify: `klist -kt kundana.keytab`

---

2. Flink Launch Command (Production Ready)

Use these parameters to ensure the job runs long-term without credential expiry.

```

flink run -d -m yarn-cluster \

  -yD security.kerberos.login.principal=kundana@ALEPHYS.COM \

  -yD security.kerberos.login.keytab=kundana.keytab \

  -yD security.kerberos.token.provider.hadoopfs.renewer=yarn \

  -yt kundana.keytab \

  YourFlinkJob.jar

```

---

3. Key Parameters Explained

• `security.kerberos.login.keytab`: The "Passport" file for authentication.

• `security.kerberos.token.provider.hadoopfs.renewer=yarn`: The "Safety Net." Tells YARN to renew HDFS delegation tokens so the job doesn't crash after 24-72 hours.

• `-yt <file>`: Ships the keytab file to all YARN containers/workers.

---

4. Permanent Configuration (`flink-conf.yaml`)

Add these to `/etc/flink/conf/flink-conf.yaml` to avoid typing them every time:

• `security.kerberos.login.use-ticket-cache: false`

• `security.kerberos.login.principal: kundana@ALEPHYS.COM`

• `security.kerberos.login.keytab: /absolute/path/to/kundana.keytab`

• `security.kerberos.token.provider.hadoopfs.renewer: yarn`

---

5. Troubleshooting Lockouts

• Error: "Client's credentials have been revoked" -> Account is Locked.

• Unlock Command (on KDC): `kadmin.local -q "modprinc -unlock kundana@ALEPHYS.COM"`

• Check KVNO: `kadmin.local -q "getprinc kundana@ALEPHYS.COM"`

To verify if your Kerberos principal has the correct permissions to write to HDFS, you should perform a manual "Simulated Test" on the cluster command line.

1. Clear any old tickets
kdestroy

2. Authenticate using your keytab
kinit -kt kundana.keytab kundana@ALEPHYS.COM

3. Try to create a dummy file in your target HDFS directory
hadoop fs -touchz /user/kundana/transaction_data/test_connection.txt


********************************************************************************

# To run the Program

-  To build the package:

cd sai/FlinkCommerce

`mvn clean package`

- To run the program
```
flink run -m yarn-cluster   -c FlinkCommerce.test   -Dsecurity.kerberos.login.principal=kundana@ALEPHYS.COM   -Dsecurity.kerberos.login.keytab=/tmp/kundana.keytab   -Djobmanager.memory.process.size=2048m   -Dtaskmanager.memory.process.size=2048m   -Dtaskmanager.numberOfTaskSlots=1   target/FlinkCommerce-1.0-SNAPSHOT.jar
```

-  To list the jobs

`flink List`

------------------ Running/Restarting Jobs -------------------
17.04.2026 02:58:02 : 34a01a7c1ecdab8fbf4a1e397b20daf2 : Kafka-to-HDFS (RUNNING)

-  To stop the Job with savepoint

``` flink stop --savepointPath hdfs:///user/kundana/savepoints 34a01a7c1ecdab8fbf4a1e397b20daf2 ```

Suspending job "34a01a7c1ecdab8fbf4a1e397b20daf2" with a CANONICAL savepoint.
Triggering stop-with-savepoint for job 34a01a7c1ecdab8fbf4a1e397b20daf2.
Waiting for response...
Savepoint completed. Path: hdfs://ns1/user/kundana/savepoints/savepoint-34a01a-364b1f084c77


-  To run from Savepoint

```
flink run -m yarn-cluster   -s hdfs:///user/kundana/savepoints/savepoint-34a01a-364b1f084c77   -c FlinkCommerce.test   -Dsecurity.kerberos.login.principal=kundana@ALEPHYS.COM   -Dsecurity.kerberos.login.keytab=/tmp/kundana.keytab   -Djobmanager.memory.process.size=2048m   -Dtaskmanager.memory.process.size=2048m   -Dtaskmanager.numberOfTaskSlots=1   target/FlinkCommerce-1.0-SNAPSHOT.jar
```


- To list the files whether data is being written or not

``` hdfs dfs -ls -R /user/kundana/transaction_data/2026-04-17--02 ```

- To see the data in the files

``` hdfs dfs -cat /user/kundana/transaction_data/2026-04-17--02/part-b36df530-5600-4ac6-aa17-d178ee7c4c40-0 | head -n 20 ```

****************************************************************************************


# To kill the application:

``` yarn application -kill application_1775521765514_0002 ```


-----------------------------------------------------------------------------------------------------------------------------------------

# To consume the messages
kundanatest1 node

cat /etc/kafka/client.properties

security.protocol=SSL
ssl.key.password=confluentkeystorestorepass
ssl.keystore.location=/var/ssl/private/kafka_broker.keystore.jks
ssl.keystore.password=confluentkeystorestorepass
ssl.truststore.location=/var/ssl/private/kafka_broker.truststore.jks
ssl.truststore.password=confluenttruststorepass

cd /usr/bin/

``` ./kafka-console-consumer --bootstrap-server kundanatest1.infra.alephys.com:9091 --consumer.config /etc/kafka/client.properties --topic transaction ```

-------------------------------------------------------------------------------------------------------------------------------------------


# To produce the messages 
kundanatest4 node

``` python3.11 kafka_producer_sasl.py ```




