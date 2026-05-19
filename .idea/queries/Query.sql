ALTER TABLE account DROP PRIMARY KEY;
ALTER TABLE account ADD PRIMARY KEY (`card`);
DESC account;
ALTER TABLE transaction ADD COLUMN to_name VARCHAR(50);
SELECT card, password FROM account;
# ALTER TABLE account CHANGE `limit` daily_limit DOUBLE NOT NULL;
