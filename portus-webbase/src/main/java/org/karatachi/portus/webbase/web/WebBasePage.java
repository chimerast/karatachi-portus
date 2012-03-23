package org.karatachi.portus.webbase.web;

import org.apache.wicket.IPageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WebBasePage extends WebPage implements IHeaderContributor {
    private static final long serialVersionUID = 1L;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public static final ResourceReference DEFAULT_CSS =
            new ResourceReference(WebBasePage.class, "default.css");

    protected WebBasePage() {
        commonInit();
    }

    protected WebBasePage(IModel<?> model) {
        super(model);
        commonInit();
    }

    protected WebBasePage(IPageMap pageMap) {
        super(pageMap);
        commonInit();
    }

    protected WebBasePage(IPageMap pageMap, IModel<?> model) {
        super(pageMap, model);
        commonInit();
    }

    protected WebBasePage(PageParameters parameters) {
        super(parameters);
        commonInit();
    }

    protected WebBasePage(IPageMap pageMap, PageParameters parameters) {
        super(pageMap, parameters);
        commonInit();
    }

    private void commonInit() {
        add(new Label("title", getPageTitle()));
        add(new Label("pageTitle", getPageTitle()));

        Panel header = createHeaderPanel("header");
        if (header != null) {
            add(header);
        } else {
            add(new WebMarkupContainer("header").setVisible(false));
        }

        Panel menu = createMenuPanel("menu");
        if (menu != null) {
            add(menu);
        } else {
            add(new WebMarkupContainer("menu").setVisible(false));
        }
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.renderCSSReference(DEFAULT_CSS);
    }

    protected String getPageTitle() {
        return getClass().getSimpleName();
    }

    protected Panel createHeaderPanel(String id) {
        return null;
    }

    protected Panel createMenuPanel(String id) {
        return null;
    }
}
