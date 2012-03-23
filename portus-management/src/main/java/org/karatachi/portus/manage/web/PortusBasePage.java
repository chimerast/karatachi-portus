package org.karatachi.portus.manage.web;

import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.karatachi.portus.core.auth.Authorize;
import org.karatachi.portus.core.type.AccountRole;
import org.karatachi.portus.manage.web.top.HeaderPanel;
import org.karatachi.portus.manage.web.top.MenuPanel;
import org.karatachi.portus.webbase.web.WebBasePage;
import org.karatachi.wicket.dialog.ConfirmDialog;
import org.karatachi.wicket.dialog.InputDialog;

@Authorize(AccountRole.Bit.ROOT)
public abstract class PortusBasePage extends WebBasePage {
    private static final long serialVersionUID = 1L;

    protected FeedbackPanel feedback;
    protected ConfirmDialog confirm;
    protected InputDialog input;

    public PortusBasePage() {
        commonInit();
    }

    public PortusBasePage(IModel<?> model) {
        super(model);
        commonInit();
    }

    public PortusBasePage(PageMap pageMap, IModel<?> model) {
        super(pageMap, model);
        commonInit();
    }

    public PortusBasePage(PageMap pageMap) {
        super(pageMap);
        commonInit();
    }

    public PortusBasePage(PageParameters parameters) {
        super(parameters);
        commonInit();
    }

    private void commonInit() {
        add(feedback = new FeedbackPanel("feedback"));
        feedback.setOutputMarkupId(true);
        add(confirm = new ConfirmDialog("confirm"));
        add(input = new InputDialog("input"));
    }

    @Override
    protected Panel createHeaderPanel(String id) {
        return new HeaderPanel(id);
    }

    @Override
    protected Panel createMenuPanel(String id) {
        return new MenuPanel(id);
    }
}
