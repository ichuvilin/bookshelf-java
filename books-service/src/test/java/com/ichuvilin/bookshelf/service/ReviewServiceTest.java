package com.ichuvilin.bookshelf.service;

import com.ichuvilin.bookshelf.db.entity.Review;
import com.ichuvilin.bookshelf.db.repository.BookRepository;
import com.ichuvilin.bookshelf.db.repository.ReviewRepository;
import com.ichuvilin.bookshelf.exception.BookNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
}
