ALTER TABLE order_items ADD COLUMN product_id BIGINT;

UPDATE order_items oi
SET product_id = p.id
FROM products p
WHERE p.name = oi.product_name
  AND p.price = oi.product_price;

ALTER TABLE order_items ALTER COLUMN product_id SET NOT NULL;

ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id),
    DROP COLUMN product_name,
    DROP COLUMN product_price;
