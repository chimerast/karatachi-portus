package org.karatachi.portus.manage.daemon;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.karatachi.daemon.Daemon;
import org.karatachi.db.ConnectionManager;
import org.karatachi.db.DataSourceManager;
import org.karatachi.translator.IntervalTranslator;

public class SystemMaintenanceDaemonGroup {
    public static class SystemUpdateDaemon extends Daemon {
        public SystemUpdateDaemon() {
            super("SystemUpdate");
        }

        @Override
        protected void updateNextRun() {
            setNextRun(getLastStarted() + IntervalTranslator.min(5));
        }

        @Override
        protected void work() throws Exception {
            ConnectionManager conn =
                    DataSourceManager.getMasterConnectionManager();
            try {
                int update;
                {
                    Statement stmt = conn.createStatement();
                    ResultSet rs =
                            stmt.executeQuery("SELECT count(*) FROM node WHERE status=1");
                    if (!rs.next()) {
                        return;
                    }

                    int available = rs.getInt(1);
                    if (!(available > 280)) {
                        return;
                    }
                    update = available - 280;
                }

                ArrayList<Integer> ids = new ArrayList<Integer>();
                {
                    PreparedStatement stmt =
                            conn.prepareStatement("SELECT id FROM node WHERE status <> -1 AND update <> 1 LIMIT ?");
                    stmt.setInt(1, update);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        ids.add(rs.getInt(1));
                    }
                }

                {
                    PreparedStatement stmt =
                            conn.prepareStatement("UPDATE node SET status=4, update=1 WHERE id = ?");
                    for (int id : ids) {
                        stmt.setInt(1, id);
                        stmt.executeUpdate();
                    }
                }

                {
                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate("DELETE FROM storedinfo "
                            + "WHERE id IN (SELECT storedinfo.id FROM file JOIN storedinfo ON file.id = storedinfo.file_id WHERE file.digest <> storedinfo.digest AND file.digest <> 'calculating')");
                }
            } catch (SQLException e) {
                logger.error("システムアップデートエラー", e);
            } finally {
                conn.dispose();
            }
        }
    }
}
