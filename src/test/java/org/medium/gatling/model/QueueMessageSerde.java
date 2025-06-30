package org.medium.gatling.model;

import com.google.gson.Gson;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

/**
 * Serde implementation for QueueMessage.
 * This class provides serialization and deserialization methods
 * for QueueMessage objects using Gson.
 */
public class QueueMessageSerde implements Serde<QueueMessage> {

    private final Gson gson = new Gson();

    @Override
    public Serializer<QueueMessage> serializer() {
        return (topic, data) -> gson.toJson(data).getBytes();
    }

    @Override
    public Deserializer<QueueMessage> deserializer() {
        return (topic, data) -> gson.fromJson(new String(data), QueueMessage.class);
    }
}
