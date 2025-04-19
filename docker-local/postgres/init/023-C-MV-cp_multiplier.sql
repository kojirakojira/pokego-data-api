/**
 * CP Multiplierの本表
 */
CREATE MATERIALIZED VIEW cp_multiplier AS
  SELECT 
    trim(to_char(cpmo1.pl, '09.9'))::bpchar(4) AS pl,
    calc_cpm(cpmo1.pl, cpmo1.multiplier, cpmo2.multiplier, cpmo3.multiplier)::double precision AS multiplier
    FROM cp_multiplier_original cpmo1
    LEFT JOIN cp_multiplier_original cpmo2
      ON MOD(cpmo1.pl, 1) > 0
      AND (cpmo1.pl - 0.5) = cpmo2.pl
    LEFT JOIN cp_multiplier_original cpmo3
      ON MOD(cpmo1.pl, 1) > 0
      AND (cpmo1.pl + 0.5) = cpmo3.pl;

/**
 * CREATE INDEX
 */
CREATE INDEX IF NOT EXISTS cp_multiplier_pk ON cp_multiplier (pl);

COMMIT;
