package com.ichuvilin.bookshelf.service;

import com.ichuvilin.bookshelf.api.model.request.BookListRequest;
import com.ichuvilin.bookshelf.api.model.response.BookResponse;
import com.ichuvilin.bookshelf.db.entity.Book;
import com.ichuvilin.bookshelf.db.repository.BookRepository;
import com.ichuvilin.bookshelf.db.repository.specification.BookSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository repository;

    public List<BookResponse> list(BookListRequest request) {
        Sort.Direction direction = "desc".equalsIgnoreCase(request.order()) ? Sort.Direction.DESC : Sort.Direction.ASC;

        String sortField = switch (request.sort() == null ? "" : request.sort()) {
            case "title" -> "title";
            case "author" -> "author";
            default -> "createdAt";
        };

        Pageable pageable = PageRequest.of(request.page() - 1, request.limit(), Sort.by(direction, sortField));

        Specification<Book> specification = BookSpecifications.search(request.search());

        return repository
                .findAll(specification, pageable)
                .get()
                .map(book -> new BookResponse(
                        book.getId(),
                        book.getTitle(),
                        book.getDescription(),
                        book.getIsbn(),
                        book.getAuthor(),
                        book.getPublishedYear(),
                        book.getCreatedAt(),
                        book.getUpdatedAt()))
                .toList();
    }
}
