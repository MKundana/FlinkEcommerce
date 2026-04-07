# Flink



kinit ldapusername

To build the package:

mvn clean package

flink run -m yarn-cluster   -Dclassloader.resolve-order=parent-last   -c FlinkCommerce.TestStream   target/FlinkCommerce-1.0-SNAPSHOT.jar



To kill the application:

yarn application -kill application_1775521765514_0002



