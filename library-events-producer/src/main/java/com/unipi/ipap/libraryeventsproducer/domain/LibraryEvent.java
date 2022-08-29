package com.unipi.ipap.libraryeventsproducer.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LibraryEvent {

    private Long libraryEventId;
    @NotNull
    @Valid // We add valid here in order to work Book class validations
    private Book book;
    private LibraryEventType libraryEventType;

}
