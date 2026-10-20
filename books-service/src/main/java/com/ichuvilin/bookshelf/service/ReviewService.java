package com.ichuvilin.bookshelf.service;

import com.ichuvilin.bookshelf.api.model.request.ReviewCreationRequest;
import com.ichuvilin.bookshelf.api.model.request.UpdateReviewRequest;
import com.ichuvilin.bookshelf.api.model.response.ReviewResponse;
import com.ichuvilin.bookshelf.db.entity.Review;
import com.ichuvilin.bookshelf.db.repository.BookRepository;
import com.ichuvilin.bookshelf.db.repository.ReviewRepository;
import com.ichuvilin.bookshelf.exception.BookNotFoundException;
import com.ichuvilin.bookshelf.exception.NotReviewOwnerException;
import com.ichuvilin.bookshelf.exception.ReviewNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(rollbackFor = Exception.class)
    public ReviewResponse create(Long bookId, Long userId, ReviewCreationRequest request) {
        Review review = Review.builder()
                .bookId(bookId)
                .userId(userId)
                .rating(request.rating())
                .title(request.title())
                .content(request.content())
                .build();
        return toResponse(repository.save(review));
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long id) {
        Review review = repository.findById(id).orElseThrow(ReviewNotFoundException::new);
        if (!review.getUserId().equals(userId)) {
            throw new NotReviewOwnerException();
        }
        repository.delete(review);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReviewResponse update(Long userId, Long id, @Valid UpdateReviewRequest request) {
        Review review = repository.findById(id).orElseThrow(ReviewNotFoundException::new);
        if (!review.getUserId().equals(userId)) {
            throw new NotReviewOwnerException();
        }

        if (review.getTitle() != null
                && request.title() != null
                && !request.title().equals(review.getTitle())) {
            review.setTitle(request.title());
        }

        if (request.content() != null && !request.content().equals(review.getContent())) {
            review.setContent(request.content());
        }

        review.setRating(request.rating());

        return toResponse(repository.save(review));
    }
}
