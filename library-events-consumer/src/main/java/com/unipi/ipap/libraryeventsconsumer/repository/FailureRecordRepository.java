package com.unipi.ipap.libraryeventsconsumer.repository;

import com.unipi.ipap.libraryeventsconsumer.entity.FailureRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FailureRecordRepository extends JpaRepository<FailureRecord, Long> {
}
