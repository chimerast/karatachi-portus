package org.karatachi.portus.admin.web.account;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.IModel;
import org.karatachi.portus.core.entity.Customer;
import org.karatachi.portus.core.type.CustomerRole;
import org.karatachi.wicket.form.behavior.CheckBoxLabel;

public class CustomerRoleCheckBox extends CheckBox {
    private static final long serialVersionUID = 1L;

    public CustomerRoleCheckBox(String id, final IModel<Customer> model,
            final CustomerRole.Bit bit, String label) {
        super(id, new IModel<Boolean>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Boolean getObject() {
                return model.getObject().role.hasBit(bit);
            }

            @Override
            public void setObject(Boolean object) {
                if (object) {
                    model.getObject().role.addBit(bit);
                } else {
                    model.getObject().role.removeBit(bit);
                }
            }

            @Override
            public void detach() {
            }
        });
        add(new CheckBoxLabel(label));
    }
}
