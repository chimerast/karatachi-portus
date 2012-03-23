SELECT
    date_trunc('day', date), lpad(upper(to_hex(node_id)),12,'0'), sum(count), sum(transfer)
  FROM
    portus.node_count
  GROUP BY
    node_id, date_trunc('day', date)
  ORDER BY
    date_trunc('day', date), node_id;
