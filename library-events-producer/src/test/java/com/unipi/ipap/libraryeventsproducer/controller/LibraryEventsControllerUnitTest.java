package com.unipi.ipap.libraryeventsproducer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unipi.ipap.libraryeventsproducer.domain.Book;
import com.unipi.ipap.libraryeventsproducer.domain.LibraryEvent;
import com.unipi.ipap.libraryeventsproducer.domain.LibraryEventType;
import com.unipi.ipap.libraryeventsproducer.producer.LibraryEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryEventsController.class)
@AutoConfigureMockMvc
public class LibraryEventsControllerUnitTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    LibraryEventProducer libraryEventProducer;

    @Test
    void postLibraryEvent() throws Exception {
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

        String json = objectMapper.writeValueAsString(libraryEvent);

        // Mock LibraryEventProducer behavior
        doNothing().when(libraryEventProducer).sendLibraryEvent(isA(LibraryEvent.class));

        // When
        // Then
        mockMvc.perform(post("/v1/libraryEvent")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated());
    }

    @Test
    void postLibraryEventNullBookShouldFail() throws Exception {
        // Given
        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(null)
                .build();

        String json = objectMapper.writeValueAsString(libraryEvent);

        // Mock LibraryEventProducer behavior
        // For methods that have void return type, we use doNothing();
        // doNothing().when(libraryEventProducer).sendLibraryEventSecondApproach(isA(LibraryEvent.class));
        when(libraryEventProducer.sendLibraryEventSecondApproach(isA(LibraryEvent.class))).thenReturn(null);

        // When
        String expectedErrorMessage = "book - must not be null";
        mockMvc.perform(post("/v1/libraryEvent")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
        )
        // Expect
        .andExpect(status().is4xxClientError())
        .andExpect(content().string(expectedErrorMessage))
        .andExpect(result -> assertEquals(MethodArgumentNotValidException.class,
                Objects.requireNonNull(result.getResolvedException()).getClass()));
    }

    @Test
    void postLibraryEventInvalidBookShouldFail() throws Exception {
        // Given
        Book book = Book.builder()
                .bookId(1111L)
                .bookName("")
                .bookAuthor("")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        String json = objectMapper.writeValueAsString(libraryEvent);

        // Mock LibraryEventProducer behavior
        // For methods that have void return type, we use doNothing();
        // doNothing().when(libraryEventProducer).sendLibraryEventSecondApproach(isA(LibraryEvent.class));
        when(libraryEventProducer.sendLibraryEventSecondApproach(isA(LibraryEvent.class))).thenReturn(null);

        // When
        String expectedErrorMessage = "book.bookAuthor - must not be blank, book.bookName - must not be blank";
        mockMvc.perform(post("/v1/libraryEvent")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
        )
        // Expect
        .andExpect(status().is4xxClientError())
        .andExpect(content().string(expectedErrorMessage))
        .andExpect(result -> assertEquals(MethodArgumentNotValidException.class,
                Objects.requireNonNull(result.getResolvedException()).getClass()));
    }

    @Test
    void putLibraryEvent() throws Exception {
        // Given
        Book book = Book.builder()
                .bookId(1111L)
                .bookName("Test Book Title")
                .bookAuthor("Test Book Author")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(100L)
                .book(book)
                .libraryEventType(LibraryEventType.NEW)
                .build();

        String json = objectMapper.writeValueAsString(libraryEvent);

        // Mock LibraryEventProducer behavior
        doNothing().when(libraryEventProducer).sendLibraryEvent(isA(LibraryEvent.class));

        // When
        // Then
        mockMvc.perform(put("/v1/libraryEvent")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void putLibraryEventNullValueInLibraryEventId() throws Exception {
        // Given
        Book book = Book.builder()
                .bookId(1111L)
                .bookName("Test Book Title")
                .bookAuthor("Test Book Author")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .libraryEventType(LibraryEventType.NEW)
                .build();

        String json = objectMapper.writeValueAsString(libraryEvent);

        // Mock LibraryEventProducer behavior
        doNothing().when(libraryEventProducer).sendLibraryEvent(isA(LibraryEvent.class));

        // When
        // Then
        String expectedErrorMessage = "Please pass the LibraryEventId";
        mockMvc.perform(put("/v1/libraryEvent")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(expectedErrorMessage));
    }
}
