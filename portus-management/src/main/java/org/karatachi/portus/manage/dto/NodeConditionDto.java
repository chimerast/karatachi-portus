package org.karatachi.portus.manage.dto;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.net.whois.WhoisClient;
import org.karatachi.net.shell.CommandResponse;
import org.karatachi.portus.core.entity.Node;

public class NodeConditionDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String UNRESOLVED = "Unresolved";
    private static final String UNKNOWN = "Unknown";

    private Node node;
    private String hostname = UNRESOLVED;
    private String network = UNRESOLVED;
    private String networkname = UNRESOLVED;
    private int status;
    private final Map<String, Long> performance;

    public NodeConditionDto() {
        this.hostname = "Unknown";
        this.performance = new HashMap<String, Long>();
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        if (this.node == null || !this.node.ipAddress.equals(node.ipAddress)) {
            try {
                this.hostname =
                        InetAddress.getByName(node.ipAddress).getCanonicalHostName();
            } catch (Exception e) {
                this.hostname = node.ipAddress;
            }

            try {
                WhoisClient whois = new WhoisClient();
                whois.connect("whois.nic.ad.jp");
                String out = whois.query("NET " + node.ipAddress + "/e");
                whois.disconnect();
                for (String line : out.split("\n")) {
                    if (line.startsWith("a.")) {
                        this.network =
                                line.substring(line.lastIndexOf(" ") + 1);
                    } else if (line.startsWith("b.")) {
                        this.networkname =
                                line.substring(line.lastIndexOf(" ") + 1);
                        break;
                    }
                }
            } catch (Exception e) {
                this.network = UNKNOWN;
                this.networkname = UNKNOWN;
            }
        }

        this.node = node;
        this.status = node.status;
    }

    public String getHostname() {
        return hostname;
    }

    public String getNetwork() {
        return network;
    }

    public String getNetworkname() {
        return networkname;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<String, Long> getPerformance() {
        return Collections.unmodifiableMap(performance);
    }

    public void setPerformance(CommandResponse response) {
        performance.clear();
        if (response == null) {
            return;
        }
        for (String name : response.getHeaderNames()) {
            performance.put(name, response.getHeaderLong(name));
        }
    }

    public static class IdComparator implements Comparator<NodeConditionDto> {
        private final boolean ascending;

        public IdComparator(boolean ascending) {
            this.ascending = ascending;
        }

        @Override
        public int compare(NodeConditionDto o1, NodeConditionDto o2) {
            if (o1.getNode().id < o2.getNode().id) {
                return ascending ? -1 : 1;
            } else if (o1.getNode().id > o2.getNode().id) {
                return ascending ? 1 : -1;
            } else {
                return 0;
            }
        }
    }

    public static class HostnameComparator implements
            Comparator<NodeConditionDto> {
        private final boolean ascending;

        public HostnameComparator(boolean ascending) {
            this.ascending = ascending;
        }

        @Override
        public int compare(NodeConditionDto o1, NodeConditionDto o2) {
            String[] dn1 = o1.getHostname().split("\\.");
            String[] dn2 = o2.getHostname().split("\\.");
            int l1 = dn1.length;
            int l2 = dn2.length;

            while (true) {
                --l1;
                --l2;

                if (l1 < 0 && l2 < 0) {
                    return 0;
                } else if (l1 < 0) {
                    return ascending ? -1 : 1;
                } else if (l2 < 0) {
                    return ascending ? 1 : -1;
                }

                int ret = dn1[l1].compareTo(dn2[l2]);
                if (ret != 0) {
                    return ascending ? ret : -ret;
                }
            }
        }
    }

    public static class FieldComparator implements Comparator<NodeConditionDto> {
        private final Field field;
        private final boolean isnode;
        private final boolean ascending;

        public FieldComparator(String field, boolean ascending)
                throws NoSuchFieldException {
            if (field.startsWith("node.")) {
                this.field = Node.class.getField(field.substring(5));
                this.isnode = true;
            } else {
                this.field = NodeConditionDto.class.getDeclaredField(field);
                this.isnode = false;
            }
            this.field.setAccessible(true);
            this.ascending = ascending;

            if (!this.field.getType().isPrimitive()
                    && !Comparable.class.isAssignableFrom(this.field.getType())) {
                throw new IllegalStateException();
            }
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public int compare(NodeConditionDto o1, NodeConditionDto o2) {
            try {
                int ret;
                if (isnode) {
                    ret =
                            ((Comparable) field.get(o1.getNode())).compareTo(field.get(o2.getNode()));
                } else {
                    ret = ((Comparable) field.get(o1)).compareTo(field.get(o2));
                }
                return ascending ? ret : -ret;
            } catch (IllegalAccessException e) {
                return 0;
            }
        }
    }

    public static class PerformanceComparator implements
            Comparator<NodeConditionDto> {
        private final String name;
        private final boolean ascending;

        public PerformanceComparator(String name, boolean ascending) {
            this.name = name;
            this.ascending = ascending;
        }

        @Override
        public int compare(NodeConditionDto o1, NodeConditionDto o2) {
            Long l1 = o1.getPerformance().get(name);
            Long l2 = o2.getPerformance().get(name);

            if (l1 == null && l2 == null) {
                return 0;
            } else if (l1 == null) {
                return ascending ? 1 : -1;
            } else if (l2 == null) {
                return ascending ? -1 : 1;
            }

            if (l1 < l2) {
                return ascending ? -1 : 1;
            } else if (l1 > l2) {
                return ascending ? 1 : -1;
            } else {
                return 0;
            }
        }
    }
}
