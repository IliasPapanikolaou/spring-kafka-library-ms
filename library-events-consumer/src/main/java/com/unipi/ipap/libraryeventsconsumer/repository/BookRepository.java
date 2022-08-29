package com.unipi.ipap.libraryeventsconsumer.repository;

import com.unipi.ipap.libraryeventsconsumer.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
