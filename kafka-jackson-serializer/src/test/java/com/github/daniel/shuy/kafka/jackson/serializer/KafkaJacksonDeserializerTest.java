package com.github.daniel.shuy.kafka.jackson.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.charithe.kafka.KafkaHelper;
import com.github.charithe.kafka.KafkaJunitExtension;
import com.github.charithe.kafka.KafkaJunitExtensionConfig;
import com.github.charithe.kafka.StartupMode;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.StreamSupport;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.Deserializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KafkaJunitExtension.class)
@KafkaJunitExtensionConfig(startupMode = StartupMode.WAIT_FOR_STARTUP)
class KafkaJacksonDeserializerTest {
    private static final String TOPIC = "topic";
    private static final long TIMEOUT = 10000;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private <T> void deserialize(KafkaHelper kafkaHelper, T input, Class<T> clazz) {
        Deserializer<T> deserializer = new KafkaJacksonDeserializer<>(MAPPER, clazz);

        try (
            KafkaProducer<byte[], byte[]> producer = kafkaHelper.createByteProducer();
            KafkaConsumer<T, T> consumer = kafkaHelper.createConsumer(
                    deserializer,
                    deserializer,
                    null);
        ) {
            byte[] data;
            try {
                data = MAPPER.writeValueAsBytes(input);
            } catch (JsonProcessingException e) {
                Assertions.fail(e);
                return;
            }

            Future<RecordMetadata> future = producer.send(new ProducerRecord<>(TOPIC, data, data));
            try {
                future.get(TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                Assertions.fail(e);
            }

            consumer.subscribe(Collections.singleton(TOPIC));
            ConsumerRecords<T, T> records = consumer.poll(TIMEOUT);
            if (records.isEmpty()) {
                Assertions.fail("Kafka topic is empty");
            }

            StreamSupport.stream(records.spliterator(), false)
                    .forEach(record -> {
                        T key = record.key();
                        T value = record.value();

                        Assertions.assertEquals(key, input);
                        Assertions.assertEquals(value, input);
                    });
        }
    }

    @Test
    void deserialize(KafkaHelper kafkaHelper) {
        deserialize(kafkaHelper, new MyValue("name", 18), MyValue.class);
    }

    @Test
    void deserializeNull(KafkaHelper kafkaHelper) {
        deserialize(kafkaHelper, null, MyValue.class);
    }
}
