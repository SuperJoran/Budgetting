ALTER TABLE T_CATEGORY DROP FOREIGN KEY FK01_CATEGORY;
DROP INDEX FK01_CATEGORY ON T_CATEGORY;
ALTER TABLE T_CATEGORY DROP ADMINISTRATOR_UUID;