CREATE TABLE grns (
                      id VARCHAR(36) PRIMARY KEY,
                      vendor_id VARCHAR(36) NOT NULL,
                      warehouse_id VARCHAR(36) NOT NULL,
                      tenant_id VARCHAR(36) NOT NULL,
                      status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
                      remarks TEXT,
                      created_at TIMESTAMP NOT NULL,
                      updated_at TIMESTAMP NOT NULL,
                      confirmed_at TIMESTAMP
);

CREATE TABLE grn_items (
                           id VARCHAR(36) PRIMARY KEY,
                           grn_id VARCHAR(36) NOT NULL,
                           sku_id VARCHAR(36) NOT NULL,
                           quantity INTEGER NOT NULL,
                           received_quantity INTEGER NOT NULL DEFAULT 0,
                           status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                           rejection_reason TEXT,
                           CONSTRAINT fk_grn FOREIGN KEY (grn_id) REFERENCES grns(id)
);

