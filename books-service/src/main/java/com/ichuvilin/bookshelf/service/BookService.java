package com.ichuvilin.bookshelf.service;

import com.ichuvilin.bookshelf.api.model.request.BookCreationRequest;
import com.ichuvilin.bookshelf.api.model.request.BookListRequest;
import com.ichuvilin.bookshelf.api.model.request.BookUpdateRequest;
import com.ichuvilin.bookshelf.api.model.response.BookListResponse;
import com.ichuvilin.bookshelf.api.model.response.BookResponse;
import com.ichuvilin.bookshelf.api.model.response.PaginationResponse;
import com.ichuvilin.bookshelf.db.entity.Book;
import com.ichuvilin.bookshelf.db.repository.BookRepository;
import com.ichuvilin.bookshelf.db.repository.specification.BookSpecifications;
import com.ichuvilin.bookshelf.exception.BookNotFoundException;
import com.ichuvilin.bookshelf.exception.NotBookOwnerException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository repository;

    public BookListResponse list(BookListRequest request) {
        Sort.Direction direction = "desc".equalsIgnoreCase(request.order()) ? Sort.Direction.DESC : Sort.Direction.ASC;

        String sortField = switch (request.sort() == null ? "" : request.sort()) {
            case "title" -> "title";
            case "author" -> "author";
            default -> "createdAt";
        };

        Pageable pageable = PageRequest.of(request.page() - 1, request.limit(), Sort.by(direction, sortField));

        Specification<Book> specification = BookSpecifications.search(request.search());

        Page<Book> books = repository.findAll(specification, pageable);

        List<BookResponse> data =
                books.getContent().stream().map(this::toResponse).toList();

        PaginationResponse pagination = new PaginationResponse(
                books.getNumber() + 1, books.getSize(), books.getTotalElements(), books.getTotalPages());

        return new BookListResponse(data, pagination);
    }

    public BookResponse getById(Long id) {
        return repository.findById(id).map(this::toResponse).orElseThrow(BookNotFoundException::new);
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getDescription(),
                book.getIsbn(),
                book.getAuthor(),
                book.getPublishedYear(),
                book.getCreatedAt(),
                book.getUpdatedAt());
    }

    @Transactional(rollbackFor = Exception.class)
    public BookResponse create(BookCreationRequest request, Long userId) {
        Book book = Book.builder()
                .title(request.title())
                .author(request.author())
                .description(request.description())
                .isbn(request.isbn())
                .publishedYear(request.publishedYear())
                .userId(userId)
                .build();
        return toResponse(repository.save(book));
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long userId) {
        Book book = repository.findById(id).orElseThrow(BookNotFoundException::new);
        if (!book.getUserId().equals(userId)) {
            throw new NotBookOwnerException();
        }
        repository.delete(book);
    }

    @Transactional(rollbackFor = Exception.class)
    public BookResponse update(Long id, Long userId, BookUpdateRequest request) {
        Book book = repository.findById(id).orElseThrow(BookNotFoundException::new);
        if (!book.getUserId().equals(userId)) {
            throw new NotBookOwnerException();
        }

        if (book.getTitle() != null
                && request.title() != null
                && !request.title().equals(book.getTitle())) {
            book.setTitle(request.title());
        }

        if (book.getDescription() != null
                && request.description() != null
                && !request.description().equals(book.getDescription())) {
            book.setDescription(request.description());
        }

        if (book.getAuthor() != null
                && request.author() != null
                && !request.author().equals(book.getAuthor())) {
            book.setAuthor(request.author());
        }

        return toResponse(repository.save(book));
    }
}
