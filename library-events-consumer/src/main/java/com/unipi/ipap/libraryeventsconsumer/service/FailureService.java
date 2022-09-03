package com.unipi.ipap.libraryeventsconsumer.service;

import com.unipi.ipap.libraryeventsconsumer.entity.FailureRecord;
import com.unipi.ipap.libraryeventsconsumer.repository.FailureRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FailureService {

    private final FailureRecordRepository failureRecordRepository;

    public FailureService(FailureRecordRepository failureRecordRepository) {
        this.failureRecordRepository = failureRecordRepository;
    }


    public void saveFailedRecord(ConsumerRecord<Long, String> consumerRecord, Exception e, String status) {

        FailureRecord failureRecord = new FailureRecord(
                null, consumerRecord.topic(), consumerRecord.key(), consumerRecord.value(),
                consumerRecord.partition(), consumerRecord.offset(), e.getCause().getMessage(), status
        );

        failureRecordRepository.save(failureRecord);
    }
}
