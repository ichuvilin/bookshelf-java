package com.ichuvilin.bookshelf.api.controller;

import com.ichuvilin.bookshelf.api.model.request.UpdateReviewRequest;
import com.ichuvilin.bookshelf.api.model.response.ReviewResponse;
import com.ichuvilin.bookshelf.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        reviewService.delete(userId, id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateReviewRequest reviewRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(reviewService.update(userId, id, reviewRequest));
    }
}
