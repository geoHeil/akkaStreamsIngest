# bitStream comparison
Real-time stream of different bitCoin courses, published to kafka and then compared in streaming engine 
for arbitrage opportunities

use `sbt console`to interactively run queries

or `sbt run`. For the stream-processing part make sure to set `$SBT_OPTS -Xmx8G -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -Xss2M`
as spark/flink/beam will be launched inside sbt 


**using it***

start kafka
```
docker-compose up -d
sbt run # choose 2
sbt console
```
check connectivity
```
kafkacat -C -b localhost:9092 -t topic1 #starts listener
kafkacat -P -b localhost:9092 -t topic1 #starts producer
```
you can send some simple messages over kafka and see the output

**connect a real stream**
Lets connect a real stream
```
sbt run
```