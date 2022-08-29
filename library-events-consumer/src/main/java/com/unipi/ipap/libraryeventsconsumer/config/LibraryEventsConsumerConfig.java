package com.unipi.ipap.libraryeventsconsumer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
public class LibraryEventsConsumerConfig {

    /**
     * This is Optional method:
     * Use this only in case we want to change the default offset commit mode to manual (manual acks).
     * <p>
     * Taken from KafkaAnnotationDrivenConfiguration.class
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
}
