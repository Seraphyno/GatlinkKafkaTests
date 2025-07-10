package org.medium.gatling.configuration;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.galaxio.gatling.kafka.javaapi.protocol.KafkaProtocolBuilderNew;

import java.util.Map;

import static org.galaxio.gatling.kafka.javaapi.KafkaDsl.kafka;

/**
 * Configuration class for Gatling simulations.
 * This class loads properties from the application.properties file and sets up the Kafka protocol builder.
 */
public final class GatlingConfiguration {

    // Configurable properties
    public static final String KAFKA_BROKER = PropertyLoader.getOrDefault("kafka.bootstrap.server", "localhost:9092");
    public static final String PRODUCER_TOPIC = PropertyLoader.getOrDefault("kafka.producer.topic", "");
    public static final String CONSUMER_TOPIC = PropertyLoader.getOrDefault("kafka.consumer.topic", "");

    public static final KafkaProtocolBuilderNew KAFKA_PROTOCOL_BUILDER = kafka()
            .requestReply()
            .producerSettings(
                    Map.of(
                            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKER,
                            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer",
                            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer"
                    )
            )
            .consumeSettings(
                    Map.of(
                            "bootstrap.servers", KAFKA_BROKER,
                            "key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer",
                            "value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer",
                            "group.id", "gatling-consumer-group"
                    )
            )
            .withDefaultTimeout()
            .matchByMessage(message -> {
                String key = new String(message.key());
                String[] parts = key.split("_");
                if (parts.length < 2) return new byte[0];
                return parts[1].trim().getBytes();
            });
}
