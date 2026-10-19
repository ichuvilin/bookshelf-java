package com.ichuvilin.bookshelf.service;

import com.ichuvilin.bookshelf.api.model.response.ReviewResponse;
import com.ichuvilin.bookshelf.db.entity.Review;
import com.ichuvilin.bookshelf.db.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository repository;

    public List<ReviewResponse> getReviewsByBookId(Long bookId) {
        return repository.findAllByBookId(bookId).stream().map(this::toResponse).toList();
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getBookId(),
                review.getUserId(),
                review.getRating(),
                review.getTitle(),
                review.getContent(),
                review.getCreatedAt(),
                review.getUpdatedAt());
    }
}
