CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    phone VARCHAR(50)
);

INSERT INTO customers (full_name, address, phone)
SELECT DISTINCT ON (customer_full_name) customer_full_name, customer_address, customer_phone
FROM orders
ORDER BY customer_full_name, created_at;

ALTER TABLE orders ADD COLUMN customer_id BIGINT;

UPDATE orders o
SET customer_id = c.id
FROM customers c
WHERE c.full_name = o.customer_full_name;

ALTER TABLE orders ALTER COLUMN customer_id SET NOT NULL;

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    DROP COLUMN customer_full_name,
    DROP COLUMN customer_address,
    DROP COLUMN customer_phone;
