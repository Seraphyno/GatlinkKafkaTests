package org.medium.gatling.model;

import java.util.UUID;

/**
 * Represents a message in the queue with an ID, content, and processing status.
 * This record is used to encapsulate the data structure for messages being processed
 * in the Kafka queue simulation.
 *
 * @param id          Unique identifier for the message.
 * @param message     The content of the message.
 * @param isProcessed Indicates whether the message has been processed.
 */
public record QueueMessage(UUID id, String message, boolean isProcessed) {
}
