CREATE TABLE users(
                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                      email varchar(254) NOT NULL UNIQUE,
                      password varchar NOT NULL,
                      role varchar(10) NOT NULL DEFAULT 'READER'
);
CREATE TABLE refresh_tokens(
                               token UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               user_id UUID NOT NULL UNIQUE REFERENCES users(id)
                                   ON DELETE CASCADE,
                               expiration TIMESTAMP NOT NULL DEFAULT now()
);