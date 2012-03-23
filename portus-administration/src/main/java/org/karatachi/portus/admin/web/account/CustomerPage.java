package org.karatachi.portus.admin.web.account;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.convert.IConverter;
import org.karatachi.portus.admin.web.PortusBasePage;
import org.karatachi.portus.core.PortusRuntimeException;
import org.karatachi.portus.core.auth.Authorize;
import org.karatachi.portus.core.dto.AccountDto;
import org.karatachi.portus.core.entity.Customer;
import org.karatachi.portus.core.logic.AccountLogic;
import org.karatachi.portus.core.type.AccountRole;
import org.karatachi.portus.core.type.AccountRole.Bit;
import org.karatachi.portus.core.type.CustomerRole;
import org.karatachi.translator.DataSizeTranslator;
import org.karatachi.wicket.auto.AutoResolveForm;
import org.karatachi.wicket.auto.AutoResolveListView;
import org.karatachi.wicket.dialog.ConfirmLink;
import org.seasar.framework.container.annotation.tiger.Binding;

@Authorize(AccountRole.Bit.MODIFY_CUSTOMER)
public class CustomerPage extends PortusBasePage {
    private static final long serialVersionUID = 1L;

    @Binding
    private AccountLogic accountLogic;

    @Binding
    private AccountDto accountDto;

    private CustomerForm customerForm;
    private AjaxTabbedPanel domainAccount;

    private Customer selected;

    public CustomerPage() {
        resetForm();

        add(new AjaxLink<Void>("reset") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                resetForm();
                customerForm.setVisible(true);
                domainAccount.setVisible(false);
                target.addComponent(customerForm);
                target.addComponent(domainAccount);
            }
        });

        add(customerForm =
                new CustomerForm("customerForm", new PropertyModel<Customer>(
                        this, "selected")));

        List<ITab> tabs = new ArrayList<ITab>();
        tabs.add(new AbstractTab(new Model<String>("アカウント")) {
            private static final long serialVersionUID = 1L;

            @Override
            public Panel getPanel(String panelId) {
                return new AccountPanel(panelId, selected);
            }
        });
        tabs.add(new AbstractTab(new Model<String>("ドメイン")) {
            private static final long serialVersionUID = 1L;

            @Override
            public Panel getPanel(String panelId) {
                return new DomainPanel(panelId, selected);
            }
        });
        add(domainAccount = new AjaxTabbedPanel("tabs", tabs));
        domainAccount.setOutputMarkupPlaceholderTag(true);
        domainAccount.setVisible(false);

        add(new CustomerListView("customers"));
    }

    private void resetForm() {
        selected = new Customer();
        selected.id = -1L;
        if (accountDto.hasAccountRole(Bit.ROOT)) {
            selected.parentId = 0L;
        } else {
            selected.parentId = accountDto.getCustomer().id;
        }
        selected.quota = DataSizeTranslator.toLong("1G");
        selected.role = new CustomerRole();
    }

    public class CustomerForm extends AutoResolveForm<Customer> {
        private static final long serialVersionUID = 1L;

        private Map<Long, Customer> parents = new TreeMap<Long, Customer>();

        public CustomerForm(String id, IModel<Customer> model) {
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

            reloadParentCustomers();

            add(new DropDownChoice<Long>("parentId",
                    new AbstractReadOnlyModel<List<Long>>() {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public List<Long> getObject() {
                            return new ArrayList<Long>(parents.keySet());
                        }
                    }, new ChoiceRenderer<Long>() {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public Object getDisplayValue(Long object) {
                            if (object == 0L) {
                                return "なし";
                            }
                            return parents.get(object).name;
                        }
                    }).setVisible(accountDto.hasAccountRole(Bit.ROOT)));

            add(new TextField<Long>("quota") {
                private static final long serialVersionUID = 1L;

                @Override
                public IConverter getConverter(Class<?> type) {
                    return new GigaConverter();
                }
            });

            add(new CustomerRoleCheckBox("hasChild", model,
                    CustomerRole.Bit.HASCHILD, "子契約作成").setVisible(accountDto.hasAccountRole(Bit.ROOT)));
        }

        @Override
        protected void onSubmit() {
            try {
                if (getModelObject().id == -1L) {
                    selected = accountLogic.addCustomerTx(getModelObject());
                    info("契約を追加しました。");
                    resetForm();
                } else {
                    selected = accountLogic.updateCustomerTx(getModelObject());
                    info("契約を更新しました。");
                }
                customerForm.reloadParentCustomers();
            } catch (PortusRuntimeException e) {
                error(e.getMessage());
            } catch (Exception e) {
                error("同じ名前の契約があります。");
            }
        }

        public void reloadParentCustomers() {
            parents.clear();
            parents.put(0L, null);
            for (Customer customer : accountLogic.getCustomers(0L)) {
                parents.put(customer.id, customer);
            }
        }
    }

    public class CustomerListView extends AutoResolveListView<Customer> {
        private static final long serialVersionUID = 1L;

        public CustomerListView(String id) {
            super(id, new LoadableDetachableModel<List<Customer>>() {
                private static final long serialVersionUID = 1L;

                @Override
                protected List<Customer> load() {
                    if (accountDto.hasAccountRole(Bit.ROOT)) {
                        return accountLogic.getCustomers();
                    } else {
                        List<Customer> customers = new ArrayList<Customer>();
                        customers.add(accountDto.getCustomer());
                        customers.addAll(accountLogic.getCustomers(accountDto.getCustomer().id));
                        return customers;
                    }
                }
            });
        }

        @Override
        protected void populateItem(final ListItem<Customer> item) {
            Customer customer = item.getModelObject();
            item.add(new Label("name", (customer.parentId == 0 ? "" : "└ ")
                    + customer.name));

            item.add(new AjaxEventBehavior("onclick") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onEvent(AjaxRequestTarget target) {
                    selected = item.getModelObject();
                    customerForm.setVisible(true);
                    domainAccount.setVisible(true);
                    domainAccount.setSelectedTab(0);
                    target.addComponent(customerForm);
                    target.addComponent(domainAccount);
                }
            });

            item.add(new ConfirmLink<Void>("delete", customer.name
                    + "を削除してもよろしいですか？") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick() {
                    accountLogic.removeCustomerTx(item.getModelObject());
                    customerForm.reloadParentCustomers();
                    domainAccount.setVisible(false);
                    resetForm();
                    CustomerListView.this.getModel().detach();
                }
            });
        }
    }

    @Override
    protected String getPageTitle() {
        return "契約者管理";
    }
}
