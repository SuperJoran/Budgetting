ALTER TABLE T_BANKACCOUNT DROP FOREIGN KEY FK02_BANKACCOUNT;
DROP INDEX I01_BANKACCOUNT ON T_BANKACCOUNT;

ALTER TABLE T_BANKACCOUNT DROP COLUMN BANK_UUID;

DROP TABLE T_BANK;