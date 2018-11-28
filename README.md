# kafka-jackson-serializer
**This project is no longer being maintained (however, the released artifacts will still remain in Maven Central). Use [spring-kafka](http://projects.spring.io/spring-kafka/)'s [JsonSerializer](https://docs.spring.io/spring-kafka/api/org/springframework/kafka/support/serializer/JsonSerializer.html)/[JsonDeserializer](https://docs.spring.io/spring-kafka/api/org/springframework/kafka/support/serializer/JsonDeserializer.html)/[JsonSerde](https://docs.spring.io/spring-kafka/api/org/springframework/kafka/support/serializer/JsonSerde.html) instead.**

Serializer/Deserializer for Kafka that uses Jackson to marshall/unmarshall Objects to/from JSON

## Requirements
| Dependency | Version |
| ------- | ------------------ |
| Kafka | 1.X.X |
| Jackson | 2.9.X |
| Java | 7+ |

## Usage
Add the following to your Maven dependency list:
```
<dependency>
    <groupId>com.github.daniel-shuy</groupId>
    <artifactId>kafka-jackson-serializer</artifactId>
    <version>0.1.2</version>
</dependency>
```
Override the `kafka-client` dependency version with the version of Kafka you wish to use:
```
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>1.1.1</version>
</dependency>
```
Override the `jackson-databind` dependency version with the version of Jackson you wish to use:
```
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.9.7</version>
</dependency>
```

### Kafka Producer
```java
ObjectMapper mapper = new ObjectMapper();
// mapper.configure(..., ...);

Properties props = new Properties();
// props.put(..., ...);

Producer<String, MyValue> producer = new KafkaProducer<>(props,
    new StringSerializer(),
    new KafkaJacksonSerializer(mapper));

producer.send(new ProducerRecord<>("topic", new MyValue()));
```

### Kafka Consumer
```java
ObjectMapper mapper = new ObjectMapper();
// mapper.configure(..., ...);

Properties props = new Properties();
// props.put(..., ...);

Consumer<String, MyValue> consumer = new KafkaConsumer<>(props,
    new StringDeserializer(),
    new KafkaJacksonDeserializer(mapper, MyValue.class));

consumer.subscribe("topic");
ConsumerRecords<String, MyValue> records = consumer.poll(100);

records.forEach(record -> {
    String key = record.key();
    MyValue value = record.value();

    // ...
});
```

### Kafka Streams
```java
ObjectMapper mapper = new ObjectMapper();
// mapper.configure(..., ...);

Serde<String> stringSerde = Serdes.String();
Serde<MyValue> myValueSerde = Serdes.serdeFrom(
        new KafkaJacksonSerializer(mapper), 
        new KafkaJacksonDeserializer(mapper, MyValue.class));

Properties config = new Properties();
// config.put(..., ...);

StreamsBuilder builder = new StreamsBuilder();
KStream<String, MyValue> myValues = builder.stream("input_topic", Consumed.with(stringSerde, myValueSerde));
KStream<String, MyValue> filteredMyValues = myValues.filter((key, value) -> {
    // ...
});
filteredMyValues.to("output_topic", Produced.with(stringSerde, myValueSerde));

Topology topology = builder.build();
KafkaStreams streams = new KafkaStreams(topology, config);
streams.setUncaughtExceptionHandler((thread, throwable) -> {
    // ...
});
Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
streams.start();
```
