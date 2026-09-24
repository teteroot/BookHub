CREATE TABLE persons(
                        id UUID PRIMARY KEY,
                        first_name VARCHAR(20) NOT NULL,
                        last_name VARCHAR(20) NOT NULL ,
                        date_of_birth TIMESTAMP NOT NULL,
                        date_of_registration TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        biography TEXT,
                        role varchar(10) NOT NULL
);

CREATE TABLE favorite_authors(
                                 favorite_author_id UUID NOT NULL REFERENCES persons(id),
                                 marked_as_favorite_by_id UUID NOT NULL REFERENCES persons(id),
                                 PRIMARY KEY (favorite_author_id,marked_as_favorite_by_id)
);

CREATE TABLE favorite_books (
                                id UUID PRIMARY KEY,
                                book_id UUID NOT NULL,
                                person_id UUID NOT NULL,
                                created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                CONSTRAINT uq_person_book UNIQUE (person_id, book_id)
);
CREATE INDEX idx_favorite_books_id ON favorite_books (id);
CREATE INDEX idx_persons_role ON persons (role);
CREATE INDEX idx_persons_lower_name
    ON persons (LOWER(first_name), LOWER(last_name));