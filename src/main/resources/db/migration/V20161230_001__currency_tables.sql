DELETE FROM T_HISTORIC_PRICE;


ALTER TABLE T_HISTORIC_PRICE DROP COLUMN PRICE;

ALTER TABLE T_HISTORIC_PRICE
    ADD COLUMN AMOUNT DOUBLE;

ALTER TABLE T_HISTORIC_PRICE
    ADD COLUMN CURRENCY VARCHAR(255);