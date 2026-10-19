package com.ichuvilin.bookshelf.api.controller;

import com.ichuvilin.bookshelf.api.model.request.BookListRequest;
import com.ichuvilin.bookshelf.api.model.response.BookListResponse;
import com.ichuvilin.bookshelf.api.model.response.BookResponse;
import com.ichuvilin.bookshelf.api.model.response.ReviewResponse;
import com.ichuvilin.bookshelf.service.BookService;
import com.ichuvilin.bookshelf.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<BookListResponse> list(@Valid @ModelAttribute BookListRequest request) {
        return ResponseEntity.ok(bookService.list(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> byId(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getById(id));
    }

    @GetMapping("/{id}/review")
    public ResponseEntity<List<ReviewResponse>> reviewList(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewsByBookId(id));
    }
}
