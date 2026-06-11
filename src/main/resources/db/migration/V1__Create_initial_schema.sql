-- V1__Create_initial_schema.sql
-- Flyway 6.x migration format

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(13) NOT NULL UNIQUE,
    description VARCHAR(2000),
    published_year INT,
    price DECIMAL(10, 2),
    available_copies INT DEFAULT 0,
    is_active CHAR(1) DEFAULT 'Y',   -- used with Hibernate @Type(type="yes_no")
    category VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_book_title ON books(title);
CREATE INDEX idx_book_author ON books(author);

CREATE TABLE IF NOT EXISTS book_tags (
    book_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, tag_id),
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (tag_id) REFERENCES tags(id)
);

CREATE TABLE IF NOT EXISTS borrow_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    borrow_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    is_returned CHAR(1) DEFAULT 'N',  -- Hibernate @Type(type="yes_no")
    status VARCHAR(20) NOT NULL DEFAULT 'BORROWED',
    fine_amount DECIMAL(10, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Seed admin user (password: admin123, BCrypt encoded)
INSERT INTO users (username, email, password, full_name, is_active)
VALUES ('admin', 'admin@library.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'System Admin', TRUE);

INSERT INTO user_roles (user_id, role) VALUES (1, 'ADMIN');
INSERT INTO user_roles (user_id, role) VALUES (1, 'USER');

-- Seed sample books
INSERT INTO books (title, author, isbn, description, published_year, price, available_copies, is_active, category)
VALUES
('The Great Gatsby', 'F. Scott Fitzgerald', '9780743273565', 'A story of the fabulously wealthy Jay Gatsby', 1925, 12.99, 3, 'Y', 'FICTION'),
('Clean Code', 'Robert C. Martin', '9780132350884', 'A handbook of agile software craftsmanship', 2008, 35.99, 5, 'Y', 'TECHNOLOGY'),
('Sapiens', 'Yuval Noah Harari', '9780062316097', 'A brief history of humankind', 2011, 18.99, 4, 'Y', 'HISTORY'),
('The Pragmatic Programmer', 'Andrew Hunt', '9780201616224', 'From journeyman to master', 1999, 42.00, 2, 'Y', 'TECHNOLOGY');
