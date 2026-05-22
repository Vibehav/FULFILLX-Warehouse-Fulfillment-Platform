ALTER TABLE skus DROP COLUMN seller_id;
DROP INDEX IF EXISTS idx_skus_seller_id;