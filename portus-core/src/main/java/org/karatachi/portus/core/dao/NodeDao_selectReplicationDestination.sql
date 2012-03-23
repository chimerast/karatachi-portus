SELECT
  node.*
FROM
  node
WHERE
  status = 1
    AND node_block_id NOT IN (SELECT node_block_id FROM node JOIN storedinfo ON node.id = storedinfo.node_id WHERE file_id=/*file_id*/)
    AND (SELECT count(*) FROM storedinfo WHERE node_id=node.id AND file_id=/*file_id*/) = 0
ORDER BY
  random()
LIMIT
  1
