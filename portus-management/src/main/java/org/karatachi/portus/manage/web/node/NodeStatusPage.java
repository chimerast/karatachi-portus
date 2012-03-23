package org.karatachi.portus.manage.web.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.resource.AbstractStringResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.karatachi.portus.core.entity.Node;
import org.karatachi.portus.manage.dto.NodeConditionDto;
import org.karatachi.portus.manage.service.NodeManagementService;
import org.karatachi.portus.webbase.web.WebBasePage;
import org.karatachi.wicket.table.FormattedPropertyColumn;
import org.seasar.framework.container.annotation.tiger.Binding;

public class NodeStatusPage extends WebBasePage {
    private static final long serialVersionUID = 1L;

    @Binding
    private NodeManagementService nodeManagementService;

    public NodeStatusPage() {
        add(new Link<Void>("csv") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                IResourceStream rs =
                        new AbstractStringResourceStream("text/csv") {
                            private static final long serialVersionUID = 1L;

                            @Override
                            protected String getString() {
                                try {
                                    List<NodeConditionDto> data =
                                            nodeManagementService.getNodeList(new NodeConditionDto.FieldComparator(
                                                    "node.registered", true));
                                    removeIgnoringNode(data);

                                    StringBuilder ret = new StringBuilder();
                                    for (NodeConditionDto dto : data) {
                                        ret.append(String.format(
                                                "%012X,%tF,%s,%d\n",
                                                dto.getNode().id,
                                                dto.getNode().registered,
                                                dto.getNode().ipAddress,
                                                dto.getStatus()));
                                    }
                                    return ret.toString();
                                } catch (NoSuchFieldException e) {
                                    throw new IllegalStateException(e);
                                }
                            }
                        };
                getRequestCycle().setRequestTarget(
                        new ResourceStreamRequestTarget(rs, "nodestatus.csv"));
            }
        });

        List<IColumn<NodeConditionDto>> columns =
                new ArrayList<IColumn<NodeConditionDto>>();

        columns.add(new FormattedPropertyColumn<NodeConditionDto>(
                new Model<String>("ID"), "%012X", "ID", "node.id"));
        columns.add(new FormattedPropertyColumn<NodeConditionDto>(
                new Model<String>("登録日時"), "%tF", "Registered",
                "node.registered"));
        columns.add(new PropertyColumn<NodeConditionDto>(new Model<String>(
                "IPアドレス"), "node.ipAddress"));
        columns.add(new AbstractColumn<NodeConditionDto>(
                new Model<String>("状態")) {
            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(
                    Item<ICellPopulator<NodeConditionDto>> cellItem,
                    String componentId, IModel<NodeConditionDto> rowModel) {
                NodeConditionDto dto = rowModel.getObject();
                String value;
                switch (dto.getStatus()) {
                case Node.STATUS_DOWN:
                    value = "停止";
                    break;
                case Node.STATUS_OK:
                    value = "稼働";
                    break;
                case Node.STATUS_OVERLOADED:
                    value = "過負荷";
                    break;
                case Node.STATUS_NOT_WORKING:
                    value = "故障";
                    break;
                case Node.STATUS_MAINTENANCE:
                    value = "メンテナンス";
                    break;
                case Node.STATUS_REMOVED:
                    value = "撤去";
                    break;
                default:
                    value = Integer.toString(dto.getStatus());
                    break;
                }
                cellItem.add(new Label(componentId, value));
            }
        });

        add(new DefaultDataTable<NodeConditionDto>("table", columns,
                new NodeConditionProvider(), 100));
    }

    private class NodeConditionProvider extends
            SortableDataProvider<NodeConditionDto> implements Serializable {
        private static final long serialVersionUID = 1L;

        public NodeConditionProvider() {
            setSort("Registered", true);
        }

        @Override
        public Iterator<NodeConditionDto> iterator(int first, int count) {
            Comparator<NodeConditionDto> sorting;
            try {
                if (getSort().getProperty().equals("ID")) {
                    sorting =
                            new NodeConditionDto.FieldComparator("node.id",
                                    getSort().isAscending());
                } else if (getSort().getProperty().equals("Registered")) {
                    sorting =
                            new NodeConditionDto.FieldComparator(
                                    "node.registered", getSort().isAscending());
                } else {
                    sorting =
                            new NodeConditionDto.PerformanceComparator(
                                    getSort().getProperty(),
                                    getSort().isAscending());
                }
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException(e);
            }

            List<NodeConditionDto> data =
                    nodeManagementService.getNodeList(sorting);
            removeIgnoringNode(data);

            int fromIndex = first;
            int toIndex =
                    first + count < data.size() ? first + count : data.size();
            return data.subList(fromIndex, toIndex).iterator();
        }

        @Override
        public IModel<NodeConditionDto> model(NodeConditionDto object) {
            return new Model<NodeConditionDto>(object);
        }

        @Override
        public int size() {
            try {
                List<NodeConditionDto> data =
                        nodeManagementService.getNodeList(new NodeConditionDto.FieldComparator(
                                "node.id", true));
                removeIgnoringNode(data);
                return data.size();
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private void removeIgnoringNode(List<NodeConditionDto> data) {
        Iterator<NodeConditionDto> itr = data.iterator();
        while (itr.hasNext()) {
            if (itr.next().getNode().ipAddress.startsWith("59.106.")) {
                itr.remove();
            }
        }
    }
}
