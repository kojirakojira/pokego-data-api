DROP TABLE IF EXISTS citations;

CREATE TABLE citations (
  page_id varchar(20),
  display_order int,
  author varchar(64),
  title varchar(128),
  url varchar(256) not null,
  CONSTRAINT citations_pk PRIMARY KEY(page_id, display_order)
);

COMMIT;