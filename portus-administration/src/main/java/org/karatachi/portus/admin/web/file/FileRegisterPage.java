package org.karatachi.portus.admin.web.file;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.PropertyModel;
import org.karatachi.portus.admin.web.PortusBasePage;
import org.karatachi.portus.admin.web.file.component.LocalDirChoice;
import org.karatachi.portus.admin.web.file.component.LocalFileListChoice;
import org.karatachi.portus.admin.web.file.component.PortusDirChoice;
import org.karatachi.portus.admin.web.file.component.PortusFileListChoice;
import org.karatachi.portus.core.entity.File;
import org.karatachi.portus.core.entity.LocalFile;
import org.karatachi.portus.core.logic.FileAccessLogic;
import org.karatachi.portus.core.logic.LocalFileAccessLogic;
import org.karatachi.wicket.auto.SelfResolveForm;
import org.karatachi.wicket.dialog.InputDialogParams;
import org.karatachi.wicket.dialog.MessageDialogParams;
import org.seasar.framework.container.annotation.tiger.Binding;

public class FileRegisterPage extends PortusBasePage {
    private static final long serialVersionUID = 1L;

    @Binding
    private FileAccessLogic fileAccessLogic;
    @Binding
    private LocalFileAccessLogic localFileAccessLogic;

    public FileRegisterPage() {
        add(new FileRegisterForm("form"));
    }

    private class FileRegisterForm extends SelfResolveForm {
        private static final long serialVersionUID = 1L;

        private File portusDir;
        private LocalFile localDir;

        private List<File> portusFileList;
        private List<LocalFile> localFileList;

        public FileRegisterForm(String id) {
            super(id);
            setOutputMarkupId(true);
            setFormComponentCustomizer(null);

            this.portusDir = fileAccessLogic.getAccountRoot();
            this.localDir = localFileAccessLogic.getAccountRoot();
            this.portusFileList = new ArrayList<File>();
            this.localFileList = new ArrayList<LocalFile>();

            if (this.portusDir == null) {
                error("操作可能なドメインがありません。");
                setVisible(false);
                return;
            }

            add(new PortusDirChoice("portusDir").add(new AjaxFormComponentUpdatingBehavior(
                    "onchange") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    reload(target);
                }
            }));
            add(new PortusFileListChoice("portusFileList",
                    new PropertyModel<File>(this, "portusDir")).add(new AjaxFormComponentUpdatingBehavior(
                    "ondblclick") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    PortusFileListChoice choice =
                            (PortusFileListChoice) getComponent();
                    if (choice.getModelObject().size() == 0) {
                        return;
                    }
                    File selected = choice.getModelObject().iterator().next();
                    if (selected.directory) {
                        portusDir = selected;
                        reload(target);
                    }
                }
            }));
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
                                        error("フォルダの作成に失敗しました。");
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
                                    for (File file : portusFileList) {
                                        fileAccessLogic.removeFileTx(file);
                                    }
                                    reload(target);
                                }
                            });
                }
            });

            add(new LocalDirChoice("localDir").add(new AjaxFormComponentUpdatingBehavior(
                    "onchange") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    reload(target);
                }
            }));
            add(new LocalFileListChoice("localFileList",
                    new PropertyModel<LocalFile>(this, "localDir")).add(new AjaxFormComponentUpdatingBehavior(
                    "ondblclick") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    LocalFileListChoice choice =
                            (LocalFileListChoice) getComponent();
                    if (choice.getModelObject().size() == 0) {
                        return;
                    }
                    LocalFile selected =
                            choice.getModelObject().iterator().next();
                    if (selected.file.isDirectory()) {
                        localDir = selected;
                        reload(target);
                    }
                }
            }));
            add(new AjaxLink<Void>("upLocalDir") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    LocalFile parent =
                            localFileAccessLogic.getParentDir(localDir);
                    if (parent != null) {
                        localDir = parent;
                        reload(target);
                    }
                }
            });

            add(new AjaxButton("reloadView") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    info("再読み込みしました");
                    reload(target);
                }
            });
            add(new AjaxButton("registerFile") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    try {
                        for (LocalFile localFile : localFileList) {
                            fileAccessLogic.registerFileTx(portusDir, localFile);
                        }
                        info("ファイルを登録しました。");
                    } catch (Exception e) {
                        logger.error("ファイルの登録に失敗", e);
                        error("一部ファイルの登録に失敗しました。");
                        error(e.getMessage());
                    }
                    reload(target);
                }
            });
        }

        private void reload(AjaxRequestTarget target) {
            target.addComponent(FileRegisterForm.this);
            target.addComponent(feedback);
        }
    }

    @Override
    protected String getPageTitle() {
        return "ファイル登録";
    }
}
