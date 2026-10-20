package com.ichuvilin.bookshelf.service;

import com.ichuvilin.bookshelf.api.model.request.BookListRequest;
import com.ichuvilin.bookshelf.api.model.request.BookUpdateRequest;
import com.ichuvilin.bookshelf.api.model.response.BookListResponse;
import com.ichuvilin.bookshelf.db.entity.Book;
import com.ichuvilin.bookshelf.db.repository.BookRepository;
import com.ichuvilin.bookshelf.exception.BookNotFoundException;
import com.ichuvilin.bookshelf.exception.NotBookOwnerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository repository;

    @InjectMocks
    private BookService bookService;

    @Test
    void listReturnsBooksAndPaginationMetadata() {
        BookListRequest request = new BookListRequest("clean", "title", "desc", 2, 5);
        Pageable expectedPageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.DESC, "title"));
        Book book = book(7L, "Clean Code", "Robert C. Martin");
        Page<Book> page = new PageImpl<>(List.of(book), expectedPageable, 12);
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        BookListResponse response = bookService.list(request);

        assertEquals(1, response.data().size());
        assertEquals(7L, response.data().getFirst().id());
        assertEquals("Clean Code", response.data().getFirst().title());
        assertEquals(book.getUserId(), response.data().getFirst().createdBy());
        assertEquals(2, response.pagination().page());
        assertEquals(5, response.pagination().limit());
        assertEquals(12, response.pagination().total());
        assertEquals(3, response.pagination().totalPages());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), pageableCaptor.capture());
        assertEquals(expectedPageable, pageableCaptor.getValue());
    }

    @Test
    void listUsesDefaultsAndReturnsEmptyPage() {
        BookListRequest request = new BookListRequest(null, null, null, null, null);
        Pageable expectedPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdAt"));
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), expectedPageable, 0));

        BookListResponse response = bookService.list(request);

        assertTrue(response.data().isEmpty());
        assertEquals(1, response.pagination().page());
        assertEquals(10, response.pagination().limit());
        assertEquals(0, response.pagination().total());
        assertEquals(0, response.pagination().totalPages());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), pageableCaptor.capture());
        assertEquals(expectedPageable, pageableCaptor.getValue());
    }

    @Test
    void getByIdReturnsBookResponse() {
        Book book = book(42L, "Effective Java", "Joshua Bloch");
        when(repository.findById(42L)).thenReturn(Optional.of(book));

        var response = bookService.getById(42L);

        assertEquals(42L, response.id());
        assertEquals("Effective Java", response.title());
        assertEquals("Joshua Bloch", response.author());
    }

    @Test
    void getByIdThrowsWhenBookDoesNotExist() {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.getById(404L));
    }


    @Test
    void deletesBookWhenUserIsOwner() {
        Book book = book(7L, "Clean Code", "Robert C. Martin");
        book.setUserId(42L);
        when(repository.findById(7L)).thenReturn(Optional.of(book));

        bookService.delete(7L, 42L);

        verify(repository).delete(book);
    }

    @Test
    void rejectsDeletingBookOwnedByAnotherUser() {
        Book book = book(7L, "Clean Code", "Robert C. Martin");
        book.setUserId(42L);
        when(repository.findById(7L)).thenReturn(Optional.of(book));

        assertThrows(NotBookOwnerException.class, () -> bookService.delete(7L, 99L));

        verify(repository, never()).delete(any(Book.class));
    }

    @Test
    void updateCanFillPreviouslyNullDescription() {
        Book book = book(7L, "Clean Code", "Robert C. Martin");
        book.setUserId(42L);
        book.setDescription(null);
        when(repository.findById(7L)).thenReturn(Optional.of(book));
        when(repository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = bookService.update(7L, 42L,
                new BookUpdateRequest(null, "New description", null, null, null));

        assertEquals("New description", response.description());
    }

    private Book book(Long id, String title, String author) {
        Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
        return Book.builder()
                .id(id)
                .title(title)
                .author(author)
                .description("Description")
                .isbn("978-0134685991")
                .publishedYear(LocalDate.of(2018, 1, 1))
                .userId(42L)
                .createdAt(timestamp)
                .updatedAt(timestamp)
                .build();
    }
}
