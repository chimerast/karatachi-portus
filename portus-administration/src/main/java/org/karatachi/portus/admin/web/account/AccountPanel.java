package org.karatachi.portus.admin.web.account;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.karatachi.portus.core.PortusRuntimeException;
import org.karatachi.portus.core.dto.AccountDto;
import org.karatachi.portus.core.entity.Account;
import org.karatachi.portus.core.entity.Customer;
import org.karatachi.portus.core.logic.AccountLogic;
import org.karatachi.portus.core.type.AccountRole;
import org.karatachi.wicket.auto.AutoResolveForm;
import org.karatachi.wicket.auto.AutoResolveListView;
import org.karatachi.wicket.dialog.ConfirmLink;
import org.seasar.framework.container.annotation.tiger.Binding;

public class AccountPanel extends Panel {
    private static final long serialVersionUID = 1L;

    @Binding
    private AccountLogic accountLogic;

    @Binding
    private AccountDto accountDto;

    private AccountForm accountForm;

    private Customer customer;
    private Account selected;

    public AccountPanel(String id, Customer customer) {
        super(id);
        this.customer = customer;
        resetForm();

        add(new AjaxLink<Void>("reset") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                resetForm();
                accountForm.setVisible(true);
                target.addComponent(accountForm);
            }
        });

        add(accountForm =
                new AccountForm("accountForm", new PropertyModel<Account>(this,
                        "selected")));
        add(new AccountListView("accounts"));
    }

    private void resetForm() {
        selected = new Account();
        selected.id = -1L;
        selected.customerId = AccountPanel.this.customer.id;
        selected.role = AccountRole.DEFAULT;
    }

    public class AccountForm extends AutoResolveForm<Account> {
        private static final long serialVersionUID = 1L;

        public AccountForm(String id, IModel<Account> model) {
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

            add(new PasswordTextField("password").setResetPassword(false));

            List<AccountRole> choices =
                    Arrays.asList(AccountRole.DEFAULT,
                            AccountRole.CUSTOMER_ADMIN);
            add(new DropDownChoice<AccountRole>("role", choices));
        }

        @Override
        protected void onSubmit() {
            try {
                if (getModelObject().id == -1L) {
                    selected = accountLogic.addAccountTx(getModelObject());
                    info("アカウントを追加しました。");
                    resetForm();
                } else {
                    selected = accountLogic.updateAccountTx(getModelObject());
                    info("アカウントを更新しました。");
                }
            } catch (PortusRuntimeException e) {
                error(e.getMessage());
            } catch (Exception e) {
                error("同じ名前のアカウントがあります。");
            }
        }
    }

    public class AccountListView extends AutoResolveListView<Account> {
        private static final long serialVersionUID = 1L;

        public AccountListView(String id) {
            super(id, new LoadableDetachableModel<List<Account>>() {
                private static final long serialVersionUID = 1L;

                @Override
                protected List<Account> load() {
                    return accountLogic.getAccounts(customer);
                }
            });
        }

        @Override
        protected void populateItem(final ListItem<Account> item) {
            Account account = item.getModelObject();

            item.setVisible(accountDto.getAccount().id != account.id);

            item.add(new AjaxEventBehavior("onclick") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onEvent(AjaxRequestTarget target) {
                    selected = item.getModelObject();
                    accountForm.setVisible(true);
                    target.addComponent(accountForm);
                }
            });

            item.add(new ConfirmLink<Void>("delete", account.name
                    + "を削除してもよろしいですか？") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick() {
                    accountLogic.removeAccountTx(item.getModelObject());
                    resetForm();
                    AccountListView.this.getModel().detach();
                }
            });
        }
    }
}
