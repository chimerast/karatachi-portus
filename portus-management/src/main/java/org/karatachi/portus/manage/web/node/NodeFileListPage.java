package org.karatachi.portus.manage.web.node;

import java.util.Date;
import java.util.List;

import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.karatachi.portus.common.net.AccessInfo;
import org.karatachi.portus.core.dto.NodeFileDto;
import org.karatachi.portus.core.entity.Node;
import org.karatachi.portus.core.entity.Storedinfo;
import org.karatachi.portus.manage.logic.FileConsistencyLogic;
import org.karatachi.portus.manage.web.PortusBasePage;
import org.karatachi.wicket.auto.AutoResolvePageableListView;
import org.karatachi.wicket.listview.SimplePageableListViewNavigator;
import org.seasar.framework.container.annotation.tiger.Binding;

public class NodeFileListPage extends PortusBasePage {
    private static final long serialVersionUID = 1L;

    @Binding
    private FileConsistencyLogic fileConsistencyLogic;

    private final Node chassis;

    public NodeFileListPage(Node chassiz) {
        this.chassis = chassiz;

        setDefaultModel(new LoadableDetachableModel<List<Storedinfo>>() {
            private static final long serialVersionUID = 1L;

            @Override
            protected List<Storedinfo> load() {
                return fileConsistencyLogic.getNodeFile(chassis.id);
            }
        });

        AutoResolvePageableListView<NodeFileDto> table;
        add(table = new ChassisFileListView("chassisFileList"));
        add(new SimplePageableListViewNavigator("pageTableNav", table));
    }

    @SuppressWarnings("unchecked")
    public final IModel<List<NodeFileDto>> getModel() {
        return (IModel<List<NodeFileDto>>) getDefaultModel();
    }

    private class ChassisFileListView extends
            AutoResolvePageableListView<NodeFileDto> {
        private static final long serialVersionUID = 1L;

        public ChassisFileListView(String id) {
            super(id, NodeFileListPage.this.getModel(), 50);
        }

        @Override
        protected void populateItem(ListItem<NodeFileDto> item) {
            NodeFileDto dto = item.getModelObject();
            Date expire = new Date(new Date().getTime() + 10 * 60 * 1000);
            AccessInfo info =
                    new AccessInfo(dto.getFileId(), "0.0.0.0", dto.getName(),
                            expire.getTime(), false, false);
            item.add(new ExternalLink("directLink", String.format(
                    "http://%s:%s/~%s?x=%s", chassis.ipAddress,
                    chassis.httpPort, dto.getFullPath(),
                    AccessInfo.encrypt(info))));
        }
    }
}
