CREATE TABLE T_PERSON
(
  ID INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  PASSWORD VARCHAR(255) NOT NULL,
  USERNAME VARCHAR(255) NOT NULL
);
CREATE UNIQUE INDEX UN01_PERSON ON T_PERSON (USERNAME);
CREATE TABLE T_BANK
(
  ID INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  NAME VARCHAR(255)
);
CREATE TABLE T_BANKACCOUNT
(
  ID INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  NUMBER VARCHAR(255) NOT NULL,
  ADMINISTRATOR_ID INT(11) NOT NULL,
  BANK_ID INT(11),
  OWNER_ID INT(11),
  CONSTRAINT FK01_BANKACCOUNT FOREIGN KEY (ADMINISTRATOR_ID) REFERENCES T_PERSON (ID),
  CONSTRAINT FK02_BANKACCOUNT FOREIGN KEY (BANK_ID) REFERENCES T_BANK (ID),
  CONSTRAINT FK03_BANKACCOUNT FOREIGN KEY (OWNER_ID) REFERENCES T_PERSON (ID)
);
CREATE INDEX I01_BANKACCOUNT ON T_BANKACCOUNT (BANK_ID);
CREATE INDEX I02_BANKACCOUNT ON T_BANKACCOUNT (ADMINISTRATOR_ID);
CREATE INDEX I03_BANKACCOUNT ON T_BANKACCOUNT (OWNER_ID);
CREATE UNIQUE INDEX UN_BANKACCOUNT ON T_BANKACCOUNT (NUMBER, ADMINISTRATOR_ID);
CREATE TABLE T_CAR
(
  ID INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  BRAND VARCHAR(255),
  FUELTYPE INT(11),
  MODEL VARCHAR(255),
  PURCHASEDATE DATE,
  PURCHASEPRICE DOUBLE,
  OWNER_ID INT(11),
  CONSTRAINT FK01_CAR FOREIGN KEY (OWNER_ID) REFERENCES T_PERSON (ID)
);
CREATE INDEX I01_CAR ON T_CAR (OWNER_ID);
CREATE TABLE T_CATEGORY
(
  ID INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  NAME VARCHAR(255)
);
CREATE TABLE T_FINANCIAL_INSTRUMENT
(
  ID INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  QUOTE VARCHAR(255)
);
CREATE UNIQUE INDEX UN01_FINANCIAL_INSTRUMENT ON T_FINANCIAL_INSTRUMENT (QUOTE);
CREATE TABLE T_FUND_PURCHASE
(
  ID INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  DATE DATE NOT NULL,
  NUMBEROFSHARES INT(11) NOT NULL,
  QUOTE VARCHAR(255),
  SHAREPRICE DOUBLE NOT NULL,
  TRANSACTIONCOST DOUBLE,
  PERSON_ID INT(11),
  CONSTRAINT FK01_FUND_PURCHASE FOREIGN KEY (PERSON_ID) REFERENCES T_PERSON (ID)
);
CREATE INDEX I01_FUND_PURCHASE ON T_FUND_PURCHASE (PERSON_ID);
CREATE TABLE T_HISTORIC_PRICE
(
  ID INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  DATE DATE,
  PRICE DOUBLE,
  FINANCIAL_INSTRUMENT_ID INT(11) NOT NULL,
  CONSTRAINT FK01_HISTORIC_PRICE FOREIGN KEY (FINANCIAL_INSTRUMENT_ID) REFERENCES T_FINANCIAL_INSTRUMENT (ID)
);
CREATE UNIQUE INDEX UN_HISTORIC_PRICE_01 ON T_HISTORIC_PRICE (FINANCIAL_INSTRUMENT_ID, DATE);
CREATE TABLE T_REFUELING
(
  ID INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  DATE DATE,
  KILOMETRES DOUBLE,
  LITERS DOUBLE,
  PRICE DOUBLE,
  PRICEPERLITER DOUBLE,
  CAR_ID INT(11),
  FUELTANKFULL BIT(1) NOT NULL ,
  CONSTRAINT FK01_REFUELING FOREIGN KEY (CAR_ID) REFERENCES T_CAR (ID)
);
CREATE INDEX I01_REFUELING ON T_REFUELING (CAR_ID);
CREATE TABLE T_STATEMENT
(
  ID INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  AMOUNT DECIMAL(19,2),
  DATE DATE,
  DESTINATIONACCOUNT_ID INT(11),
  ORIGINATINGACCOUNT_ID INT(11),
  CONSTRAINT FK01_STATEMENT FOREIGN KEY (DESTINATIONACCOUNT_ID) REFERENCES T_BANKACCOUNT (ID),
  CONSTRAINT FK02_STATEMENT FOREIGN KEY (ORIGINATINGACCOUNT_ID) REFERENCES T_BANKACCOUNT (ID)
);
CREATE INDEX I01_STATEMENT ON T_STATEMENT (DESTINATIONACCOUNT_ID);
CREATE INDEX I02_STATEMENT ON T_STATEMENT (ORIGINATINGACCOUNT_ID);