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

### Serializer
```java
ObjectMapper mapper = new ObjectMapper();
// mapper.configure(..., ...);

Properties props = new Properties();
// props.put(..., ...);

Producer<String, MyValue> producer = new KafkaProducer<>(props,
    new StringSerializer(),
    new KafkaJacksonSerializer(mapper));

producer.send("FooBar", new MyValue());
```

### Deserializer
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

StreamSupport.stream(records.spliterator(), false)
        .forEach(record -> {
            String key = record.key();
            MyValue value = record.value();

            // ...
        });
```
