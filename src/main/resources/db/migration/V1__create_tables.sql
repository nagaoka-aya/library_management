CREATE TABLE author
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    birth_date DATE         NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE book
(
    id    BIGINT       NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    price INTEGER      NOT NULL CHECK (price >= 0),
    PRIMARY KEY (id)
);

CREATE TABLE publication
(
    book_id      BIGINT    NOT NULL,
    published_at TIMESTAMP NOT NULL,
    PRIMARY KEY (book_id),
    FOREIGN KEY (book_id) REFERENCES book (id)
);

CREATE TABLE book_author
(
    book_id   BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES book (id),
    FOREIGN KEY (author_id) REFERENCES author (id)
);
