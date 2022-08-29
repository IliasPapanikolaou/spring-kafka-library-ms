package com.unipi.ipap.libraryeventsconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unipi.ipap.libraryeventsconsumer.entity.LibraryEvent;
import com.unipi.ipap.libraryeventsconsumer.repository.LibraryEventsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class LibraryEventsService {

    private final LibraryEventsRepository eventsRepository;
    private final ObjectMapper objectMapper;

    public LibraryEventsService(LibraryEventsRepository eventsRepository, ObjectMapper objectMapper) {
        this.eventsRepository = eventsRepository;
        this.objectMapper = objectMapper;
    }

    public void processLibraryEvent(ConsumerRecord<Long, String> consumerRecord) throws JsonProcessingException {
        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        log.info("LibraryEvent: {}", libraryEvent);

        switch (libraryEvent.getLibraryEventType()) {
            case NEW -> {
                save(libraryEvent);
                // break; // break not necessary in lambda style switch case
            }
            case UPDATE -> {
                // validate the library event
                validate(libraryEvent);
                // save
                save(libraryEvent);
                // break; // break not necessary in lambda style switch case
            }
            default -> {
                log.info("Invalid Library Event Type");
            }
        }
        eventsRepository.save(libraryEvent);
    }

    private void validate(LibraryEvent libraryEvent) {

        if (Objects.isNull(libraryEvent.getLibraryEventId())) {
            throw new IllegalArgumentException("Library Event Id is missing");
        }

        Optional<LibraryEvent> optionalLibraryEvent = eventsRepository.findById(libraryEvent.getLibraryEventId());
        optionalLibraryEvent.orElseThrow(() -> new IllegalArgumentException("Not a valid library event"));
        log.info("Validation is successful for the library Event: {}", optionalLibraryEvent.get());
    }

    private void save(LibraryEvent libraryEvent) {
        // In order to populate the mapping column of book entity
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        // Save to database
        eventsRepository.save(libraryEvent);
        log.info("Successfully Persisted the library event {}", libraryEvent);
    }
}
