SELECT
  node.*
FROM
  node JOIN storedinfo ON node.id=storedinfo.node_id
WHERE
  status=1 AND file_id=/*file_id*/
ORDER BY
  random()
LIMIT
  1
