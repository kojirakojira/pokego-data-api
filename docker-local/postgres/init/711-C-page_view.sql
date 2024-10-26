CREATE SEQUENCE IF NOT EXISTS page_view_seq;

CREATE TABLE IF NOT EXISTS page_view (
  page_view_id integer DEFAULT nextval('page_view_seq') NOT NULL,
  page varchar(30) NOT NULL,
  ymd timestamp NOT NULL,
  view_count integer NOT NULL,
  PRIMARY KEY(page_view_id)
);

CREATE INDEX IF NOT EXISTS page_view_idx ON page_view (page, ymd);
