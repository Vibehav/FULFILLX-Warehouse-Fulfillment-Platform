CREATE TABLE inventory (
                           id VARCHAR(36) PRIMARY KEY,
                           sku_id VARCHAR(36) NOT NULL,
                           warehouse_id VARCHAR(36) NOT NULL,
                           tenant_id VARCHAR(36) NOT NULL,
                           total_quantity INTEGER NOT NULL DEFAULT 0,
                           reserved_quantity INTEGER NOT NULL DEFAULT 0,
                           available_quantity INTEGER NOT NULL DEFAULT 0,
                           low_stock_threshold INTEGER NOT NULL DEFAULT 10,
                           status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                           version BIGINT NOT NULL DEFAULT 0,
                           created_at TIMESTAMP NOT NULL,
                           updated_at TIMESTAMP NOT NULL,
                           CONSTRAINT uq_sku_warehouse_tenant
                               UNIQUE (sku_id, warehouse_id, tenant_id)
);

CREATE TABLE processed_grn_ledger (grn_id VARCHAR(255) PRIMARY KEY);

CREATE INDEX idx_inventory_sku_id ON inventory(sku_id);
CREATE INDEX idx_inventory_warehouse_id ON inventory(warehouse_id);
CREATE INDEX idx_inventory_tenant_id ON inventory(tenant_id);
CREATE INDEX idx_inventory_status ON inventory(status);
CREATE INDEX idx_inventory_sku_warehouse ON inventory(sku_id, warehouse_id);