package org.karatachi.portus.admin.web.file;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;
import org.karatachi.portus.admin.web.PortusBasePage;
import org.karatachi.portus.admin.web.file.component.PortusDirChoice;
import org.karatachi.portus.common.net.AuthorizationInfo;
import org.karatachi.portus.core.AssemblyInfo;
import org.karatachi.portus.core.PortusRuntimeException;
import org.karatachi.portus.core.dto.AccountDto;
import org.karatachi.portus.core.entity.File;
import org.karatachi.portus.core.entity.FileReplication;
import org.karatachi.portus.core.logic.FileAccessLogic;
import org.karatachi.portus.webbase.web.utils.TrueFalseImage;
import org.karatachi.wicket.auto.AutoResolveListView;
import org.karatachi.wicket.auto.SelfResolveForm;
import org.karatachi.wicket.converter.DateTimeConverter;
import org.karatachi.wicket.dialog.InputDialogParams;
import org.karatachi.wicket.dialog.MessageDialogParams;
import org.seasar.framework.container.annotation.tiger.Binding;

public class FileListPage extends PortusBasePage {
    private static final long serialVersionUID = 1L;

    @Binding
    private FileAccessLogic fileAccessLogic;

    @Binding
    private AccountDto accountDto;

    public FileListPage() {
        add(new FileListForm("form"));
    }

    private class FileListForm extends SelfResolveForm {
        private static final long serialVersionUID = 1L;

        private File portusDir;

        private List<File> files;
        private boolean[] checks;

        public FileListForm(String id) {
            super(id);
            setOutputMarkupId(true);
            setFormComponentCustomizer(null);

            this.portusDir = fileAccessLogic.getAccountRoot();

            if (this.portusDir == null) {
                error("操作可能なドメインがありません。");
                setVisible(false);
                return;
            }

            reload(null);

            add(new PortusDirChoice("portusDir").add(new AjaxFormComponentUpdatingBehavior(
                    "onchange") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    reload(target);
                }
            }));

