-- ファイル別アクセス数転送量順（全期間）
SELECT
  domain_id, full_path, sum(count), sum(transfer)
FROM
  portus.access_count
WHERE
  code=200 AND method='GET'
GROUP BY
  domain_id, full_path
ORDER BY
  sum(transfer) DESC
LIMIT 50;
