DROP TABLE IF EXISTS cp_multiplier_original;

/* CP Multiplierの"原表"
 * PLは0.5刻みだが、xx.5の値は算出して求める仕組みになっている。
 * このテーブルでは、xx.5を含まない表を扱う。
 */
CREATE TABLE cp_multiplier_original (
  pl numeric,
  multiplier real NOT NULL, -- float型(32ビット)
  CONSTRAINT cp_multiplier_original_pk PRIMARY KEY(pl)
);
