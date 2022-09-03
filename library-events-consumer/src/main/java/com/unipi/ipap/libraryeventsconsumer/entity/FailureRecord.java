package com.unipi.ipap.libraryeventsconsumer.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class FailureRecord {
    @Id
    @GeneratedValue
    private Long id;
    private String topic;
    private Long recordKey;
    private String errorRecord;
    private Integer partition;
    private Long offsetValue;
    private String exception;
    private String status;
}
