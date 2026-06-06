CREATE TABLE orders (
                        id VARCHAR(36) PRIMARY KEY,
                        seller_id VARCHAR(36) NOT NULL,
                        tenant_id VARCHAR(36) NOT NULL,
                        warehouse_id VARCHAR(36) NOT NULL,
                        status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                        remarks TEXT,
                        created_at TIMESTAMP NOT NULL,
                        updated_at TIMESTAMP NOT NULL,
                        confirmed_at TIMESTAMP,
                        delivered_at TIMESTAMP
);

CREATE TABLE order_items (
                             id VARCHAR(36) PRIMARY KEY,
                             order_id VARCHAR(36) NOT NULL,
                             sku_id VARCHAR(36) NOT NULL,
                             quantity INTEGER NOT NULL,
                             warehouse_id VARCHAR(36) NOT NULL,
                             status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                             CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE INDEX idx_orders_tenant_id ON orders(tenant_id);
CREATE INDEX idx_orders_seller_id ON orders(seller_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_warehouse_id ON orders(warehouse_id);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_sku_id ON order_items(sku_id);
CREATE INDEX idx_order_items_status ON order_items(status);