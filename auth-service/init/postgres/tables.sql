CREATE TABLE users(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email varchar(30) NOT NULL UNIQUE,
    password varchar NOT NULL UNIQUE,
    role varchar(10) NOT NULL DEFAULT 'READER'
);
