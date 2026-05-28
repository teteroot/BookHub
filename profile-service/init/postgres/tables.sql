CREATE TABLE persons(
    id UUID PRIMARY KEY,
    first_name VARCHAR(20) NOT NULL,
    last_name VARCHAR(20) NOT NULL ,
    date_of_birth TIMESTAMP NOT NULL,
    date_of_registration TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    biography TEXT,
    role varchar(10) NOT NULL
);

CREATE TABLE person_favorites(
    favorite_author_id UUID NOT NULL REFERENCES persons(id),
    marked_as_favorite_by_id UUID NOT NULL REFERENCES persons(id),
    PRIMARY KEY (favorite_author_id,marked_as_favorite_by_id)
);