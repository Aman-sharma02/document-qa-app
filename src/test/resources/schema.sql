CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       encoded_password VARCHAR(255),
                       role VARCHAR(255)
);

CREATE TABLE files (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       title VARCHAR(255),
                       keyword VARCHAR(255),
                       filename VARCHAR(255),
                       content_type VARCHAR(255),
                       description VARCHAR(255),
                       editor_id BIGINT,
                       file_size BIGINT,
                       data BLOB
);
