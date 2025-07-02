package org.medium.gatling.model;

/**
 * Represents a message in the queue with a content, and processing status.
 * This record is used to encapsulate the data structure for messages being processed
 * in the Kafka queue simulation.
 *
 * @param message     The content of the message.
 * @param isProcessed Indicates whether the message has been processed.
 */
public record QueueMessage(String message, boolean isProcessed) {
}
