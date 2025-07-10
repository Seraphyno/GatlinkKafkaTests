package org.medium.gatling;

import io.gatling.javaapi.core.Simulation;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.galaxio.gatling.kafka.javaapi.request.expressions.ExpressionBuilder;
import org.galaxio.gatling.kafka.request.KafkaProtocolMessage;
import org.medium.gatling.model.QueueMessage;
import org.medium.gatling.model.QueueMessageSerde;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.holdFor;
import static io.gatling.javaapi.core.CoreDsl.jumpToRps;
import static io.gatling.javaapi.core.CoreDsl.reachRps;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static java.time.Duration.ofSeconds;
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
                scenario("Send and receive messages to/from Kafka")
                        .exec(feed(queueMessageIdFeeder())
                                .exec(
                                        kafka("SendAndReceiveMessages")
                                                .requestReply()
                                                .requestTopic(PRODUCER_TOPIC)
                                                .replyTopic(CONSUMER_TOPIC)
                                                .send(generateUniqueId(), generateQueueMessage())
                                                .check(simpleCheck(GatlingKafkaSimulation::verifyMessageWasProcessed))
                                                .toChainBuilder()
                                )
                        )
                        .injectClosed(constantConcurrentUsers(10).during(30))
        )
                .protocols(KAFKA_PROTOCOL_BUILDER)
                .throttle(
                        jumpToRps(1), holdFor(ofSeconds(5)),
                        reachRps(50).during(ofSeconds(15)),
                        holdFor(30)
                );
    }

    private ExpressionBuilder<String> generateUniqueId() {
        return new ExpressionBuilder<>(
                session -> "request_" + session.get("id"),
                String.class,
                Serdes.String()
        ) {
        };
    }

    // We want to generate unique IDs for each message sent to Kafka
    private static Iterator<Map<String, Object>> queueMessageIdFeeder() {
        return Stream.generate(() -> Map.of("id", (Object) UUID.randomUUID().toString())).iterator();
    }

    private ExpressionBuilder<QueueMessage> generateQueueMessage() {
        return new ExpressionBuilder<>(
                session -> new QueueMessage(
                        "Hello, Kafka!",
                        false
                ),
                QueueMessage.class,
                QUEUE_MESSAGE_SERDE
        ) {
        };
    }

    /**
     * Verifies that the message has been processed by transforming the consumer topic message to our QueueMessage,
     * checking if it was processed.
     */
    private static boolean verifyMessageWasProcessed(KafkaProtocolMessage kafkaMessage) {
        // Deserialize the message value to QueueMessage
        QueueMessage message = QUEUE_MESSAGE_SERDE.deserializer().deserialize(
                CONSUMER_TOPIC,
                kafkaMessage.value()
        );

        return message != null && message.isProcessed();
    }
}
