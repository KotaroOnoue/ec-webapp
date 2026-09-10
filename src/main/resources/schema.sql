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

CREATE TABLE IF NOT EXISTS orders (
    order_id BIGINT PRIMARY KEY CHECK (order_id >= 1),
    customer_name VARCHAR(100) NOT NULL,
    postal_code VARCHAR(8) NOT NULL CHECK (postal_code REGEXP '^[0-9]{7}$'),
    adress VARCHAR(255) NOT NULL,
    phone_number VARCHAR(11) NOT NULL CHECK (phone_number REGEXP '^[0-9]{10,11}$'),
    total_amount INTEGER NOT NULL CHECK (total_amount >= 0),
    order_at TIMESTAMP NOT NULL
);
