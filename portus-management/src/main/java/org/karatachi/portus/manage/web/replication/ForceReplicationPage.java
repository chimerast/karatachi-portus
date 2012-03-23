package org.karatachi.portus.manage.web.replication;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.karatachi.daemon.DaemonManager;
import org.karatachi.portus.core.entity.File;
import org.karatachi.portus.core.logic.FileAccessLogic;
import org.karatachi.portus.manage.daemon.ForceReplicationDaemonGroup;
import org.karatachi.portus.manage.web.PortusBasePage;
import org.seasar.framework.container.annotation.tiger.Binding;

public class ForceReplicationPage extends PortusBasePage {
    private static final long serialVersionUID = 1L;

    @Binding
    private FileAccessLogic fileAccessLogic;

    public ForceReplicationPage() {
        add(new ForceReplicationForm("form"));
        add(new ReplicationQueuePanel("queue",
                ForceReplicationDaemonGroup.class));
    }

    private class ForceReplicationForm extends Form<ForceReplicationForm> {
        private static final long serialVersionUID = 1L;

        private long fileId;
        private int count;

        public ForceReplicationForm(String id) {
            super(id);
            setModel(new CompoundPropertyModel<ForceReplicationForm>(this));

            add(new TextField<Long>("fileId"));
            add(new TextField<Integer>("count"));

            add(new SubmitLink("add") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onSubmit() {
                    if (!(count > 0 && count < 100)) {
                        error("追加数が多すぎるか０です少なすぎます。");
                    }

                    File file = new File();
                    file.id = fileId;
                    file = fileAccessLogic.getFile(file);

                    if (file == null) {
                        error("ファイルIDに対応するファイルがありません。");
                    } else if (file.directory) {
                        error("ファイルIDに対応するファイルはディレクトリです。");
                    }

                    if (hasError()) {
                        return;
                    }

                    ForceReplicationDaemonGroup daemonGroup =
                            DaemonManager.getDaemonGroup(ForceReplicationDaemonGroup.class);
                    daemonGroup.addTask(fileId, count);
                }
            });

            add(new SubmitLink("clear") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onSubmit() {
                    ForceReplicationDaemonGroup daemonGroup =
                            DaemonManager.getDaemonGroup(ForceReplicationDaemonGroup.class);
                    daemonGroup.clearTask();
                }
            });
        }
    }
}
