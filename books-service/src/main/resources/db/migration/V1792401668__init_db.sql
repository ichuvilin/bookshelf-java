CREATE TABLE IF NOT EXISTS books
(
    id             BIGSERIAL PRIMARY KEY,
    title          VARCHAR(500) NOT NULL,
    author         VARCHAR(255) NOT NULL,
    description    TEXT,
    isbn           VARCHAR(20),
    published_year DATE,
    created_by     BIGINT       NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_books_created_by ON books (created_by);
CREATE INDEX idx_books_title ON books (title);
CREATE INDEX idx_books_author ON books (author);
CREATE INDEX idx_books_created_at ON books (created_at);

CREATE TABLE IF NOT EXISTS reviews
(
    id         BIGSERIAL PRIMARY KEY,
    book_id    BIGINT  NOT NULL REFERENCES books (id) ON DELETE CASCADE,
    user_id    BIGINT  NOT NULL,
    rating     INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title      VARCHAR(255),
    content    TEXT    NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE (book_id, user_id)
);

CREATE INDEX idx_reviews_book_id ON reviews (book_id);
CREATE INDEX idx_reviews_user_id ON reviews (user_id);