package com.github.daniel.shuy.kafka.jackson.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * Deserializer for Kafka that uses Jackson to unmarshall Objects from JSON
 *
 * @param <T> Type to be deserialized into.
 */
public class KafkaJacksonDeserializer<T> implements Deserializer<T> {
    private final ObjectMapper objectMapper;
    private final Class<T> clazz;

    public KafkaJacksonDeserializer(ObjectMapper objectMapper, Class<T> clazz) {
        this.objectMapper = objectMapper;
        this.clazz = clazz;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            return objectMapper.readValue(data, clazz);
        } catch (IOException | RuntimeException e) {
            throw new SerializationException("Error deserializing from JSON with Jackson", e);
        }
    }

    @Override
    public void close() {
    }
}
