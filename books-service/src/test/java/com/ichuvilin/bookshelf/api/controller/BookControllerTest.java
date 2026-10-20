package com.ichuvilin.bookshelf.api.controller;

import com.ichuvilin.bookshelf.api.model.response.BookListResponse;
import com.ichuvilin.bookshelf.api.model.response.BookResponse;
import com.ichuvilin.bookshelf.api.model.response.PaginationResponse;
import com.ichuvilin.bookshelf.api.model.response.ReviewResponse;
import com.ichuvilin.bookshelf.exception.GlobalExceptionHandler;
import com.ichuvilin.bookshelf.service.BookService;
import com.ichuvilin.bookshelf.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import(GlobalExceptionHandler.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private ReviewService reviewService;

    @Test
    void listReturnsDataAndPagination() throws Exception {
        Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
        BookResponse book = new BookResponse(
                1L, "Clean Code", "Description", "978-0134685991", "Robert C. Martin",
                null, timestamp, timestamp);
        given(bookService.list(any()))
                .willReturn(new BookListResponse(List.of(book), new PaginationResponse(1, 10, 21, 3)));

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Clean Code"))
                .andExpect(jsonPath("$.data[0].isbn").value("978-0134685991"))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.limit").value(10))
                .andExpect(jsonPath("$.pagination.total").value(21))
                .andExpect(jsonPath("$.pagination.totalPages").value(3));
    }

    @Test
    void listRejectsPageBelowOne() throws Exception {
        mockMvc.perform(get("/api/v1/books").param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void listRejectsNonNumericPage() throws Exception {
        mockMvc.perform(get("/api/v1/books").param("page", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void listRejectsUnsupportedSort() throws Exception {
        mockMvc.perform(get("/api/v1/books").param("sort", "password"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void listRejectsUnsupportedOrder() throws Exception {
        mockMvc.perform(get("/api/v1/books").param("order", "sideways"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void reviewListReturnsReviews() throws Exception {
        Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
        given(reviewService.getReviewsByBookId(7L)).willReturn(List.of(
                new ReviewResponse(1L, 7L, 42L, 5, "Great", "Loved it", timestamp, timestamp)));

        mockMvc.perform(get("/api/v1/books/7/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].bookId").value(7))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[0].title").value("Great"));
    }
}
