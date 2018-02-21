package com.github.daniel.shuy.kafka.jackson.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

/**
 * Serializer for Kafka that uses Jackson to marshall Objects to JSON
 * 
 * @param <T> Type to be serialized from.
 */
public class KafkaJacksonSerializer<T> implements Serializer<T> {
    private final ObjectMapper mapper;

    /**
     * Returns a new instance of {@link KafkaJacksonSerializer}.
     * 
     * @param mapper 
     *                  The Jackson {@link ObjectMapper}.
     */
    public KafkaJacksonSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, T data) {
        try {
            return mapper.writeValueAsBytes(data);
        } catch (JsonProcessingException | RuntimeException e) {
            throw new SerializationException("Error serializing to JSON with Jackson", e);
        }
    }

    @Override
    public void close() {
    }
}
