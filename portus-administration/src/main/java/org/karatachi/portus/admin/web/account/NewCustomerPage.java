package org.karatachi.portus.admin.web.account;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.convert.IConverter;
import org.karatachi.portus.admin.web.PortusBasePage;
import org.karatachi.portus.core.PortusRuntimeException;
import org.karatachi.portus.core.auth.Authorize;
import org.karatachi.portus.core.entity.Account;
import org.karatachi.portus.core.entity.Customer;
import org.karatachi.portus.core.entity.Domain;
import org.karatachi.portus.core.logic.AccountLogic;
import org.karatachi.portus.core.type.AccountRole;
import org.karatachi.portus.core.type.CustomerRole;
import org.karatachi.translator.DataSizeTranslator;
import org.karatachi.wicket.auto.SelfResolveForm;
import org.seasar.framework.container.annotation.tiger.Binding;

@Authorize(AccountRole.Bit.ROOT)
public class NewCustomerPage extends PortusBasePage {
    private static final long serialVersionUID = 1L;

    @Binding
    private AccountLogic accountLogic;

    public NewCustomerPage() {
        add(new CustomerForm("form"));
    }

    @Override
    protected String getPageTitle() {
        return "新規契約者追加";
    }

    public class CustomerForm extends SelfResolveForm {
        private static final long serialVersionUID = 1L;

        private Map<Long, Customer> parents = new TreeMap<Long, Customer>();

        private Customer customer = new Customer();
        private Domain domain = new Domain();
        private Account account = new Account();

        public CustomerForm(String id) {
            super(id);
            reloadCustomers();

            customer.id = -1L;
            customer.parentId = 0L;
            customer.quota = DataSizeTranslator.toLong("1G");
            customer.role = new CustomerRole();

            domain.id = -1L;

            account.id = -1L;
            account.role = AccountRole.CUSTOMER_ADMIN;
        }

        @Override
        protected void init(boolean confirm) {
            if (!confirm) {
                add(new DropDownChoice<Long>("customer.parentId",
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
                        }));
                add(new TextField<Long>("customer.quota") {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public IConverter getConverter(Class<?> type) {
                        return new GigaConverter();
                    }
                });
                add(new CustomerRoleCheckBox("hasChild",
                        new PropertyModel<Customer>(this, "customer"),
                        CustomerRole.Bit.HASCHILD, "子契約作成"));
                add(new Button("submit"));
            } else {
                String parentName =
                        parents.get(customer.parentId) == null ? "なし"
                                : parents.get(customer.parentId).name;
                add(new Label("customer.parentId", parentName).setRenderBodyOnly(true));
                add(new Label("customer.quota", String.format("%.2f",
                        customer.quota / GigaConverter.GIGA)).setRenderBodyOnly(true));
                add(new Label("hasChild", "子契約者作成:"
                        + customer.role.hasBit(CustomerRole.Bit.HASCHILD)).setRenderBodyOnly(true));
                add(new Button("submit").setVisible(false));
            }
        }

        @Override
        protected void onSubmit() {
            if (!isConfirm()) {
                try {
                    accountLogic.addNewCustomerTx(customer, domain, account);
                    setConfirm(true);
                } catch (PortusRuntimeException e) {
                    error(e.getMessage());
                } catch (Exception e) {
                    error("同じ名前の契約があります。");
                }
            }
        }

        public void reloadCustomers() {
            parents.clear();
            parents.put(0L, null);
            for (Customer customer : accountLogic.getCustomers(0L)) {
                parents.put(customer.id, customer);
            }
        }
    }
}
