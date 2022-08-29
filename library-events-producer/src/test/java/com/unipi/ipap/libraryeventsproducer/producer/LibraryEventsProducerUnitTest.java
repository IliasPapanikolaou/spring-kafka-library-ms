package com.unipi.ipap.libraryeventsproducer.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unipi.ipap.libraryeventsproducer.domain.Book;
import com.unipi.ipap.libraryeventsproducer.domain.LibraryEvent;
import com.unipi.ipap.libraryeventsproducer.domain.LibraryEventType;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LibraryEventsProducerUnitTest {

    @InjectMocks
    LibraryEventProducer eventProducer;

    @Mock
    KafkaTemplate<Long, String> kafkaTemplate;

    @Spy // Creates instance of object mapper
    ObjectMapper objectMapper;

    @Test
    @SuppressWarnings("unchecked")
    void sendLibraryEventSecondApproachTestOnFailure() {
        // Given
        Book book = Book.builder()
                .bookId(1111L)
                .bookName("Test Book Title")
                .bookAuthor("Test Book Author")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .libraryEventType(LibraryEventType.NEW)
                .build();

        SettableListenableFuture<Exception> future = new SettableListenableFuture<>();
        future.setException(new RuntimeException("Exception Calling Kafka"));
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

        // When
        assertThrows(Exception.class, () -> eventProducer.sendLibraryEventSecondApproach(libraryEvent).get());
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendLibraryEventSeccondApproachTestOnSuccess()
            throws JsonProcessingException, ExecutionException, InterruptedException {
        // Given
        Book book = Book.builder()
                .bookId(1111L)
                .bookName("Test Book Title")
                .bookAuthor("Test Book Author")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .libraryEventType(LibraryEventType.NEW)
                .build();

        SettableListenableFuture<SendResult<Long, String>> future = new SettableListenableFuture<>();

        ProducerRecord<Long, String> producerRecord = new ProducerRecord<>(
                "library-events",
                libraryEvent.getLibraryEventId(),
                objectMapper.writeValueAsString(libraryEvent)
        );

        RecordMetadata recordMetadata = new RecordMetadata(
                new TopicPartition("library-events", 1),
                1, 1, Instant.now().getEpochSecond(), 1, 2
        );

        SendResult<Long, String> sendResult = new SendResult<>(producerRecord, recordMetadata);
        future.set(sendResult);

        // When
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);


        ListenableFuture<SendResult<Long, String>> listenableFuture = eventProducer
                .sendLibraryEventSecondApproach(libraryEvent);

        // Then
        SendResult<Long, String> sendResult1 = listenableFuture.get();
        assertEquals(1, sendResult1.getRecordMetadata().partition());
    }
}
