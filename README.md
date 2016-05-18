# rebalance-cf-apps
sample code using cf-java-client to rebalance apps on CF.

### To compile

```
$ mvn clean package
```

### To run

```
$ java -jar target/rebalanceapps-0.0.1-SNAPSHOT.jar --cf.host=<api address> --cf.username=<cf username> --cf.password=<cf user password>
```
