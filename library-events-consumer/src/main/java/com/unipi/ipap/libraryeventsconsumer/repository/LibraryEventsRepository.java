package com.unipi.ipap.libraryeventsconsumer.repository;

import com.unipi.ipap.libraryeventsconsumer.entity.LibraryEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryEventsRepository extends JpaRepository<LibraryEvent, Long> {
}
