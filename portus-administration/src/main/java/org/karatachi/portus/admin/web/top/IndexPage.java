package org.karatachi.portus.admin.web.top;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.karatachi.portus.admin.web.PortusBasePage;
import org.karatachi.portus.core.dao.AccessCountDao;
import org.karatachi.portus.core.dao.FileDao;
import org.karatachi.portus.core.dto.AccountDto;
import org.karatachi.portus.core.entity.Domain;
import org.karatachi.portus.core.logic.AccountLogic;
import org.karatachi.portus.core.logic.SystemLogic;
import org.karatachi.wicket.auto.AutoResolveListView;
import org.karatachi.wicket.label.DataSizeLabel;
import org.karatachi.wicket.label.FormattedLabel;
import org.seasar.framework.container.annotation.tiger.Binding;

public class IndexPage extends PortusBasePage {
    private static final long serialVersionUID = 1L;

    @Binding
    private AccountLogic accountLogic;
    @Binding
    private SystemLogic systemLogic;
    @Binding
    private FileDao fileDao;
    @Binding
    private AccessCountDao accessCountDao;

    @Binding
    private AccountDto accountDto;

    public IndexPage() {
        String notify = systemLogic.getNotifyMessage();
        add(new Label("notify", notify).setEscapeModelStrings(false).setVisible(
                notify != null));

        add(new Label("name", accountDto.getCustomer().name));
        add(new Label("code", accountDto.getCustomerCode()));

        add(new AutoResolveListView<Domain>("domains", getRelatedDomains()) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<Domain> item) {
                Domain domain = item.getModelObject();
                Map<String, Number> domainInfo =
                        fileDao.selectDomainInfo(domain.id);

                item.add(new DataSizeLabel("totalSize", domainInfo.get(
                        "totalSize").longValue()));
                item.add(new FormattedLabel("totalCount", "%,d",
                        domainInfo.get("totalCount").longValue()));

                Date date = DateUtils.truncate(new Date(), Calendar.MONTH);

                Map<String, Object> count =
                        accessCountDao.selectAccessCountByDomain(domain.id,
                                date);
                item.add(new FormattedLabel("totalAccess", "%,d",
                        ((Number) count.get("totalAccess")).longValue()));
                item.add(new DataSizeLabel("totalTransfer",
                        ((Number) count.get("totalTransfer")).longValue()));

                DateFormat df =
                        SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
                String range =
                        "(" + df.format((Date) count.get("from")) + "～"
                                + df.format((Date) count.get("to")) + ")";
                item.add(new Label("range1", range));
                item.add(new Label("range2", range));
            }
        });
    }

    public List<Domain> getRelatedDomains() {
        List<Domain> ret = accountLogic.getDomains(accountDto.getCustomer());
        Iterator<Domain> itr = ret.iterator();
        while (itr.hasNext()) {
            if (!accountDto.getRoots().contains(
                    fileDao.selectDomainDirectory(itr.next().id).id)) {
                itr.remove();
            }
        }
        return ret;
    }

    @Override
    protected String getPageTitle() {
        return "管理トップ";
    }
}
