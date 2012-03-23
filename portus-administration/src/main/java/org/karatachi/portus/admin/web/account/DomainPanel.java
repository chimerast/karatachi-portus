package org.karatachi.portus.admin.web.account;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.karatachi.portus.core.PortusRuntimeException;
import org.karatachi.portus.core.entity.Account;
import org.karatachi.portus.core.entity.Customer;
import org.karatachi.portus.core.entity.Domain;
import org.karatachi.portus.core.logic.AccountLogic;
import org.karatachi.wicket.auto.AutoResolveForm;
import org.karatachi.wicket.auto.AutoResolveListView;
import org.karatachi.wicket.dialog.ConfirmLink;
import org.seasar.framework.container.annotation.tiger.Binding;

public class DomainPanel extends Panel {
    private static final long serialVersionUID = 1L;

    @Binding
    private AccountLogic accountLogic;

    private DomainForm domainForm;

    private Customer customer;
    private Domain selected;

    public DomainPanel(String id, Customer customer) {
        super(id);
        this.customer = customer;

        resetForm();

        add(new AjaxLink<Void>("reset") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                resetForm();
                domainForm.setVisible(true);
                target.addComponent(domainForm);
            }
        });

        add(domainForm =
                new DomainForm("domainForm", new PropertyModel<Domain>(this,
                        "selected")));

        add(new DomainListView("domains"));
    }

    private void resetForm() {
        selected = new Domain();
        selected.id = -1L;
        selected.customerId = DomainPanel.this.customer.id;
        if (domainForm != null) {
            domainForm.reload();
        }
    }

    public class DomainForm extends AutoResolveForm<Domain> {
        private static final long serialVersionUID = 1L;

        private List<Account> activeList = new ArrayList<Account>();
        private List<Account> activeSelected = new ArrayList<Account>();
        private List<Account> inactiveList = new ArrayList<Account>();
        private List<Account> inactiveSelected = new ArrayList<Account>();

        public DomainForm(String id, IModel<Domain> model) {
            super(id, model);
            setOutputMarkupId(true);
            setOutputMarkupPlaceholderTag(true);
            setVisible(false);

            add(new Label("id", new LoadableDetachableModel<String>() {
                private static final long serialVersionUID = 1L;

                @Override
                protected String load() {
                    return getModelObject().id == -1L ? "新規作成"
                            : Long.toString(getModelObject().id);
                }
            }));

            add(new ListMultipleChoice<Account>("active",
                    new PropertyModel<List<Account>>(this, "activeSelected"),
                    new PropertyModel<List<Account>>(this, "activeList"),
                    new ChoiceRenderer<Account>("name")));
            add(new ListMultipleChoice<Account>("inactive",
                    new PropertyModel<List<Account>>(this, "inactiveSelected"),
                    new PropertyModel<List<Account>>(this, "inactiveList"),
                    new ChoiceRenderer<Account>("name")));

            add(new AjaxButton("join") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    activeList.addAll(inactiveSelected);
                    inactiveList.removeAll(inactiveSelected);
                    activeSelected.clear();
                    inactiveSelected.clear();
                    target.addComponent(DomainForm.this);
                }
            });
            add(new AjaxButton("remove") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    activeList.removeAll(activeSelected);
                    inactiveList.addAll(activeSelected);
                    activeSelected.clear();
                    inactiveSelected.clear();
                    target.addComponent(DomainForm.this);
                }
            });
            add(new Button("submit") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onSubmit() {
                    try {
                        if (DomainForm.this.getModelObject().id == -1L) {
                            selected =
                                    accountLogic.addDomainTx(
                                            DomainForm.this.getModelObject(),
                                            activeList);
                            info("ドメインを追加しました。");
                        } else {
                            selected =
                                    accountLogic.updateDomainTx(
                                            DomainForm.this.getModelObject(),
                                            activeList);
                            info("ドメインを更新しました。");
                        }
                    } catch (PortusRuntimeException e) {
                        error(e.getMessage());
                    } catch (Exception e) {
                        error("同じ名前のドメインがあります。");
                    }
                }
            });

            reload();
        }

        private void reload() {
            activeSelected.clear();
            inactiveSelected.clear();
            if (selected.id != -1L) {
                activeList = accountLogic.getActiveAccounts(selected);
            } else {
                activeList =
                        new ArrayList<Account>(
                                accountLogic.getAccounts(customer));
            }
            inactiveList =
                    new ArrayList<Account>(accountLogic.getAccounts(customer));
            inactiveList.removeAll(activeList);
        }
    }

    public class DomainListView extends AutoResolveListView<Domain> {
        private static final long serialVersionUID = 1L;

        public DomainListView(String id) {
            super(id, new LoadableDetachableModel<List<Domain>>() {
                private static final long serialVersionUID = 1L;

                @Override
                protected List<Domain> load() {
                    return accountLogic.getDomains(customer);
                }
            });
        }

        @Override
        protected void populateItem(final ListItem<Domain> item) {
            Domain domain = item.getModelObject();

            item.add(new AjaxEventBehavior("onclick") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onEvent(AjaxRequestTarget target) {
                    selected = item.getModelObject();
                    domainForm.reload();
                    domainForm.setVisible(true);
                    target.addComponent(domainForm);
                }
            });

            item.add(new ConfirmLink<Void>("delete", domain.name
                    + "を削除してもよろしいですか？") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick() {
                    accountLogic.removeDomainTx(item.getModelObject());
                    resetForm();
                    DomainListView.this.getModel().detach();
                }
            });
        }
    }
}
