# kafka-jackson-serializer
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
