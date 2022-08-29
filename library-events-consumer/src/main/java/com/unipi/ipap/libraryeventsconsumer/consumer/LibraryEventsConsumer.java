package com.unipi.ipap.libraryeventsconsumer.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.unipi.ipap.libraryeventsconsumer.service.LibraryEventsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LibraryEventsConsumer {

    private static final String KAFKA_TOPIC = "library-events";

    private final LibraryEventsService eventsService;

    public LibraryEventsConsumer(LibraryEventsService eventsService) {
        this.eventsService = eventsService;
    }

    @KafkaListener(topics = {KAFKA_TOPIC})
    public void onMessage(ConsumerRecord<Long, String> consumerRecord) throws JsonProcessingException {

        log.info("ConsumerRecord: {}", consumerRecord);
        eventsService.processLibraryEvent(consumerRecord);
    }
}
