DELETE FROM cart_item;

DELETE FROM discount_codes;

DELETE FROM products;

INSERT INTO discount_codes (discount_code, discount_rate) VALUES
    (1001, 0.10),
    (2002, 0.20),
    (3003, 0.30);

INSERT INTO products (product_id, name, description, price, stock, status, image_url) VALUES
    (1, 'ワイヤレスイヤホン', 'ノイズキャンセリング対応', 5980, 10, 'ON_SALE', '/images/products/1.png'),
    (2, 'ゲーミングマウス', '6ボタン搭載', 3980, 5, 'ON_SALE', '/images/products/2.png'),
    (3, 'USB-Cハブ', '5in1モデル', 2980, 0, 'ON_SALE', '/images/products/3.png'),
    (4, 'Webカメラ', 'フルHD対応', 4980, 8, 'STOPPED', '/images/products/4.png');
