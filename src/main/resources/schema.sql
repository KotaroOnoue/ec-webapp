CREATE TABLE IF NOT EXISTS products (
    product_id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description CLOB,
    price INTEGER NOT NULL,
    stock INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    image_url VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS cart_item (
    product_id BIGINT NOT NULL CHECK (product_id >= 1),
    quantity INTEGER NOT NULL CHECK (quantity BETWEEN 1 AND 99)
);
