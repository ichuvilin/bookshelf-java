package com.ichuvilin.bookshelf.db.repository.specification;

import com.ichuvilin.bookshelf.db.entity.Book;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public class BookSpecifications {
    private BookSpecifications() {}

    public static Specification<Book> search(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }

            String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";

            return cb.or(cb.like(cb.lower(root.get("title")), pattern), cb.like(cb.lower(root.get("author")), pattern));
        };
    }
}
