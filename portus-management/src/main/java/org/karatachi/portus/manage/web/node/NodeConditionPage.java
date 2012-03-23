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
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.karatachi.portus.core.entity.Node;
import org.karatachi.portus.manage.dto.NodeConditionDto;
import org.karatachi.portus.manage.service.NodeManagementService;
import org.karatachi.portus.manage.web.PortusBasePage;
import org.karatachi.wicket.table.FormattedPropertyColumn;
import org.seasar.framework.container.annotation.tiger.Binding;

public class NodeConditionPage extends PortusBasePage {
    private static final long serialVersionUID = 1L;

    @Binding
    private NodeManagementService nodeManagementService;

    public NodeConditionPage() {
        List<IColumn<NodeConditionDto>> columns =
                new ArrayList<IColumn<NodeConditionDto>>();

        columns.add(new FormattedPropertyColumn<NodeConditionDto>(
                new Model<String>("ID"), "%012X", "ID", "node.id"));
        columns.add(new FormattedPropertyColumn<NodeConditionDto>(
                new Model<String>("BlockID"), "%012X", "node.nodeBlockId"));
        columns.add(new FormattedPropertyColumn<NodeConditionDto>(
                new Model<String>("Registered"), "%1$tF %1$tT", "Registered",
                "node.registered"));
        columns.add(new PropertyColumn<NodeConditionDto>(
                new Model<String>("IP"), "node.ipAddress"));
        columns.add(new AbstractColumn<NodeConditionDto>(new Model<String>(
                "Status")) {
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
                default:
                    value = Integer.toString(dto.getStatus());
                    break;
                }
                cellItem.add(new Label(componentId, value));
            }
        });
        columns.add(new PropertyColumn<NodeConditionDto>(new Model<String>(
                "Revision"), "node.nodeRevision"));
        columns.add(new PropertyColumn<NodeConditionDto>(new Model<String>(
                "Hostname"), "Hostname", "hostname"));
        columns.add(new PropertyColumn<NodeConditionDto>(new Model<String>(
                "Network"), "Network", "network"));
        columns.add(new PropertyColumn<NodeConditionDto>(new Model<String>(
                "NetworkName"), "NetworkName", "networkname"));
        columns.add(new PropertyColumn<NodeConditionDto>(new Model<String>(
                "Freespace"), "FreeSpace", "performance[FreeSpace]"));
        columns.add(new PropertyColumn<NodeConditionDto>(new Model<String>(
                "Uptime"), "System-Uptime", "performance[System-Uptime]"));
        columns.add(new PropertyColumn<NodeConditionDto>(new Model<String>(
                "Sent"), "Network-BytesSent", "performance[Network-BytesSent]"));
        columns.add(new PropertyColumn<NodeConditionDto>(new Model<String>(
                "Received"), "Network-BytesReceived",
                "performance[Network-BytesReceived]"));
        columns.add(new PropertyColumn<NodeConditionDto>(new Model<String>(
                "Queue"), "Http-QueueCount", "performance[Http-QueueCount]"));
        columns.add(new PropertyColumn<NodeConditionDto>(new Model<String>(
                "Active"), "Http-ActiveCount", "performance[Http-ActiveCount]"));
        columns.add(new PropertyColumn<NodeConditionDto>(new Model<String>(
                "CPU"), "Processor-ProcessorTime",
                "performance[Processor-ProcessorTime]"));

        add(new DefaultDataTable<NodeConditionDto>("table", columns,
                new NodeConditionProvider(), 100));
    }

    private class NodeConditionProvider extends
            SortableDataProvider<NodeConditionDto> implements Serializable {
        private static final long serialVersionUID = 1L;

        public NodeConditionProvider() {
            setSort("ID", true);
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
                } else if (getSort().getProperty().equals("Hostname")) {
                    sorting =
                            new NodeConditionDto.HostnameComparator(
                                    getSort().isAscending());
                } else if (getSort().getProperty().equals("Network")) {
                    sorting =
                            new NodeConditionDto.FieldComparator("network",
                                    getSort().isAscending());
                } else if (getSort().getProperty().equals("NetworkName")) {
                    sorting =
                            new NodeConditionDto.FieldComparator("networkname",
                                    getSort().isAscending());
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
            return nodeManagementService.getCount();
        }
    }
}
