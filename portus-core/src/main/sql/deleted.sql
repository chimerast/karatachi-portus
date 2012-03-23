CREATE SCHEMA deleted;

CREATE TABLE deleted.file (
  id BIGINT PRIMARY KEY,
  registered TIMESTAMP NOT NULL,
  updated TIMESTAMP NOT NULL,
  deleted TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  parent_id BIGINT NOT NULL,

  directory BOOLEAN NOT NULL,

  name TEXT NOT NULL,
  size BIGINT NOT NULL,
  digest TEXT,

  full_path TEXT NOT NULL
);

CREATE OR REPLACE FUNCTION portus.file_log_delete() RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO deleted.file(id, registered, updated, parent_id, directory, name, size, digest, full_path)
    VALUES(OLD.id, OLD.registered, OLD.updated, OLD.parent_id, OLD.directory, OLD.name, OLD.size, OLD.digest, OLD.full_path);
  RETURN NULL;
END
$$ LANGUAGE plpgsql;
CREATE TRIGGER file_log_delete AFTER DELETE ON portus.file
  FOR EACH ROW EXECUTE PROCEDURE portus.file_log_delete();


CREATE TABLE deleted.customer (
  id BIGINT PRIMARY KEY,
  registered TIMESTAMP NOT NULL,
  updated TIMESTAMP NOT NULL,
  deleted TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  parent_id BIGINT NOT NULL,

  name TEXT NOT NULL,
);
CREATE OR REPLACE FUNCTION portus.customer_log_delete() RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO deleted.file(id, registered, updated, parent_id, name)
    VALUES(OLD.id, OLD.registered, OLD.updated, OLD.parent_id, OLD.directory, OLD.name, OLD.size, OLD.digest, OLD.full_path);
  RETURN NULL;
END
$$ LANGUAGE plpgsql;
CREATE TRIGGER customer_log_delete AFTER DELETE ON portus.customer
  FOR EACH ROW EXECUTE PROCEDURE portus.customer_log_delete();


CREATE TABLE deleted.domain (
  id BIGINT PRIMARY KEY,
  registered TIMESTAMP NOT NULL,
  updated TIMESTAMP NOT NULL,
  deleted TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  customer_id BIGINT NOT NULL,

  name TEXT NOT NULL,
);

CREATE TABLE deleted.account (
  id BIGINT PRIMARY KEY,
  registered TIMESTAMP NOT NULL,
  updated TIMESTAMP NOT NULL,
  deleted TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  customer_id BIGINT NOT NULL,

  name TEXT NOT NULL,
);
