package com.ichuvilin.bookshelf.service;

import com.ichuvilin.bookshelf.api.model.request.ReviewCreationRequest;
import com.ichuvilin.bookshelf.db.entity.Review;
import com.ichuvilin.bookshelf.db.repository.BookRepository;
import com.ichuvilin.bookshelf.db.repository.ReviewRepository;
import com.ichuvilin.bookshelf.exception.BookNotFoundException;
import com.ichuvilin.bookshelf.exception.NotReviewOwnerException;
import com.ichuvilin.bookshelf.exception.ReviewNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void returnsReviewsWhenBookExists() {
        Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
        Review review = Review.builder()
                .id(1L)
                .bookId(7L)
                .userId(42L)
                .rating(5)
                .title("Great")
                .content("Loved it")
                .createdAt(timestamp)
                .updatedAt(timestamp)
                .build();
        when(bookRepository.existsById(7L)).thenReturn(true);
        when(reviewRepository.findAllByBookId(7L)).thenReturn(List.of(review));

        var result = reviewService.getReviewsByBookId(7L);

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().id());
        assertEquals(7L, result.getFirst().bookId());
        assertEquals(42L, result.getFirst().userId());
        assertEquals(5, result.getFirst().rating());
        assertEquals("Great", result.getFirst().title());
        verify(bookRepository).existsById(7L);
        verify(reviewRepository).findAllByBookId(7L);
    }

    @Test
    void throwsWhenBookDoesNotExist() {
        when(bookRepository.existsById(404L)).thenReturn(false);

        assertThrows(BookNotFoundException.class, () -> reviewService.getReviewsByBookId(404L));

        verifyNoInteractions(reviewRepository);
    }

    @Test
    void createsReviewWhenBookExists() {
        when(bookRepository.existsById(7L)).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review saved = invocation.getArgument(0);
            saved.setId(9L);
            return saved;
        });

        var response = reviewService.create(7L, 42L,
                new ReviewCreationRequest(5, "Great", "Loved it"));

        assertEquals(9L, response.id());
        assertEquals(7L, response.bookId());
        assertEquals(42L, response.userId());
        assertEquals(5, response.rating());
        assertEquals("Great", response.title());
        assertEquals("Loved it", response.content());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void doesNotCreateReviewWhenBookDoesNotExist() {
        when(bookRepository.existsById(404L)).thenReturn(false);

        assertThrows(BookNotFoundException.class,
                () -> reviewService.create(404L, 42L,
                        new ReviewCreationRequest(5, "Great", "Loved it")));

        verifyNoInteractions(reviewRepository);
    }

    @Test
    void deletesReviewWhenUserIsOwner() {
        Review review = review(9L, 7L, 42L);
        when(reviewRepository.findById(9L)).thenReturn(Optional.of(review));

        reviewService.delete(42L, 9L);

        verify(reviewRepository).delete(review);
    }

    @Test
    void rejectsDeletingReviewOwnedByAnotherUser() {
        Review review = review(9L, 7L, 42L);
        when(reviewRepository.findById(9L)).thenReturn(Optional.of(review));

        assertThrows(NotReviewOwnerException.class, () -> reviewService.delete(99L, 9L));

        verify(reviewRepository, never()).delete(any(Review.class));
    }

    @Test
    void throwsWhenReviewDoesNotExist() {
        when(reviewRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class, () -> reviewService.delete(42L, 404L));

        verify(reviewRepository, never()).delete(any(Review.class));
    }

    private Review review(Long id, Long bookId, Long userId) {
        return Review.builder()
                .id(id)
                .bookId(bookId)
                .userId(userId)
                .rating(5)
                .title("Great")
                .content("Loved it")
                .build();
    }

}
