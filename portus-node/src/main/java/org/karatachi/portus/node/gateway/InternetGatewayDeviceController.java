package org.karatachi.portus.node.gateway;

import java.io.IOException;

import net.sbbi.upnp.devices.UPNPDevice;
import net.sbbi.upnp.devices.UPNPRootDevice;
import net.sbbi.upnp.impls.InternetGatewayDevice;
import net.sbbi.upnp.messages.ActionMessage;
import net.sbbi.upnp.messages.ActionResponse;
import net.sbbi.upnp.messages.UPNPMessageFactory;
import net.sbbi.upnp.messages.UPNPResponseException;
import net.sbbi.upnp.services.UPNPService;

public class InternetGatewayDeviceController {
    public static InternetGatewayDeviceController[] getDevices(int timeout)
            throws IOException {
        InternetGatewayDevice[] igds = InternetGatewayDevice
                .getDevices(timeout);
        if (igds == null) {
            return null;
        } else {
            InternetGatewayDeviceController[] igdcs = new InternetGatewayDeviceController[igds.length];
            for (int i = 0; i < igds.length; ++i)
                igdcs[i] = new InternetGatewayDeviceController(igds[i]);
            return igdcs;
        }
    }

    private InternetGatewayDevice igd;
    private UPNPMessageFactory factory;

    public InternetGatewayDeviceController(InternetGatewayDevice igd) {
        this.igd = igd;

        UPNPDevice device = igd.getIGDRootDevice().getChildDevice(
                "urn:schemas-upnp-org:device:WANDevice:1");
        UPNPService service = device
                .getService("urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1");

        factory = UPNPMessageFactory.getNewInstance(service);
    }

    public long getTotalBytesSent() throws UPNPResponseException, IOException {
        ActionMessage msg = factory.getMessage("GetTotalBytesSent");
        return Long.parseLong(msg.service().getOutActionArgumentValue(
                "NewTotalBytesSent"));
    }

    public long getTotalBytesReceived() throws UPNPResponseException,
            IOException {
        ActionMessage msg = factory.getMessage("GetTotalBytesReceived");
        return Long.parseLong(msg.service().getOutActionArgumentValue(
                "NewTotalBytesReceived"));
    }

    public boolean addPortMapping(String description, String remoteHost,
            int internalPort, int externalPort, String internalClient,
            int leaseDuration, String protocol) throws IOException,
            UPNPResponseException {
        return igd.addPortMapping(description, remoteHost, internalPort,
                externalPort, internalClient, leaseDuration, protocol);
    }

    public boolean deletePortMapping(String remoteHost, int externalPort,
            String protocol) throws IOException, UPNPResponseException {
        return igd.deletePortMapping(remoteHost, externalPort, protocol);
    }

    public String getExternalIPAddress() throws UPNPResponseException,
            IOException {
        return igd.getExternalIPAddress();
    }

    public ActionResponse getGenericPortMappingEntry(int newPortMappingIndex)
            throws IOException, UPNPResponseException {
        return igd.getGenericPortMappingEntry(newPortMappingIndex);
    }

    public UPNPRootDevice getIGDRootDevice() {
        return igd.getIGDRootDevice();
    }

    public Integer getNatMappingsCount() throws IOException,
            UPNPResponseException {
        return igd.getNatMappingsCount();
    }

    public Integer getNatTableSize() throws IOException, UPNPResponseException {
        return igd.getNatTableSize();
    }

    public ActionResponse getSpecificPortMappingEntry(String remoteHost,
            int externalPort, String protocol) throws IOException,
            UPNPResponseException {
        return igd.getSpecificPortMappingEntry(remoteHost, externalPort,
                protocol);
    }
}
