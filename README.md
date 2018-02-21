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
    <version>0.1.1</version>
</dependency>
```

```java
ObjectMapper mapper = new ObjectMapper();
// mapper.configure(..., ...);

Properties props = new Properties();
// props.put(..., ...);

Producer<String, MyValue> producer = new KafkaProducer<>(props,
    new StringSerializer(),
    new KafkaJacksonSerializer(mapper));
```
