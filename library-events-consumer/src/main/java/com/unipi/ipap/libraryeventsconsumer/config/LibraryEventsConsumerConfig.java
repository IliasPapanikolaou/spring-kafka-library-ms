package com.unipi.ipap.libraryeventsconsumer.config;

import com.unipi.ipap.libraryeventsconsumer.service.FailureService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import java.util.List;

/**
 * This is an Optional class:
 * Use in case we want to handle and Recover the erroneous messages and put them in a new kafka topic
 */
@Slf4j
@Configuration
@EnableKafka // This is necessary in order Kafka Listener to work
public class LibraryEventsConsumerConfig {

    private static final String RETRY = "RETRY";
    private static final String DEAD = "DEAD";

    // The below field are needed for the DeadLetterPublishingRecoverer method.
    private final KafkaTemplate<?, ?> kafkaTemplate;
    private FailureService failureService;
    @Value("${topics.retry}")
    private String retryTopic;
    @Value("${topics.dlt}")
    private String deadLetterTopic;


    // Constructor
    public LibraryEventsConsumerConfig(KafkaTemplate<?, ?> kafkaTemplate, FailureService failureService) {
        this.kafkaTemplate = kafkaTemplate;
        this.failureService = failureService;
    }

    public DeadLetterPublishingRecoverer publishingRecoverer() {

        return new DeadLetterPublishingRecoverer(kafkaTemplate, (r, e) -> {
            log.error("Exception in publishingRecoverer: {}", e.getMessage(), e);
            if (e.getCause() instanceof RecoverableDataAccessException) {
                return new TopicPartition(retryTopic, r.partition());
            } else {
                return new TopicPartition(deadLetterTopic, r.partition());
            }
        });
    }

    ConsumerRecordRecoverer consumerRecordRecoverer = (consumerRecord, e) -> {
        log.error("Exception in consumerRecordRecoverer : {}", e.getMessage());
        var consumerRcrd = (ConsumerRecord<Long, String>) consumerRecord;
        if (e.getCause() instanceof RecoverableDataAccessException) {
            // recovery logic
            log.info("Inside Recovery");
            failureService.saveFailedRecord(consumerRcrd, e, RETRY);
        } else {
            // non-recovery logic
            log.info("Inside Recovery");
            failureService.saveFailedRecord(consumerRcrd, e, DEAD);
        }
    };

    /**
     * This method is Optional method:
     * Use this only in case we want to change the default offset commit mode to manual (manual acks).
     * <p>
     * This method is taken from KafkaAnnotationDrivenConfiguration.class and modified (override)
     */
//    @Bean
//    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
//            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
//            ObjectProvider<ConsumerFactory<Object, Object>> kafkaConsumerFactory) {
//
//        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        configurer.configure(factory, kafkaConsumerFactory.getIfAvailable());
//        // Set Ack mode to Manual
//        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
//
//        return factory;
//    }

    /**
     * This is Optional method in order to Scale the Consumer.
     * Use this only in case we want to have multiple (concurrent) message listener containers (multithreading).
     * Not necessary for cloud environments like Kubernetes, since we have multiple pods.
     * <p>
     * This method is taken from KafkaAnnotationDrivenConfiguration.class and modified (override)
     */
//    @Bean
//    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
//            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
//            ObjectProvider<ConsumerFactory<Object, Object>> kafkaConsumerFactory) {
//
//        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        configurer.configure(factory, kafkaConsumerFactory.getIfAvailable());
//        // Set Concurrent message listener containers
//        factory.setConcurrency(3);
//
//        return factory;
//    }

    /**
     * The following two methods are Optional in order to use Custom Error handler and Custom Retry
     * <p>
     * This method is taken from KafkaAnnotationDrivenConfiguration.class and modified (override)
     */
    public DefaultErrorHandler errorHandler() {

        // Retry twice with time interval of 1 second
        // FixedBackOff fixedBackOff = new FixedBackOff(1000L, 2);

        // Exponential Backoff option - the retry interval increases exponentially with each retry
        ExponentialBackOffWithMaxRetries expBackOff = new ExponentialBackOffWithMaxRetries(2);
        expBackOff.setInitialInterval(1_000L);
        expBackOff.setMultiplier(2.0); // 2x times of the first retry time and so on
        expBackOff.setMaxInterval(2_000L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                // publishingRecoverer(),
                consumerRecordRecoverer,
                // fixedBackOff
                expBackOff
        );

        // Optional Custom Retry Policy to Ignore exceptions that won't recover with retries
        List<Class<IllegalArgumentException>> exceptionsToIgnoreList = List.of(IllegalArgumentException.class);
        exceptionsToIgnoreList.forEach(errorHandler::addNotRetryableExceptions);

        // Optional Custom Retry Policy to explicitly add exceptions that are likely to recover with retries
        List<Class<RecoverableDataAccessException>> exceptionsToRetryList = List.of(RecoverableDataAccessException.class);
        exceptionsToRetryList.forEach(errorHandler::addRetryableExceptions);

        // Optional Retry Listener to monitor each retry attempt
        errorHandler.setRetryListeners((consumerRecord, ex, deliverAttempt) -> {
            log.info("Failed Record in Retry Listener, Exception: {}, delivery attempt: {}",
                    ex.getMessage(), deliverAttempt);
        });

        return errorHandler;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ObjectProvider<ConsumerFactory<Object, Object>> kafkaConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory.getIfAvailable());

        // Set Custom Error Handler
        factory.setCommonErrorHandler(errorHandler());

        return factory;
    }
}
