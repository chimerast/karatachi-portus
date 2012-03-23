package org.karatachi.portus.manage.node.task;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.karatachi.net.shell.CommandHandler;
import org.karatachi.portus.core.dto.NodeFileDto;
import org.karatachi.portus.manage.logic.FileConsistencyLogic;
import org.karatachi.portus.manage.node.NodeTask;
import org.seasar.framework.container.annotation.tiger.Binding;

public class NodeConsistencyTask extends NodeTask {
    @Binding
    private FileConsistencyLogic fileConsistencyLogic;

    @Override
    public void execute(CommandHandler handler) throws IOException {
        for (int i = 0; i < 0x10000; ++i) {
            String prefix = String.format("%04x", i);
            checkConsistency(handler, prefix);
        }
    }

    private void checkConsistency(CommandHandler handler, final String prefix)
            throws IOException {
        logger.debug("比較開始: {} '{}'", getNode().id, prefix);
        keepalive(handler, true);

        List<NodeFileDto> chassisFile =
                fileConsistencyLogic.readNodeFile(handler.executeCommand("ls "
                        + prefix));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<List<NodeFileDto>> future =
                executor.submit(new Callable<List<NodeFileDto>>() {
                    @Override
                    public List<NodeFileDto> call() throws Exception {
                        return fileConsistencyLogic.getNodeFileWithPrefix(
                                getNode().id, prefix);
                    }
                });

        List<NodeFileDto> databaseFile = null;
        while (true) {
            try {
                databaseFile = future.get(1, TimeUnit.SECONDS);
                break;
            } catch (TimeoutException e) {
                keepalive(handler, false);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        executor.shutdown();

        if (chassisFile == null || databaseFile == null) {
            return;
        }

        Iterator<NodeFileDto> chassisItr = chassisFile.iterator();
        Iterator<NodeFileDto> databaseItr = databaseFile.iterator();

        NodeFileDto cf = null;
        NodeFileDto df = null;

        while (chassisItr.hasNext() && databaseItr.hasNext()) {
            if (cf == null) {
                cf = chassisItr.next();
            }
            if (df == null) {
                df = databaseItr.next();
            }

            int cmp = cf.getFilename().compareTo(df.getFilename());
            if (cmp < 0) {
                // cfの方が小さいのでcfを消す
                deleteFromNode(handler, cf);
                cf = null;
            } else if (cmp > 0) {
                // dfの方が小さいのでdfを消す
                deleteFromDatabase(df);
                df = null;
            } else {
                if (cf.getSize() != df.getSize()) {
                    // サイズが違ったら双方とも削除
                    deleteFromNode(handler, cf);
                    deleteFromDatabase(df);
                }
                // 両方進める
                cf = null;
                df = null;
            }

            keepalive(handler, false);
        }

        if (cf != null) {
            deleteFromNode(handler, cf);
        }

        if (df != null) {
            deleteFromDatabase(df);
        }

        while (chassisItr.hasNext()) {
            deleteFromNode(handler, chassisItr.next());
        }

        while (databaseItr.hasNext()) {
            deleteFromDatabase(databaseItr.next());
        }
    }

    @Override
    public boolean isSupported(int revision) {
        return revision >= 9;
    }

    private boolean deleteFromNode(CommandHandler handler, NodeFileDto cf)
            throws IOException {
        logger.debug("ノードより削除: {} {}", getNode().id, cf.getFilename());
        return fileConsistencyLogic.deleteFromNode(handler.executeCommand("rm "
                + cf.getFilename()));
    }

    private boolean deleteFromDatabase(NodeFileDto df) throws IOException {
        logger.debug("DBより削除: {} {}", getNode().id, df.getFilename());
        return fileConsistencyLogic.deleteFromDatabase(getNode().id,
                df.getFileId());
    }

    private long prevKeepalive;

    private void keepalive(CommandHandler handler, boolean force)
            throws IOException {
        if (!force && System.currentTimeMillis() < prevKeepalive + 10000) {
            return;
        }
        handler.executeCommand("version");
        prevKeepalive = System.currentTimeMillis();
        logger.debug("キープアライブ: {}", getNode().id);
    }
}
