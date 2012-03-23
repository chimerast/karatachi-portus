package org.karatachi.portus.admin.web.file;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.protocol.http.WebResponse;
import org.karatachi.portus.admin.web.PortusBasePage;
import org.karatachi.portus.admin.web.file.component.PortusDirChoice;
import org.karatachi.portus.batch.logic.PickUpAccessLogLogic;
import org.karatachi.portus.core.dao.AccessCountDao;
import org.karatachi.portus.core.dto.AccessCountDto;
import org.karatachi.portus.core.entity.File;
import org.karatachi.portus.core.logic.FileAccessLogic;
import org.karatachi.wicket.auto.AutoResolveListView;
import org.karatachi.wicket.auto.SelfResolveForm;
import org.karatachi.wicket.label.FormattedLabel;
import org.seasar.framework.container.annotation.tiger.Binding;

public class AccessCountPage extends PortusBasePage {
    private static final long serialVersionUID = 1L;

    @Binding
    private AccessCountDao accessCountDao;
    @Binding
    private FileAccessLogic fileAccessLogic;
    @Binding
    private PickUpAccessLogLogic pickUpAccessLogLogic;

    public AccessCountPage() {
        add(new AccessCountForm("form"));
    }

    private class AccessCountForm extends SelfResolveForm {
        private static final long serialVersionUID = 1L;

        private Date date;
        private File portusDir;

        private List<File> files;

        public AccessCountForm(String id) {
            super(id);
            setOutputMarkupId(true);
            setFormComponentCustomizer(null);

            this.date = new Date();
            this.portusDir = fileAccessLogic.getAccountRoot();
            reload(null);

            add(new DateTextField("date", "yyyy/MM/dd").add(new DatePicker()).add(
                    new AjaxFormComponentUpdatingBehavior("onchange") {
                        private static final long serialVersionUID = 1L;

                        @Override
                        protected void onUpdate(AjaxRequestTarget target) {
                            reload(target);
                        }
                    }));

            add(new PortusDirChoice("portusDir").add(new AjaxFormComponentUpdatingBehavior(
                    "onchange") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    reload(target);
                }
            }));

            add(new Loop("hourHeader1", 12) {
                private static final long serialVersionUID = 1L;

                @Override
                protected void populateItem(LoopItem item) {
                    item.add(new Label("value",
                            Integer.toString(item.getIteration())));
                }
            });
            add(new Loop("hourHeader2", 12) {
                private static final long serialVersionUID = 1L;

                @Override
                protected void populateItem(LoopItem item) {
                    item.add(new Label("value",
                            Integer.toString(item.getIteration() + 12)));
                }
            });

            add(new AutoResolveListView<File>("files") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void populateItem(ListItem<File> item) {
                    final File file = item.getModelObject();
                    item.add(new AjaxLink<Void>("nameLink") {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            portusDir = file;
                            reload(target);
                        }
                    }.setEnabled(file.directory));

                    final long[] accessCounts = new long[24];
                    if (!file.directory) {
                        for (AccessCountDto dto : accessCountDao.selectAccessCountInDay(
                                date, file.fullPath)) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(dto.date);
                            accessCounts[cal.get(Calendar.HOUR_OF_DAY)] =
                                    dto.count;
                        }
                    }

                    item.add(new Loop("hour1", 12) {
                        private static final long serialVersionUID = 1L;

                        @Override
                        protected void populateItem(LoopItem item) {
                            if (!file.directory) {
                                item.add(new FormattedLabel("value", "%,d",
                                        accessCounts[item.getIteration()]));
                            } else {
                                item.add(new Label("value", "&nbsp;").setEscapeModelStrings(false));
                                item.add(new SimpleAttributeModifier("rowspan",
                                        "2"));
                            }
                        }
                    });
                    item.add(new Loop("hour2", 12) {
                        private static final long serialVersionUID = 1L;

                        @Override
                        protected void populateItem(LoopItem item) {
                            if (!file.directory) {
                                item.add(new FormattedLabel("value", "%,d",
                                        accessCounts[item.getIteration() + 12]));
                            } else {
                                item.setVisible(false);
                            }
                        }
                    });
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

            add(new Button("getlog") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onSubmit() {
                    getRequestCycle().setRequestTarget(new IRequestTarget() {
                        @Override
                        public void respond(RequestCycle requestCycle) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);

                            int year = cal.get(Calendar.YEAR);
                            int month = cal.get(Calendar.MONTH) + 1;
                            int day = cal.get(Calendar.DAY_OF_MONTH);

                            WebResponse response =
                                    (WebResponse) requestCycle.getResponse();
                            response.setContentType("application/x-gzip");
                            response.setAttachmentHeader(String.format(
                                    "accesslog_%02d-%02d-%02d.log.gz", year,
                                    month, day));
                            try {
                                Writer out =
                                        new OutputStreamWriter(
                                                new GZIPOutputStream(
                                                        response.getOutputStream()),
                                                "UTF-8");
                                pickUpAccessLogLogic.run(out,
                                        portusDir.domainId, year, month, day);
                                out.close();
                            } catch (IOException e) {
                                throw new WicketRuntimeException(
                                        "Unable to render string: "
                                                + e.getMessage(), e);
                            }
                        }

                        @Override
                        public void detach(RequestCycle requestCycle) {
                        }
                    });
                }
            });

        }

        private void reload(AjaxRequestTarget target) {
            if (portusDir != null) {
                files = fileAccessLogic.getFiles(portusDir);
            } else {
                files = new ArrayList<File>();
            }
            if (target == null) {
                return;
            }
            target.addComponent(AccessCountForm.this);
            target.addComponent(feedback);
        }
    }

    @Override
    protected String getPageTitle() {
        return "アクセス数";
    }
}
