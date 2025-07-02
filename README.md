## GatlingKafkaTests

This project contains Gatling tests for Kafka. It is designed to test the performance and reliability of a Kafka 
application that listens on a topic, processes the message, then writes the result on a different topic.

### Technical requirements:

- Java 17 or later
- Maven 3.6 or later

### Configuration

Kafka topics and other properties are externalized in `application.properties` for flexibility and environment-specific overrides.

### Running

Tests can be run using `mvn clean test`. The setup in QueueProcessor should be completed first.
