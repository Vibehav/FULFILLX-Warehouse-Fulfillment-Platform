CREATE TABLE skus (
                      id VARCHAR(36) PRIMARY KEY,
                      sku_code VARCHAR(100) NOT NULL UNIQUE,
                      name VARCHAR(255) NOT NULL,
                      description TEXT,
                      category VARCHAR(100) NOT NULL,
                      unit VARCHAR(50) NOT NULL,
                      weight DECIMAL(10,2) NOT NULL,
                      seller_id VARCHAR(36) NOT NULL,
                      tenant_id VARCHAR(36) NOT NULL,
                      status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                      created_at TIMESTAMP NOT NULL,
                      updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_skus_sku_code ON skus(sku_code);
CREATE INDEX idx_skus_seller_id ON skus(seller_id);
CREATE INDEX idx_skus_tenant_id ON skus(tenant_id);
CREATE INDEX idx_skus_category ON skus(category);