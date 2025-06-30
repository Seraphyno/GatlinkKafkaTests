package org.medium.gatling;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.Simulation;
import org.apache.kafka.common.serialization.Serde;
import org.galaxio.gatling.kafka.javaapi.request.expressions.ExpressionBuilder;
import org.galaxio.gatling.kafka.request.KafkaProtocolMessage;
import org.medium.gatling.model.QueueMessage;
import org.medium.gatling.model.QueueMessageSerde;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.core.OpenInjectionStep.atOnceUsers;
import static org.galaxio.gatling.kafka.javaapi.KafkaDsl.kafka;
import static org.galaxio.gatling.kafka.javaapi.KafkaDsl.simpleCheck;
import static org.medium.gatling.configuration.GatlingConfiguration.CONSUMER_TOPIC;
import static org.medium.gatling.configuration.GatlingConfiguration.KAFKA_PROTOCOL_BUILDER;
import static org.medium.gatling.configuration.GatlingConfiguration.PRODUCER_TOPIC;

/**
 * Gatling simulation for Kafka interactions.
 * This simulation sends messages to a Kafka topic and checks if they are processed correctly by consuming from the
 * consumer topic.
 */
public class GatlingKafkaSimulation extends Simulation {

    private static final Serde<QueueMessage> QUEUE_MESSAGE_SERDE = new QueueMessageSerde();

    {
        setUp(
            scenario("Kafka interaction scenario")
                .exec(feed(queueMessageIdFeeder())
                    .exec(
                        kafka("Send message")
                                .requestReply()
                                .requestTopic(PRODUCER_TOPIC)
                                .replyTopic(CONSUMER_TOPIC)
                                .send("#{id}",
                                        generateQueueMessage()
                                )
                                .check(simpleCheck(GatlingKafkaSimulation::verifyMessageWasProcessed))
                                .toChainBuilder()
                    )
                )
                .injectOpen(atOnceUsers(10))
        ).protocols(KAFKA_PROTOCOL_BUILDER);
    }

    // We want to generate unique IDs for each message sent to Kafka
    private static Iterator<Map<String, Object>> queueMessageIdFeeder() {
        return Stream.generate(() -> Map.of("id", (Object) UUID.randomUUID().toString())).iterator();
    }

    /**
     * Generates a QueueMessage object based on the session variable "id", each message will have a unique ID
     */
    private ExpressionBuilder<QueueMessage> generateQueueMessage() {
        return new ExpressionBuilder<>(
                session -> {
                    String id = Objects.requireNonNull(session.get("id"));
                    return new QueueMessage(
                            UUID.fromString(id),
                            "Hello, Kafka!",
                            false
                    );
                },
                QueueMessage.class,
                QUEUE_MESSAGE_SERDE
        ) {};
    }

    /**
     * Verifies that the message has been processed by transforming the consumer topic message to our QueueMessage,
     * checking if the ID matches and isProcessed is true
     */
    private static boolean verifyMessageWasProcessed(KafkaProtocolMessage kafkaMessage) {
        // Deserialize the message value to QueueMessage
        QueueMessage message = QUEUE_MESSAGE_SERDE.deserializer().deserialize(
                CONSUMER_TOPIC,
                kafkaMessage.value()
        );

        // Extract the expected ID from the key
        String expectedId = new String(kafkaMessage.key());
        // Check if IDs match and isCompleted is true
        return message != null
                && message.id().toString().equals(expectedId)
                && message.isProcessed();
    }
}
