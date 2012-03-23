package org.karatachi.portus.admin.web.account;

import org.karatachi.portus.admin.web.PortusBasePage;
import org.karatachi.portus.core.dto.AccountDto;
import org.karatachi.portus.core.logic.AccountLogic;
import org.karatachi.wicket.auto.SelfResolveForm;
import org.seasar.framework.container.annotation.tiger.Binding;

public class PasswordPage extends PortusBasePage {
    private static final long serialVersionUID = 1L;

    @Binding
    private AccountLogic accountLogic;

    @Binding
    private AccountDto accountDto;

    public PasswordPage() {
        add(new PasswordForm("passwordForm"));
    }

    public class PasswordForm extends SelfResolveForm {
        private static final long serialVersionUID = 1L;

        private String name;
        private String password;
        private String passwordConfirm;

        public PasswordForm(String id) {
            super(id);
            setOutputMarkupId(true);
            this.name = accountDto.getAccount().name;
        }

        @Override
        protected void onSubmit() {
            if (!password.equals(passwordConfirm)) {
                error("パスワードとパスワード(確認)が一致しません。");
                return;
            }
            try {
                accountLogic.updateAccountPasswordTx(accountDto.getAccount(),
                        password);
                info("パスワードを変更しました。");
            } catch (Exception e) {
                error("パスワードの設定に失敗しました。");
            }
        }
    }

    @Override
    protected String getPageTitle() {
        return "パスワード変更";
    }
}
