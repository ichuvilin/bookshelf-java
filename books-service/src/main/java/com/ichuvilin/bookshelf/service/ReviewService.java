package com.ichuvilin.bookshelf.service;

import com.ichuvilin.bookshelf.api.model.response.ReviewResponse;
import com.ichuvilin.bookshelf.db.entity.Review;
import com.ichuvilin.bookshelf.db.repository.BookRepository;
import com.ichuvilin.bookshelf.db.repository.ReviewRepository;
import com.ichuvilin.bookshelf.exception.BookNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository repository;
    private final BookRepository bookRepository;

    public List<ReviewResponse> getReviewsByBookId(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new BookNotFoundException();
        }

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
