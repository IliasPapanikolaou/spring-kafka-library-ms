package com.unipi.ipap.libraryeventsproducer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unipi.ipap.libraryeventsproducer.domain.Book;
import com.unipi.ipap.libraryeventsproducer.domain.LibraryEvent;
import com.unipi.ipap.libraryeventsproducer.domain.LibraryEventType;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

// Spins up the application context
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
// Override default property for embedded kafka address
@TestPropertySource(
        properties = {
                "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"
        })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class LibraryEventsControllerIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    // @Resource can be used to suppress the error - intellij error
    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    ObjectMapper objectMapper;

    private Consumer<Long, String> consumer;

    // Create Consumer before each test
    @BeforeEach
    void setUp() {
        Map<String, Object> configs = new HashMap<>(
                KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));

        consumer = new DefaultKafkaConsumerFactory<>(configs, new LongDeserializer(), new StringDeserializer())
                .createConsumer();

        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "library-events");
    }

    // Close Consumer after each test
    @AfterEach
    void tearDown() throws Exception {
        consumer.close();
    }

    // Warning, this test is going to produce event in the real kafka if we don't use embedded kafka
    @Test
    @Timeout(5) // Timeout the test to prevent continuous consumer polling
    void postLibraryEvent() throws JsonProcessingException {
        // Given
        Book book = Book.builder()
                .bookId(1111L)
                .bookName("Test Book Title")
                .bookAuthor("Test Book Author")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvent, headers);

        // When
        ResponseEntity<LibraryEvent> responseEntity = restTemplate.exchange(
                "/v1/libraryEvent", HttpMethod.POST, request, LibraryEvent.class);

        ConsumerRecord<Long, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, "library-events");

        // Not recommended approach to delay the test if consumer still polling, instead use @Timeout(seconds) annotation
        // Thread.sleep(3000);

        // Get Json String
        libraryEvent.setLibraryEventType(LibraryEventType.NEW);
        String expectedRecord = objectMapper.writeValueAsString(libraryEvent);
        String value = consumerRecord.value();

        // Then
        assertAll(
                // Test HttpStatus
                () -> assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode()),
                // Test the object returned
                () -> assertEquals(book.getBookId(),
                        Objects.requireNonNull(responseEntity.getBody()).getBook().getBookId()),
                // Test Consumer
                () -> assertEquals(expectedRecord, value)
        );
    }

    @Test
    @Timeout(5) // Timeout the test to prevent continuous consumer polling
    void putLibraryEvent() throws JsonProcessingException {
        // Given
        Book book = Book.builder()
                .bookId(1111L)
                .bookName("Test Book Title")
                .bookAuthor("Test Book Author")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(100L)
                .book(book)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvent, headers);

        // When
        ResponseEntity<LibraryEvent> responseEntity = restTemplate.exchange(
                "/v1/libraryEvent", HttpMethod.PUT, request, LibraryEvent.class);

        ConsumerRecord<Long, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, "library-events");

        // Not recommended approach to delay the test if consumer still polling, instead use @Timeout(seconds) annotation
        // Thread.sleep(3000);

        // Get Json String
        libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
        String expectedRecord = objectMapper.writeValueAsString(libraryEvent);
        String value = consumerRecord.value();

        // Then
        assertAll(
                // Test HttpStatus
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                // Test the object returned
                () -> assertEquals(book.getBookId(),
                        Objects.requireNonNull(responseEntity.getBody()).getBook().getBookId()),
                // Test Consumer
                () -> assertEquals(expectedRecord, value)
        );
    }
}
