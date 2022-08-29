package com.unipi.ipap.libraryeventsconsumer.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.unipi.ipap.libraryeventsconsumer.service.LibraryEventsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LibraryEventsRetryConsumer {

    private final LibraryEventsService eventsService;

    public LibraryEventsRetryConsumer(LibraryEventsService eventsService) {
        this.eventsService = eventsService;
    }

    @KafkaListener(
            topics = {"${topics.retry}"},
            autoStartup = "${retryListener.startup:true",
            groupId = "retry-listener-group"
    )
    public void onMessage(ConsumerRecord<Long, String> consumerRecord) throws JsonProcessingException {

        log.info("ConsumerRecord in Retry Consumer: {}", consumerRecord);
        consumerRecord.headers().forEach(header -> {
            // Convert byte[] to string by passing byte[] to a new String()
            log.info("Key: {}, Value: {}", header.key(), new String(header.value()));
        });
        eventsService.processLibraryEvent(consumerRecord);
    }
}
