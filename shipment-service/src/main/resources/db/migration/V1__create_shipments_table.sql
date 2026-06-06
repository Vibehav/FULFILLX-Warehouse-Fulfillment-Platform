CREATE TABLE shipments (
                           id VARCHAR(36) PRIMARY KEY,
                           order_id VARCHAR(36) NOT NULL UNIQUE,
                           tenant_id VARCHAR(36) NOT NULL,
                           warehouse_id VARCHAR(36) NOT NULL,
                           seller_id VARCHAR(36) NOT NULL,
                           status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
                           courier_partner VARCHAR(20) NOT NULL,
                           tracking_id VARCHAR(100),
                           delivered_at TIMESTAMP,
                           created_at TIMESTAMP NOT NULL,
                           updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_shipments_order_id ON shipments(order_id);
CREATE INDEX idx_shipments_tenant_id ON shipments(tenant_id);
CREATE INDEX idx_shipments_status ON shipments(status);
CREATE INDEX idx_shipments_courier ON shipments(courier_partner);