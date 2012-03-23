package org.karatachi.portus.admin.web.account;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.LoadableDetachableModel;
import org.karatachi.portus.admin.web.PortusBasePage;
import org.karatachi.portus.core.dao.AccessCountDao;
import org.karatachi.portus.core.dao.FileDao;
import org.karatachi.portus.core.dto.AccountDto;
import org.karatachi.portus.core.entity.Customer;
import org.karatachi.portus.core.entity.Domain;
import org.karatachi.portus.core.logic.AccountLogic;
import org.karatachi.portus.core.type.AccountRole.Bit;
import org.karatachi.wicket.auto.AutoResolveListView;
import org.karatachi.wicket.label.DataSizeLabel;
import org.karatachi.wicket.label.FormattedLabel;
import org.seasar.framework.container.annotation.tiger.Binding;

public class CustomerTransferPage extends PortusBasePage {
    private static final long serialVersionUID = 1L;

    @Binding
    private AccountLogic accountLogic;
    @Binding
    private AccessCountDao accessCountDao;
    @Binding
    private FileDao fileDao;

    @Binding
    private AccountDto accountDto;

    private Date date;

    public CustomerTransferPage(PageParameters parameters) {
        super(parameters);

        this.date = new Date(parameters.getLong("date", new Date().getTime()));

        add(new BookmarkablePageLink<Void>("prev", CustomerTransferPage.class,
                new PageParameters("date="
                        + DateUtils.addMonths(date, -1).getTime())));
        add(new BookmarkablePageLink<Void>("next", CustomerTransferPage.class,
                new PageParameters("date="
                        + DateUtils.addMonths(date, 1).getTime())));

        add(new FormattedLabel("period", "%1$tY年%1$tm月", DateUtils.truncate(
                date, Calendar.MONTH)));

        add(new CustomerListView("customers"));
    }

    public class CustomerListView extends AutoResolveListView<Customer> {
        private static final long serialVersionUID = 1L;

        private boolean hasChild;

        public CustomerListView(String id) {
            super(id, new LoadableDetachableModel<List<Customer>>() {
                private static final long serialVersionUID = 1L;

                @Override
                protected List<Customer> load() {
                    if (accountDto.hasAccountRole(Bit.ROOT)) {
                        return accountLogic.getCustomers(0);
                    } else {
                        List<Customer> customers = new ArrayList<Customer>();
                        customers.add(accountDto.getCustomer());
                        return customers;
                    }
                }
            });
            hasChild = true;
        }

        public CustomerListView(String id, final Customer customer) {
            super(id, new LoadableDetachableModel<List<Customer>>() {
                private static final long serialVersionUID = 1L;

                @Override
                protected List<Customer> load() {
                    return accountLogic.getCustomers(customer.id);
                }
            });
            hasChild = false;
        }

        @Override
        protected void populateItem(final ListItem<Customer> item) {
            item.add(new DomainListView("domains", item.getModelObject()));
            if (hasChild) {
                item.add(new CustomerListView("children", item.getModelObject()));
            }
        }
    }

    public class DomainListView extends AutoResolveListView<Domain> {
        private static final long serialVersionUID = 1L;

        public DomainListView(String id, final Customer customer) {
            super(id, new LoadableDetachableModel<List<Domain>>() {
                private static final long serialVersionUID = 1L;

                @Override
                protected List<Domain> load() {
                    return accountLogic.getDomains(customer);
                }
            });
        }

        @Override
        protected void populateItem(ListItem<Domain> item) {
            Domain domain = item.getModelObject();

            Date d = DateUtils.truncate(date, Calendar.MONTH);

            Map<String, Number> domainInfo =
                    fileDao.selectDomainInfo(domain.id);
            item.add(new DataSizeLabel("totalSize",
                    domainInfo.get("totalSize").longValue()));
            item.add(new FormattedLabel("totalCount", "%,d", domainInfo.get(
                    "totalCount").longValue()));

            Map<String, Object> count =
                    accessCountDao.selectAccessCountByDomain(domain.id, d);
            item.add(new FormattedLabel("totalAccess", "%,d",
                    ((Number) count.get("totalAccess")).longValue()));
            item.add(new DataSizeLabel("totalTransfer",
                    ((Number) count.get("totalTransfer")).longValue()));
        }
    }

    @Override
    protected String getPageTitle() {
        return "転送量一覧";
    }
}
