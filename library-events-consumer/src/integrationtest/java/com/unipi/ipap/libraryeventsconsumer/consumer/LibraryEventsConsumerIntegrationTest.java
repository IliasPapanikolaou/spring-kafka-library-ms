package com.unipi.ipap.libraryeventsconsumer.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unipi.ipap.libraryeventsconsumer.entity.Book;
import com.unipi.ipap.libraryeventsconsumer.entity.LibraryEvent;
import com.unipi.ipap.libraryeventsconsumer.entity.LibraryEventType;
import com.unipi.ipap.libraryeventsconsumer.repository.LibraryEventsRepository;
import com.unipi.ipap.libraryeventsconsumer.service.LibraryEventsService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

// Spins up the application context
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
// Override default property for embedded kafka address
@TestPropertySource(
        properties = {
                "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"
        })
public class LibraryEventsConsumerIntegrationTest {

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    KafkaListenerEndpointRegistry endpointRegistry;

    @Autowired
    KafkaTemplate<Long, String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @SpyBean // Gives access to the real bean
    LibraryEventsConsumer libraryEventsConsumerSpy;

    @SpyBean // Gives access to the real bean
    LibraryEventsService libraryEventsServiceSpy;

    @Autowired
    LibraryEventsRepository libraryEventsRepository;

    @BeforeEach
    void setUp() {
        endpointRegistry.getAllListenerContainers().forEach(
                messageListenerContainer -> ContainerTestUtils
                        .waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic())
        );
    }

    @AfterEach
    void tearDown() {
        libraryEventsRepository.deleteAll();
    }

    @Test
    void publishNewLibraryEvent() throws JsonProcessingException, ExecutionException, InterruptedException {
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

        String json = objectMapper.writeValueAsString(libraryEvent);

        // Make the call synchronous with get()
        kafkaTemplate.sendDefault(json).get();

        // When timer expires (3 sec), it will continue
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        // Then
        verify(libraryEventsConsumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(libraryEventsServiceSpy, times(1)).processLibraryEvent(isA(ConsumerRecord.class));

        List<LibraryEvent> libraryEventList = libraryEventsRepository.findAll();

        assertEquals(1, libraryEventList.size());

        libraryEventList.forEach(libEvent -> {
            assertNotNull(libEvent.getLibraryEventId());
            assertEquals(1111L, libEvent.getBook().getBookId());
        });
    }

    @Test
    void publishUpdateLibraryEvent() throws JsonProcessingException, ExecutionException, InterruptedException {
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

        // Save Library Event to database
        LibraryEvent resultLibraryEvent = libraryEventsRepository.save(libraryEvent);

        // Update procedure - Set Library Event Type to update
        resultLibraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
        book.setBookName("Updated Title");
        book.setBookAuthor("Updated Author");
        resultLibraryEvent.setBook(book);

        // Create json string
        String updatedJson = objectMapper.writeValueAsString(resultLibraryEvent);

        // Make the call synchronous with get()
        kafkaTemplate.sendDefault(updatedJson).get();

        // When timer expires (3 sec), it will continue
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        // Then
        // Verify number of method invocations
        verify(libraryEventsConsumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(libraryEventsServiceSpy, times(1)).processLibraryEvent(isA(ConsumerRecord.class));

        List<LibraryEvent> libraryEventList = libraryEventsRepository.findAll();

        assertEquals(1, libraryEventList.size());

        libraryEventList.forEach(libEvent -> assertAll(
                () -> assertNotNull(libEvent.getLibraryEventId()),
                () -> assertEquals(1111L, libEvent.getBook().getBookId()),
                () -> assertEquals("Updated Title", libEvent.getBook().getBookName()),
                () -> assertEquals("Updated Author", libEvent.getBook().getBookAuthor())
        ));
    }

    @Test
    void publishUpdateLibraryEventNullLibraryEventIdt()
            throws JsonProcessingException, ExecutionException, InterruptedException {
        // Given
        Book book = Book.builder()
                .bookId(1111L)
                .bookName("Updated Title")
                .bookAuthor("Updated Author")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .libraryEventType(LibraryEventType.UPDATE)
                .build();

        // Create json string
        String json = objectMapper.writeValueAsString(libraryEvent);

        // Make the call synchronous with get()
        kafkaTemplate.sendDefault(json).get();

        // When timer expires (3 sec), it will continue
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(5, TimeUnit.SECONDS);

        // Then
        // Verify number of method invocations - Error handling retries 10 times
        verify(libraryEventsConsumerSpy, times(10)).onMessage(isA(ConsumerRecord.class));
        verify(libraryEventsServiceSpy, times(10)).processLibraryEvent(isA(ConsumerRecord.class));
    }
}
