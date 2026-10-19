package com.ichuvilin.bookshelf.api.controller;

import com.ichuvilin.bookshelf.api.model.request.BookListRequest;
import com.ichuvilin.bookshelf.api.model.response.BookListResponse;
import com.ichuvilin.bookshelf.api.model.response.BookResponse;
import com.ichuvilin.bookshelf.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping
    public ResponseEntity<BookListResponse> list(@Valid @ModelAttribute BookListRequest request) {
        return ResponseEntity.ok(bookService.list(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> byId(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getById(id));
    }
}
