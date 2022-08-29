package com.unipi.ipap.libraryeventsconsumer.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
@EnableKafka // This is necessary in order Kafka Listener to work
public class LibraryEventsConsumerConfig {

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
}
