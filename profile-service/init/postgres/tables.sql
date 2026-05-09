CREATE TABLE persons(
    id UUID PRIMARY KEY,
    first_name VARCHAR(20) NOT NULL,
    last_name VARCHAR(20) NOT NULL ,
    date_of_birth TIMESTAMP NOT NULL,
    date_of_registration TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    biography TEXT
);
