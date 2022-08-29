package com.unipi.ipap.libraryeventsproducer.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unipi.ipap.libraryeventsproducer.domain.LibraryEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class LibraryEventProducer {

    private static final String KAFKA_TOPIC = "library-events";

    KafkaTemplate<Long, String> kafkaTemplate;
    ObjectMapper objectMapper; // Json representation of an object

    public LibraryEventProducer(KafkaTemplate<Long, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // Default approach 1
    public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {

        Long key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        ListenableFuture<SendResult<Long, String>> listenableFuture = kafkaTemplate.sendDefault(key, value);

        listenableFuture.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Long, String> result) {
                handleSuccess(key, value, result);
            }
        });
    }

    // Approach 2 - Explicit topic name and Producer Record
    public ListenableFuture<SendResult<Long, String>> sendLibraryEventSecondApproach(LibraryEvent libraryEvent)
            throws JsonProcessingException {

        Long key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        // ListenableFuture<SendResult<Long, String>> listenableFuture = kafkaTemplate.send(KAFKA_TOPIC, key, value);

        // Use of ProducerRecord
        ProducerRecord<Long, String> longStringProducerRecord = buildProducerRecord(
                KAFKA_TOPIC, null, key, value, null);
        ListenableFuture<SendResult<Long, String>> listenableFuture = kafkaTemplate.send(longStringProducerRecord);

        listenableFuture.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Long, String> result) {
                handleSuccess(key, value, result);
            }
        });

        return listenableFuture;
    }

    private ProducerRecord<Long, String> buildProducerRecord(
            String topic, Integer partition, Long key, String value, Iterable<Header> headers) {

        // Create kafka headers (optional)
        List<Header> recordHeaders = List.of(new RecordHeader("event-source", "scanner".getBytes()));

        return new ProducerRecord<>(topic, partition, null, key, value, recordHeaders);
    }

    /**
     * Only for demonstration on how to send synchronous messages.
     * Use the default method 'sendLibraryEvent' instead.
     * Unless we want to wait for the response of the kafka server to proceed.
     */
    public SendResult<Long, String> sendLibraryEventSynchronously(LibraryEvent libraryEvent)
            throws JsonProcessingException {
        Long key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        SendResult<Long, String> sendResult;
        try {
            // Use get() to make a blocking call
            sendResult = kafkaTemplate.sendDefault(key, value).get();
            // Blocking call with timeout
            // sendResult = kafkaTemplate.sendDefault(key, value).get(1, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException ex) {
            log.error("ExecutionException/InterruptedException error Sending the Message. Exception: {}",
                    ex.getMessage());
            throw new RuntimeException(ex);
        }
        //  catch (TimeoutException ex) {
        //      log.error("Send request to kafka has timed out: {}", ex.getMessage());
        //      throw new RuntimeException(ex);
        //  }
        return sendResult;
    }

    private void handleFailure(Long key, String value, Throwable ex) {
        log.error("Error Sending the Message. Exception: {}", ex.getMessage());
        try {
            throw ex;
        } catch (Throwable throwable) {
            log.error("Error in OnFailure: {}", throwable.getMessage());
        }
    }

    private void handleSuccess(Long key, String value, SendResult<Long, String> result) {
        log.info("Message Sent Successfully for key: {} and value: {}. Partition: {}",
                key, value, result.getRecordMetadata().partition()
        );
    }
}
