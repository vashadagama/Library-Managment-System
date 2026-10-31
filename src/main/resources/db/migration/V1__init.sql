-- Справочники (классификаторы)
CREATE TABLE genres (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE item_statuses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE loan_statuses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE user_statuses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE languages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE publishers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    city VARCHAR(100)
);

CREATE TABLE locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hall VARCHAR(100),
    shelf VARCHAR(100),
    rack VARCHAR(100)
);

CREATE TABLE system_parameters (
    key VARCHAR(100) PRIMARY KEY,
    value VARCHAR(255) NOT NULL,
    description TEXT
);

-- Основные сущности
CREATE TABLE authors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100)
);

CREATE TABLE library_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(500) NOT NULL,
    publication_date DATE,
    added_to_library_date DATE DEFAULT CURRENT_DATE NOT NULL,
    language_id UUID REFERENCES languages(id),
    publisher_id UUID REFERENCES publishers(id),
    location_id UUID REFERENCES locations(id)
);

CREATE TABLE books (
    id UUID PRIMARY KEY REFERENCES library_items(id) ON DELETE CASCADE,
    isbn VARCHAR(20) UNIQUE,
    page_count INTEGER CHECK (page_count > 0),
    genre_id UUID REFERENCES genres(id)
);

CREATE TABLE magazines (
    id UUID PRIMARY KEY REFERENCES library_items(id) ON DELETE CASCADE,
    issn VARCHAR(20) UNIQUE,
    page_count INTEGER CHECK (page_count > 0),
    has_glossy_cover BOOLEAN DEFAULT FALSE,
    genre_id UUID REFERENCES genres(id)
);

CREATE TABLE item_authors (
    item_id UUID NOT NULL REFERENCES library_items(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES authors(id) ON DELETE CASCADE,
    PRIMARY KEY (item_id, author_id)
);

CREATE TABLE item_copies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inventory_number VARCHAR(100) NOT NULL UNIQUE,
    acquisition_date DATE,
    price NUMERIC(10,2) CHECK (price >= 0),
    item_id UUID NOT NULL REFERENCES library_items(id) ON DELETE CASCADE,
    status_id UUID NOT NULL REFERENCES item_statuses(id)
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    email VARCHAR(255) NOT NULL UNIQUE,
    library_card_number VARCHAR(50) UNIQUE,
    registration_date DATE DEFAULT CURRENT_DATE NOT NULL,
    max_active_loans INTEGER DEFAULT 5 NOT NULL,
    password_hash VARCHAR(255),
    role_id UUID NOT NULL REFERENCES roles(id),
    status_id UUID NOT NULL REFERENCES user_statuses(id)
);

CREATE TABLE loans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    loan_date DATE DEFAULT CURRENT_DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    renewal_count INTEGER DEFAULT 0 NOT NULL CHECK (renewal_count >= 0),
    user_id UUID NOT NULL REFERENCES users(id),
    librarian_id UUID REFERENCES users(id),
    copy_id UUID NOT NULL REFERENCES item_copies(id),
    status_id UUID NOT NULL REFERENCES loan_statuses(id)
);