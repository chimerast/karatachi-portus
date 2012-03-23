-- ドメイン別アクセス数転送量
SELECT customer.name, domain.name, access.* FROM
  (
    SELECT
      domain_id, date_trunc('day', date)::date, sum(count), sum(transfer)
    FROM
      portus.access_count
    WHERE
      code=200 AND method='GET'
    GROUP BY
      domain_id, date_trunc('day', date)
    ORDER BY
      domain_id, date_trunc('day', date)
  ) access
  JOIN domain ON access.domain_id=domain.id
  JOIN customer ON domain.customer_id=customer.id;
