CREATE TABLE books (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    date_of_publishing TIMESTAMPTZ,
    count_of_pages INTEGER NOT NULL DEFAULT 0,
    s3_archive_path VARCHAR(512),
    s3_cover_path VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_books_author_id ON books (author_id);
CREATE INDEX idx_books_status ON books (status);

CREATE TABLE book_pages (
    id UUID PRIMARY KEY,
    book_id UUID NOT NULL,
    page_number INTEGER NOT NULL,
    source_type VARCHAR(20) NOT NULL,
    original_page_index INTEGER,
    s3_patch_path VARCHAR(512),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
        CONSTRAINT fk_book_pages_book
            FOREIGN KEY (book_id)
                REFERENCES books (id)
                ON DELETE CASCADE,
        CONSTRAINT uq_book_pages_book_page
            UNIQUE (book_id, page_number)
);

CREATE INDEX idx_book_pages_book_source
    ON book_pages (book_id, source_type);
