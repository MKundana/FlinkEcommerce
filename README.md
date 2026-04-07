# Flink



kinit ldapusername

To build the package:

cd sai/FlinkCommerce
mvn clean package

flink run -m yarn-cluster   -Dclassloader.resolve-order=parent-last   -c FlinkCommerce.TestStream   target/FlinkCommerce-1.0-SNAPSHOT.jar



To kill the application:

yarn application -kill application_1775521765514_0002
(137)

(219)
cat /etc/kafka/client.properties

security.protocol=SSL
ssl.key.password=confluentkeystorestorepass
ssl.keystore.location=/var/ssl/private/kafka_broker.keystore.jks
ssl.keystore.password=confluentkeystorestorepass
ssl.truststore.location=/var/ssl/private/kafka_broker.truststore.jks
ssl.truststore.password=confluenttruststorepass

To consume the messages
kafka-console-consumer --bootstrap-server kundanatest1.infra.alephys.com:9091 --consumer.config /etc/kafka/client.properties --topic transaction



To produce the messages  (193)

python3.11 kafka_producer_sasl.py




