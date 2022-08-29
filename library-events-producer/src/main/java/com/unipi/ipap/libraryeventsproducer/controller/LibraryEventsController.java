package com.unipi.ipap.libraryeventsproducer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.unipi.ipap.libraryeventsproducer.domain.LibraryEvent;
import com.unipi.ipap.libraryeventsproducer.domain.LibraryEventType;
import com.unipi.ipap.libraryeventsproducer.producer.LibraryEventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/v1/libraryEvent")
public class LibraryEventsController {

    LibraryEventProducer libraryEventProducer;

    public LibraryEventsController(LibraryEventProducer libraryEventProducer) {
        this.libraryEventProducer = libraryEventProducer;
    }

    // Default POST
    @PostMapping
    public ResponseEntity<LibraryEvent> postLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent)
            throws JsonProcessingException {
        libraryEvent.setLibraryEventType(LibraryEventType.NEW);
        // Invoke kafka producer
        libraryEventProducer.sendLibraryEvent(libraryEvent);
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    // Second Approach POST
    @PostMapping("/secondApproach")
    public ResponseEntity<LibraryEvent> postLibraryEventSecondApproach(@RequestBody @Valid LibraryEvent libraryEvent)
            throws JsonProcessingException {
        libraryEvent.setLibraryEventType(LibraryEventType.NEW);
        // Invoke kafka producer
        libraryEventProducer.sendLibraryEventSecondApproach(libraryEvent);
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    // Synchronous Blocking approach POST
    @PostMapping("/synchronous")
    public ResponseEntity<LibraryEvent> postLibraryEventSynchronously(@RequestBody @Valid LibraryEvent libraryEvent)
            throws JsonProcessingException {
        libraryEvent.setLibraryEventType(LibraryEventType.NEW);
        // Invoke kafka producer
        SendResult<Long, String> sendResult = libraryEventProducer.sendLibraryEventSynchronously(libraryEvent);
        log.info("SendResult is {}", sendResult.toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    // Default PUT
    @PutMapping
    public ResponseEntity<?> putLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent)
            throws JsonProcessingException {

        if (Objects.isNull(libraryEvent.getLibraryEventId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Please pass the LibraryEventId");
        }
        libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
        // Invoke kafka producer
        libraryEventProducer.sendLibraryEvent(libraryEvent);
        return ResponseEntity.status(HttpStatus.OK).body(libraryEvent);
    }

}
