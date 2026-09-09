CREATE TABLE IF NOT EXISTS products (
    product_id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description CLOB,
    price INTEGER NOT NULL,
    stock INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    image_url VARCHAR(255)
);
