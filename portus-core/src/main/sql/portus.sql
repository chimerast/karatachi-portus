CREATE LANGUAGE plpgsql;

CREATE SCHEMA portus;

--------------------------------------------------------------------------------
-- コンフィグ関連
--------------------------------------------------------------------------------
CREATE TABLE portus.config (
  key text PRIMARY KEY,
  value text
);

CREATE TABLE portus.network (
  id BIGSERIAL PRIMARY KEY,
  carrier text NOT NULL,
  ip_address text NOT NULL
);

CREATE TABLE portus.ssl_crl (
  serial BIGINT PRIMARY KEY
);

--------------------------------------------------------------------------------
-- ノード関連
--------------------------------------------------------------------------------
CREATE TABLE portus.node_block (
  id BIGSERIAL PRIMARY KEY,
  registered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  name TEXT
);

CREATE TABLE portus.node_type (
  id BIGSERIAL PRIMARY KEY,
  registered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  name TEXT
);

CREATE TABLE portus.node (
  id BIGINT PRIMARY KEY,
  registered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  node_block_id BIGINT NOT NULL DEFAULT 0,
  node_type_id INTEGER NOT NULL DEFAULT 0,

  status INTEGER NOT NULL DEFAULT 0,
  update INTEGER NOT NULL DEFAULT 0,

  ip_address TEXT NOT NULL DEFAULT '0.0.0.0',
  ctrl_port INTEGER NOT NULL DEFAULT 0,
  http_port INTEGER NOT NULL DEFAULT 0,

  bootstrap_revision INTEGER NOT NULL DEFAULT 0,
  node_revision INTEGER NOT NULL DEFAULT 0,
  protocol_revision INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE portus.node_event (
  id BIGSERIAL PRIMARY KEY,
  registered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  
  node_id BIGINT NOT NULL,
  name TEXT NOT NULL,
  date TIMESTAMP NOT NULL,
  
  UNIQUE(node_id, name)
);


--------------------------------------------------------------------------------
-- アカウント関連
--------------------------------------------------------------------------------
CREATE TABLE portus.customer (
  id BIGSERIAL PRIMARY KEY,
  registered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  parent_id BIGINT NOT NULL REFERENCES customer(id) ON DELETE CASCADE ON UPDATE CASCADE,

  name TEXT NOT NULL,
  valid BOOLEAN NOT NULL DEFAULT TRUE,
  quota BIGINT NOT NULL DEFAULT -1,
  role BIGINT NOT NULL DEFAULT 0,
  UNIQUE (name)
);

CREATE TABLE portus.domain (
  id BIGSERIAL PRIMARY KEY,
  registered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  customer_id BIGINT NOT NULL REFERENCES customer(id) ON DELETE CASCADE ON UPDATE CASCADE,

  name TEXT NOT NULL,
  valid BOOLEAN NOT NULL DEFAULT TRUE,
  quota BIGINT NOT NULL DEFAULT -1,
  allow_from TEXT,
  UNIQUE (name)
);

CREATE TABLE portus.account (
  id BIGSERIAL PRIMARY KEY,
  registered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  customer_id BIGINT NOT NULL REFERENCES customer(id) ON DELETE CASCADE ON UPDATE CASCADE,

  name TEXT NOT NULL,
  valid BOOLEAN NOT NULL DEFAULT TRUE,
  quota BIGINT NOT NULL DEFAULT -1,
  role INTEGER NOT NULL DEFAULT 0,
  password TEXT NOT NULL,

  homedir TEXT NOT NULL,
  UNIQUE (name)
);


--------------------------------------------------------------------------------
-- ファイル情報
--------------------------------------------------------------------------------
CREATE TABLE portus.file (
  id BIGSERIAL PRIMARY KEY,
  registered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  parent_id BIGINT NOT NULL REFERENCES file(id) ON DELETE CASCADE ON UPDATE CASCADE,
  domain_id BIGINT NOT NULL REFERENCES domain(id) ON DELETE CASCADE ON UPDATE CASCADE,

  directory BOOLEAN NOT NULL,
  name TEXT NOT NULL,
  size BIGINT NOT NULL,
  digest TEXT,

  published BOOLEAN NOT NULL DEFAULT TRUE,
  authorized BOOLEAN NOT NULL DEFAULT FALSE,
  open_date TIMESTAMP DEFAULT NULL,
  close_date TIMESTAMP DEFAULT NULL,
  replication INTEGER NOT NULL DEFAULT 0,
  referer TEXT DEFAULT NULL,

  -- for performance (following another attributes)
  full_path TEXT,
  actual_published BOOLEAN,
  actual_authorized BOOLEAN,
  actual_open_date TIMESTAMP,
  actual_close_date TIMESTAMP,
  actual_size BIGINT,

  file_type_id BIGINT NOT NULL DEFAULT 0,
);
CREATE INDEX file_parent_id_key ON file(parent_id);
CREATE INDEX file_domain_id_key ON file(domain_id);
CREATE INDEX file_name_key ON file(name text_pattern_ops);
CREATE INDEX file_full_path_key ON file(full_path text_pattern_ops);

CREATE TABLE portus.file_replication (
  id BIGINT PRIMARY KEY REFERENCES file(id) ON DELETE CASCADE ON UPDATE CASCADE,
  regulated INT NOT NULL DEFAULT 0,
  replicated INT NOT NULL DEFAULT 0,
  available INT NOT NULL DEFAULT 0
);

CREATE OR REPLACE FUNCTION portus.file_create_replication() RETURNS TRIGGER AS $$
BEGIN
  IF NOT NEW.directory THEN
    INSERT INTO portus.file_replication(id, regulated, available) VALUES(NEW.id, NEW.replication, 0);
  END IF;
  RETURN NULL;
END
$$ LANGUAGE plpgsql;
CREATE TRIGGER file_after_insert AFTER INSERT ON portus.file
  FOR EACH ROW EXECUTE PROCEDURE portus.file_create_replication();

CREATE OR REPLACE FUNCTION portus.file_fill_attribute() RETURNS TRIGGER AS $$
DECLARE
  selected_row file%ROWTYPE;
BEGIN
  NEW.actual_published = NEW.published;
  NEW.actual_authorized = NEW.authorized;
  NEW.actual_open_date = NEW.open_date;
  NEW.actual_close_date = NEW.close_date;
  IF NOT NEW.directory THEN
    NEW.actual_size = ceil(NEW.size / 4096.0) * 4096;
  END IF;

  IF NEW.parent_id = 0 THEN
    NEW.full_path := NEW.name;
  ELSE
    SELECT INTO selected_row * FROM portus.file WHERE id = NEW.parent_id;
    IF NOT FOUND THEN
      RETURN NULL;
    ELSE
      NEW.full_path := selected_row.full_path || '/' || NEW.name;

      IF NOT selected_row.actual_published THEN
          NEW.actual_published := FALSE;
      END IF;

      IF selected_row.actual_authorized THEN
        NEW.actual_authorized := TRUE;
      END IF;

      IF selected_row.actual_open_date IS NOT NULL THEN
        IF NEW.open_date IS NULL OR NEW.open_date < selected_row.actual_open_date THEN
          NEW.actual_open_date := selected_row.actual_open_date;
        END IF;
      END IF;

      IF selected_row.actual_close_date IS NOT NULL THEN
        IF NEW.close_date IS NULL OR NEW.close_date > selected_row.actual_close_date THEN
          NEW.actual_close_date := selected_row.actual_close_date;
        END IF;
      END IF;
    END IF;
  END IF;

  RETURN NEW;
END
$$ LANGUAGE plpgsql;
CREATE TRIGGER file_fill_attribute BEFORE INSERT OR UPDATE ON portus.file
  FOR EACH ROW EXECUTE PROCEDURE portus.file_fill_attribute();

CREATE OR REPLACE FUNCTION portus.file_attribute_cascade_update() RETURNS TRIGGER AS $$
BEGIN
  IF OLD.full_path <> NEW.full_path
      OR OLD.actual_published <> NEW.actual_published
      OR OLD.actual_authorized <> NEW.actual_authorized
      OR OLD.actual_open_date <> NEW.actual_open_date
      OR OLD.actual_close_date <> NEW.actual_close_date THEN
    UPDATE file SET name=name WHERE parent_id = NEW.id;
  END IF;
  IF OLD.replication <> NEW.replication THEN
    UPDATE portus.file_replication SET regulated=NEW.replication WHERE id = NEW.id AND regulated < NEW.replication;
  END IF;
  RETURN NULL;
END
$$ LANGUAGE plpgsql;
CREATE TRIGGER file_attribute_cascade_update AFTER UPDATE ON portus.file
  FOR EACH ROW EXECUTE PROCEDURE file_attribute_cascade_update();

CREATE OR REPLACE FUNCTION portus.file_size_update() RETURNS TRIGGER AS $$
BEGIN
  IF (TG_OP = 'INSERT') THEN
    UPDATE
      portus.file
    SET
      size=COALESCE((SELECT sum(size) FROM portus.file WHERE parent_id=NEW.parent_id), 0),
      actual_size=COALESCE((SELECT sum(actual_size) FROM portus.file WHERE parent_id=NEW.parent_id), 0)
    WHERE
      id=NEW.parent_id;
  ELSIF (TG_OP='UPDATE') THEN
    IF OLD.parent_id <> NEW.parent_id THEN
      UPDATE
        portus.file
      SET
        size=COALESCE((SELECT sum(size) FROM portus.file WHERE parent_id=OLD.parent_id), 0),
        actual_size=COALESCE((SELECT sum(actual_size) FROM portus.file WHERE parent_id=OLD.parent_id), 0)
      WHERE
        id=OLD.parent_id;
    END IF;
    IF OLD.id <> 0 THEN
      -- 自分自身が更新されたら親も更新する
      UPDATE
        portus.file
      SET
        size=COALESCE((SELECT sum(size) FROM portus.file WHERE parent_id=NEW.parent_id), 0),
        actual_size=COALESCE((SELECT sum(actual_size) FROM portus.file WHERE parent_id=NEW.parent_id), 0)
      WHERE
        id=NEW.parent_id;
    END IF;
  ELSIF (TG_OP='DELETE') THEN
    UPDATE
      portus.file
    SET
      size=COALESCE((SELECT sum(size) FROM portus.file WHERE parent_id=OLD.parent_id), 0),
      actual_size=COALESCE((SELECT sum(actual_size) FROM portus.file WHERE parent_id=OLD.parent_id), 0)
    WHERE
      id=OLD.parent_id;
  END IF;
  RETURN NULL;
END
$$ LANGUAGE plpgsql;
CREATE TRIGGER file_size_update AFTER INSERT OR UPDATE OR DELETE ON portus.file
  FOR EACH ROW EXECUTE PROCEDURE file_size_update();

--CREATE TABLE portus.upload (
--  id BIGSERIAL PRIMARY KEY,
--  registered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
--  
--  path TEXT NOT NULL,
--  size BIGINT NOT NULL,
--  username TEXT NOT NULL
--);


--------------------------------------------------------------------------------
-- アカウントマップ
--------------------------------------------------------------------------------
CREATE TABLE portus.account_root_map (
  id BIGSERIAL PRIMARY KEY,
  registered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  account_id BIGINT NOT NULL REFERENCES account(id) ON DELETE CASCADE ON UPDATE CASCADE,
  file_id BIGINT NOT NULL REFERENCES file(id) ON DELETE CASCADE ON UPDATE CASCADE,
  UNIQUE (account_id, file_id)
);


--------------------------------------------------------------------------------
-- レプリカ情報
--------------------------------------------------------------------------------
CREATE TABLE portus.storedinfo (
  id BIGSERIAL PRIMARY KEY,
  registered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  -- mapping
  file_id BIGINT NOT NULL REFERENCES file(id) ON DELETE CASCADE,
  node_id BIGINT NOT NULL REFERENCES node(id) ON DELETE CASCADE,
  from_node_id BIGINT NOT NULL DEFAULT -1,
  digest TEXT,
  hostname TEXT,
  
  UNIQUE (file_id, node_id)
);

CREATE OR REPLACE FUNCTION portus.storedinfo_available() RETURNS TRIGGER AS $$
BEGIN
  IF (TG_OP = 'DELETE') THEN
    UPDATE portus.file_replication SET replicated=replicated-1, available=available-1 WHERE id=OLD.file_id;
  ELSE
    UPDATE portus.file_replication SET replicated=replicated+1, available=available+1 WHERE id=NEW.file_id;
  END IF;
  RETURN NULL;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER storedinfo_after_update AFTER INSERT OR DELETE ON portus.storedinfo
  FOR EACH ROW EXECUTE PROCEDURE storedinfo_available();


--------------------------------------------------------------------------------
-- アクセスカウント
--------------------------------------------------------------------------------
CREATE TABLE portus.access_count (
  id BIGSERIAL PRIMARY KEY,
  date TIMESTAMP NOT NULL,
  code INT NOT NULL,
  method VARCHAR(8) NOT NULL,
  full_path TEXT NOT NULL,
  domain_id BIGINT NOT NULL,
  count BIGINT NOT NULL,
  transfer BIGINT NOT NULL,
  
  UNIQUE(date, code, method, full_path)
);
CREATE INDEX access_count_full_path_key ON portus.access_count(full_path text_pattern_ops);

CREATE TABLE portus.node_count (
  id BIGSERIAL PRIMARY KEY,
  date TIMESTAMP NOT NULL,
  node_id BIGINT NOT NULL,
  count BIGINT NOT NULL,
  transfer BIGINT NOT NULL,
  
  UNIQUE(date, node_id)
);
CREATE INDEX node_count_node_id_key ON portus.node_count(node_id);


--------------------------------------------------------------------------------
-- 削除情報
--------------------------------------------------------------------------------
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

CREATE TABLE deleted.storedinfo (
  id BIGINT PRIMARY KEY,
  registered TIMESTAMP NOT NULL,
  updated TIMESTAMP NOT NULL,
  deleted TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  file_id BIGINT NOT NULL,
  node_id BIGINT NOT NULL
);

CREATE OR REPLACE FUNCTION portus.storedinfo_log_delete() RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO deleted.storedinfo(id, registered, updated, file_id, node_id)
    VALUES(OLD.id, OLD.registered, OLD.updated, OLD.file_id, OLD.node_id);
  RETURN NULL;
END
$$ LANGUAGE plpgsql;
CREATE TRIGGER storedinfo_log_delete AFTER DELETE ON portus.storedinfo
  FOR EACH ROW EXECUTE PROCEDURE portus.storedinfo_log_delete();


--------------------------------------------------------------------------------
-- モニタ情報
--------------------------------------------------------------------------------
CREATE SCHEMA system;

CREATE TABLE system.monitor (
  time BIGINT NOT NULL,
  host VARCHAR(64) NOT NULL,
  title VARCHAR(64) NOT NULL,
  level INTEGER NOT NULL,
  value DOUBLE PRECISION NOT NULL
);
CREATE INDEX monitor_time_key ON system.monitor (time);
CREATE INDEX monitor_key_key ON system.monitor (host, title, level);

--------------------------------------------------------------------------------
-- 初期化
--------------------------------------------------------------------------------
INSERT INTO portus.node_block(id, name) VALUES(0, 'default');
INSERT INTO portus.node_type(id, name) VALUES(0, 'default');

INSERT INTO customer(id, parent_id, name)
  VALUES(0, 0, 'portus');
INSERT INTO domain(id, customer_id, name)
  VALUES(0, 0, 'portus.karatachi.org');
INSERT INTO account(id, customer_id, name, role, password, homedir)
  VALUES(0, 0, 'root', -1, '{sha1}Tj2/3aUoSG6dw5Kp+Ezfj5Hf2jc=', '/portus/upload');
INSERT INTO file(id, parent_id, domain_id, directory, name, size)
  VALUES(0, 0, 0, true, 'root', 0);

INSERT INTO network(carrier, ip_address) VALUES('docomo', '210.153.84.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '210.136.161.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '210.153.86.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '124.146.174.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '124.146.175.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '202.229.176.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '202.229.177.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '202.229.178.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '202.229.179.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '111.89.188.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '111.89.189.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '111.89.190.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '111.89.191.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '210.153.87.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '203.138.180.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '203.138.181.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '203.138.203.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '210.153.84.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '210.136.161.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '210.153.86.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '124.146.174.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '124.146.175.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '202.229.176.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '202.229.177.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '202.229.178.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '202.229.179.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '111.89.188.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '111.89.189.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '111.89.190.0/24');
INSERT INTO network(carrier, ip_address) VALUES('docomo', '111.89.191.0/24');
INSERT INTO network(carrier, ip_address) VALUES('softbank', '123.108.237.0/27');
INSERT INTO network(carrier, ip_address) VALUES('softbank', '202.253.96.224/27');
INSERT INTO network(carrier, ip_address) VALUES('softbank', '210.146.7.192/26');
INSERT INTO network(carrier, ip_address) VALUES('softbank', '210.175.1.128/25');
INSERT INTO network(carrier, ip_address) VALUES('softbank', '123.108.237.224/27');
INSERT INTO network(carrier, ip_address) VALUES('softbank', '202.253.96.0/27');
INSERT INTO network(carrier, ip_address) VALUES('softbank', '123.108.236.0/24');
INSERT INTO network(carrier, ip_address) VALUES('softbank', '202.179.203.0/24');
INSERT INTO network(carrier, ip_address) VALUES('softbank', '202.179.204.0/24');
INSERT INTO network(carrier, ip_address) VALUES('softbank', '210.146.60.128/25');
INSERT INTO network(carrier, ip_address) VALUES('softbank', '210.169.176.0/24');
INSERT INTO network(carrier, ip_address) VALUES('softbank', '210.175.1.128/25');
INSERT INTO network(carrier, ip_address) VALUES('softbank', '123.108.239.0/24');
INSERT INTO network(carrier, ip_address) VALUES('au', '210.230.128.224/28');
INSERT INTO network(carrier, ip_address) VALUES('au', '121.111.227.160/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '61.117.1.0/28');
INSERT INTO network(carrier, ip_address) VALUES('au', '219.108.158.0/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '219.125.146.0/28');
INSERT INTO network(carrier, ip_address) VALUES('au', '61.117.2.32/29');
INSERT INTO network(carrier, ip_address) VALUES('au', '61.117.2.40/29');
INSERT INTO network(carrier, ip_address) VALUES('au', '219.108.158.40/29');
INSERT INTO network(carrier, ip_address) VALUES('au', '219.125.148.0/25');
INSERT INTO network(carrier, ip_address) VALUES('au', '222.5.63.0/25');
INSERT INTO network(carrier, ip_address) VALUES('au', '222.5.63.128/25');
INSERT INTO network(carrier, ip_address) VALUES('au', '222.5.62.128/25');
INSERT INTO network(carrier, ip_address) VALUES('au', '59.135.38.128/25');
INSERT INTO network(carrier, ip_address) VALUES('au', '219.108.157.0/25');
INSERT INTO network(carrier, ip_address) VALUES('au', '219.125.145.0/25');
INSERT INTO network(carrier, ip_address) VALUES('au', '121.111.231.0/25');
INSERT INTO network(carrier, ip_address) VALUES('au', '121.111.227.0/25');
INSERT INTO network(carrier, ip_address) VALUES('au', '118.152.214.192/26');
INSERT INTO network(carrier, ip_address) VALUES('au', '118.159.131.0/25');
INSERT INTO network(carrier, ip_address) VALUES('au', '118.159.133.0/25');
INSERT INTO network(carrier, ip_address) VALUES('au', '118.159.132.160/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.142.0/26');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.141.64/26');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.141.128/26');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.141.192/26');
INSERT INTO network(carrier, ip_address) VALUES('au', '118.159.133.192/26');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.143.192/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.143.224/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.147.0/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.142.128/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.142.160/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.142.192/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.142.224/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.143.0/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.143.32/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.147.32/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.147.64/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.147.96/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.147.128/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.147.160/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.147.192/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.147.224/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '222.15.68.192/26');
INSERT INTO network(carrier, ip_address) VALUES('au', '59.135.39.128/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '118.152.214.160/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '118.152.214.128/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '222.1.136.96/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '222.1.136.64/27');
INSERT INTO network(carrier, ip_address) VALUES('au', '59.128.128.0/20');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.140.40/30');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.140.44/30');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.140.48/30');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.140.52/30');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.140.56/30');
INSERT INTO network(carrier, ip_address) VALUES('au', '111.86.140.60/30');