            add(new AutoResolveListView<File>("files") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void populateItem(ListItem<File> item) {
                    final File file = item.getModelObject();
                    item.add(new CheckBox("checked",
                            new PropertyModel<Boolean>(FileListForm.this,
                                    "checks." + item.getIndex())));
                    item.add(new AjaxLink<Void>("nameLink") {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            portusDir = file;
                            reload(target);
                        }
                    }.setEnabled(file.directory));
                    item.add(new ExternalLink("previewLink", "http://"
                            + file.fullPath).setVisible(!file.directory));
                    item.add(new Link<Void>("authorizedLink") {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void onClick() {
                            String url = "http://" + file.fullPath;
                            AuthorizationInfo authinfo =
                                    new AuthorizationInfo(
                                            accountDto.getCustomerCode(), url,
                                            null, DateUtils.addMinutes(
                                                    new Date(), 5), null);
                            String authurl = url + "?code=" + authinfo;
                            getRequestCycle().setRequestTarget(
                                    new RedirectRequestTarget(authurl));
                        }
                    }.setVisible(!file.directory && file.actualAuthorized));

                    item.add(createStatusLabel("status", file,
                            fileAccessLogic.getReplication(file)));
                    item.add(new TrueFalseImage("published"));
                    item.add(new TrueFalseImage("authorized"));
                    item.add(new TrueFalseImage("streaming",
                            file.fileTypeId == File.TYPE_FLASH_STREAMING));
                }
            });

            add(new AjaxSubmitLink("upDir") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    File parent = fileAccessLogic.getParentDir(portusDir);
                    if (parent != null) {
                        portusDir = parent;
                        reload(target);
                    }
                }
            });
            add(new AjaxSubmitLink("createDir") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    input.show(target,
                            new InputDialogParams("フォルダの名前を入力して下さい") {
                                private static final long serialVersionUID = 1L;

                                @Override
                                public void onSuccess(AjaxRequestTarget target) {
                                    try {
                                        fileAccessLogic.createDirTx(portusDir,
                                                getInput());
                                    } catch (Exception e) {
                                        logger.error("フォルダの作成に失敗", e);
                                        error("フォルダの作成に失敗しました。フォルダ名に使用できない文字があるか、権限がありません。");
                                    }
                                    reload(target);
                                }
                            });
                }
            });
            add(new AjaxSubmitLink("renameFile") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    input.show(target, new InputDialogParams(
                            "変更するファイル名を入力してください。") {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void onSuccess(AjaxRequestTarget target) {
                            try {
                                for (File file : getSelectedFiles()) {
                                    file.name = getInput();
                                    fileAccessLogic.updateFileTx(file);
                                }
                            } catch (PortusRuntimeException e) {
                                error(e.getMessage());
                            }
                            reload(target);
                        }
                    });
                }
            });
            add(new AjaxSubmitLink("removeFile") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    confirm.show(target,
                            new MessageDialogParams("ファイルを削除しますか？") {
                                private static final long serialVersionUID = 1L;

                                @Override
                                public void onSuccess(AjaxRequestTarget target) {
                                    for (File file : getSelectedFiles()) {
                                        fileAccessLogic.removeFileTx(file);
                                    }
                                    reload(target);
                                }
                            });
                }
            });

            add(new UpdateAttributeLink("publishFile", "published", true));
            add(new UpdateAttributeLink("unpublishFile", "published", false));
            add(new AjaxSubmitLink("setFileOpenDate") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    input.show(
                            target,
                            new DateInputDialogParams(
                                    "公開開始日時を入力してください。設定を解除する場合は空白を入力してください。(書式: 2007/01/01 00:00)") {
                                private static final long serialVersionUID = 1L;

                                @Override
                                public void update(File file, Date date) {
                                    file.openDate = date;
                                }
                            });
                }
            });
            add(new AjaxSubmitLink("setFileCloseDate") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    input.show(
                            target,
                            new DateInputDialogParams(
                                    "公開終了日時を入力してください。設定を解除する場合は空白を入力してください。(書式: 2007/01/01 00:00)") {
                                private static final long serialVersionUID = 1L;

                                @Override
                                public void update(File file, Date date) {
                                    file.closeDate = date;
                                }
                            });
                }
            });

            add(new UpdateAttributeLink("authorizeFile", "authorized", true));
            add(new UpdateAttributeLink("unauthorizeFile", "authorized", false));
            add(new UpdateStreamingLink("streamingFile", true));
            add(new UpdateStreamingLink("normalFile", false));
        }

        private class UpdateAttributeLink extends AjaxSubmitLink {
            private static final long serialVersionUID = 1L;

            private final String fieldName;
            private final Object value;

            public UpdateAttributeLink(String id, String fieldName, Object value) {
                super(id);
                this.fieldName = fieldName;
                this.value = value;
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                for (File file : getSelectedFiles()) {
                    fileAccessLogic.setAttributeRecursive(file, fieldName,
                            value);
                }
                reload(target);
            }
        }

        private class UpdateStreamingLink extends AjaxSubmitLink {
            private static final long serialVersionUID = 1L;

            private final boolean value;

            public UpdateStreamingLink(String id, boolean value) {
                super(id);
                this.value = value;
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                for (File file : getSelectedFiles()) {
                    fileAccessLogic.setStreaming(file, value);
                }
                reload(target);
            }
        }

        private abstract class DateInputDialogParams extends InputDialogParams {
            private static final long serialVersionUID = 1L;

            public DateInputDialogParams(String message) {
                super(message);
            }

            public abstract void update(File file, Date date);

            @Override
            public void onSuccess(AjaxRequestTarget target) {
                target.addComponent(feedback);
                Date date;
                if (StringUtils.isNotEmpty(getInput())) {
                    DateTimeConverter converter =
                            new DateTimeConverter(DateFormat.MEDIUM,
                                    DateFormat.SHORT);
                    try {
                        date =
                                (Date) converter.convertToObject(getInput(),
                                        getLocale());
                    } catch (Exception e) {
                        error("書式が違います");
                        return;
                    }
                } else {
                    date = null;
                }
                for (File file : getSelectedFiles()) {
                    update(file, date);
                    fileAccessLogic.updateFileTx(file);
                }
                reload(target);
            }
        }

        private void reload(AjaxRequestTarget target) {
            files = fileAccessLogic.getFiles(portusDir);
            checks = new boolean[files.size()];
            if (target != null) {
                target.addComponent(FileListForm.this);
                target.addComponent(feedback);
            }
        }

        private List<File> getSelectedFiles() {
            List<File> ret = new ArrayList<File>();
            for (int i = 0; i < checks.length; ++i) {
                if (checks[i]) {
                    ret.add(files.get(i));
                }
            }
            return ret;
        }

        protected Component createStatusLabel(String id, File file,
                FileReplication status) {
            String distributionStatus, distributionClass;

            if (!file.directory) {
                if (status.available < AssemblyInfo.REPLICATION_MINIMAM) {
                    distributionStatus = "分散中";
                    distributionClass = "status_stop";
                } else if (status.available < AssemblyInfo.REPLICATION_THRESHOLD) {
                    distributionStatus = "公開可能";
                    distributionClass = "status_executing";
                } else {
                    distributionStatus = "分散完了";
                    distributionClass = "status_complete";
                }
            } else {
                distributionStatus = "";
                distributionClass = "status_complete";
            }

            String label, style;
            Date now = new Date();
            if (!file.actualPublished) {
                label = String.format("非公開 %s", distributionStatus);
                style = "status_cancel";
            } else if ((file.actualOpenDate != null && file.actualOpenDate.getTime() > now.getTime())
                    || (file.actualCloseDate != null && file.actualCloseDate.getTime() <= now.getTime())) {
                label = String.format("非公開期間 %s", distributionStatus);
                style = "status_cancel";
            } else if (file.actualAuthorized) {
                label = String.format("認証付公開 %s", distributionStatus);
                style = distributionClass;
            } else {
                label = String.format("公開 %s", distributionStatus);
                style = distributionClass;
            }
            return new Label(id, label).add(new AttributeAppender("class",
                    new Model<String>(style), " "));
        }
    }

    @Override
    protected String getPageTitle() {
        return "ファイル管理";
    }
}
