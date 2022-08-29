package com.unipi.ipap.libraryeventsconsumer.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

// @Component
@Slf4j
public class LibraryEventsConsumerManualOffset implements AcknowledgingMessageListener<Long, String> {

    private static final String KAFKA_TOPIC = "library-events";

    @Override
    @KafkaListener(topics = {KAFKA_TOPIC})
    public void onMessage(ConsumerRecord<Long, String> consumerRecord, Acknowledgment acknowledgment) {
        log.info("ConsumerRecord: {}", consumerRecord);
        assert acknowledgment != null;
        acknowledgment.acknowledge();
    }
}
