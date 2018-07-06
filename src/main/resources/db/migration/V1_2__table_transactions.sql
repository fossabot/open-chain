CREATE TABLE transaction_payload_types (
  id  INT PRIMARY KEY,
  key VARCHAR NOT NULL UNIQUE
);
INSERT INTO transaction_payload_types (id, key) VALUES (1, 'VOTE');
INSERT INTO transaction_payload_types (id, key) VALUES (2, 'DELEGATE_REGISTRATION');

--
CREATE TABLE transactions (
  id               INTEGER PRIMARY KEY,
  timestamp        BIGINT  NOT NULL,
  amount           BIGINT NOT NULL,
  recipient_key    VARCHAR NOT NULL,
  sender_key       VARCHAR NOT NULL,
  sender_signature VARCHAR NOT NULL,
  hash             VARCHAR NOT NULL,
  payload          VARCHAR NULLABLE,
  block_id         INTEGER NULLABLE REFERENCES blocks
);