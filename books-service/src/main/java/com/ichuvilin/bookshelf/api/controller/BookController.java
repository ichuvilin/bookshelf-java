package com.ichuvilin.bookshelf.api.controller;

import com.ichuvilin.bookshelf.api.model.request.BookCreationRequest;
import com.ichuvilin.bookshelf.api.model.request.BookListRequest;
import com.ichuvilin.bookshelf.api.model.request.BookUpdateRequest;
import com.ichuvilin.bookshelf.api.model.request.ReviewCreationRequest;
import com.ichuvilin.bookshelf.api.model.response.BookListResponse;
import com.ichuvilin.bookshelf.api.model.response.BookResponse;
import com.ichuvilin.bookshelf.api.model.response.ReviewResponse;
import com.ichuvilin.bookshelf.service.BookService;
import com.ichuvilin.bookshelf.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<ReviewResponse>> reviewList(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewsByBookId(id));
    }

    @PostMapping
    public ResponseEntity<BookResponse> create(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody BookCreationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.create(request, userId));
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ReviewCreationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(id, userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        bookService.delete(id, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id, @AuthenticationPrincipal Long userId, @RequestBody BookUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(bookService.update(id, userId, request));
    }
}
