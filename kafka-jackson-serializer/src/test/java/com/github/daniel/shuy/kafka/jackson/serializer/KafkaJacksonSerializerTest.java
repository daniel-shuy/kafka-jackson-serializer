package com.github.daniel.shuy.kafka.jackson.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.charithe.kafka.KafkaHelper;
import com.github.charithe.kafka.KafkaJunitExtension;
import com.github.charithe.kafka.KafkaJunitExtensionConfig;
import com.github.charithe.kafka.StartupMode;
import java.io.IOException;
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
import org.apache.kafka.common.serialization.Serializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KafkaJunitExtension.class)
@KafkaJunitExtensionConfig(startupMode = StartupMode.WAIT_FOR_STARTUP)
class KafkaJacksonSerializerTest {
    private static final String TOPIC = "topic";
    private static final long TIMEOUT = 10000;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private <T> void serialize(KafkaHelper kafkaHelper, T input, Class<T> clazz) {
        Serializer<T> serializer = new KafkaJacksonSerializer<>(MAPPER);

        try (
            KafkaProducer<T, T> producer = kafkaHelper.createProducer(
                    serializer,
                    serializer,
                    null);
            KafkaConsumer<byte[], byte[]> consumer = kafkaHelper.createByteConsumer();
        ) {
            Future<RecordMetadata> future = producer.send(new ProducerRecord<>(TOPIC, input, input));
            try {
                future.get(TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                Assertions.fail(e);
            }

            consumer.subscribe(Collections.singleton(TOPIC));
            ConsumerRecords<byte[], byte[]> records = consumer.poll(TIMEOUT);
            if (records.isEmpty()) {
                Assertions.fail("Kafka topic is empty");
            }

            StreamSupport.stream(records.spliterator(), false)
                    .forEach(record -> {
                        byte[] outputKeyData = record.key();
                        T outputKey;
                        if (outputKeyData == null) {
                            outputKey = null;
                        }
                        else {
                            try {
                                outputKey = MAPPER.readValue(outputKeyData, clazz);
                            } catch (IOException e) {
                                Assertions.fail(e);
                                return;
                            }
                        }
                        Assertions.assertEquals(outputKey, input);

                        byte[] outputValueData = record.value();
                        T outputValue;
                        if (outputValueData == null) {
                            outputValue = null;
                        }
                        else {
                            try {
                                outputValue = MAPPER.readValue(outputValueData, clazz);
                            } catch (IOException e) {
                                Assertions.fail(e);
                                return;
                            }
                        }
                        Assertions.assertEquals(outputValue, input);
                    });
        }
    }

    @Test
    void serialize(KafkaHelper kafkaHelper) {
        serialize(kafkaHelper, new MyValue("name", 18), MyValue.class);
    }

    @Test
    void serializeNull(KafkaHelper kafkaHelper) {
        serialize(kafkaHelper, null, MyValue.class);
    }
}
