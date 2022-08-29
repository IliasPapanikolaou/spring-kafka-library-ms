package com.unipi.ipap.libraryeventsconsumer.entity;

import lombok.*;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class LibraryEvent {

    @Id
    @GeneratedValue
    private Long libraryEventId;

    @OneToOne(mappedBy = "libraryEvent", cascade = CascadeType.ALL)
    @ToString.Exclude // In order to not having circular dependency (stack overflow)
    private Book book;

    @Enumerated(EnumType.STRING)
    private LibraryEventType libraryEventType;

}
