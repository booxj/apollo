package com.booxj.opensource.mine.fundation.internals;

import java.net.*;
import java.util.*;

public enum NetworkInterfaceManager {
    INSTANCE;

    private InetAddress local;

    private InetAddress localHost;

    NetworkInterfaceManager() {
        load();
    }

    public InetAddress findValidateIp(List<InetAddress> addresses) {
        InetAddress local = null;
        int maxWeight = -1;
        for (InetAddress address : addresses) {
            if (address instanceof Inet4Address) {
                int weight = 0;

                if (address.isSiteLocalAddress()) {
                    weight += 8;
                }

                if (address.isLinkLocalAddress()) {
                    weight += 4;
                }

                if (address.isLoopbackAddress()) {
                    weight += 2;
                }

                // has host name
                // TODO fix performance issue when calling getHostName
                if (!Objects.equals(address.getHostName(), address.getHostAddress())) {
                    weight += 1;
                }

                if (weight > maxWeight) {
                    maxWeight = weight;
                    local = address;
                }
            }
        }
        return local;
    }

    public String getLocalHostAddress() {
        return local.getHostAddress();
    }

    public String getLocalHostName() {
        try {
            if (null == localHost) {
                localHost = InetAddress.getLocalHost();
            }
            return localHost.getHostName();
        } catch (UnknownHostException e) {
            return local.getHostName();
        }
    }

    private String getProperty(String name) {
        String value = null;

        value = System.getProperty(name);

        if (value == null) {
            value = System.getenv(name);
        }

        return value;
    }

    private void load() {
        String ip = getProperty("host.ip");

        if (ip != null) {
            try {
                local = InetAddress.getByName(ip);
                return;
            } catch (Exception e) {
                System.err.println(e);
                // ignore
            }
        }

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            List<NetworkInterface> nis = interfaces == null ? Collections.<NetworkInterface>emptyList()
                    : Collections.list(interfaces);
            //sort the network interfaces according to the index asc
            Collections.sort(nis, new Comparator<NetworkInterface>() {
                @Override
                public int compare(NetworkInterface nis1, NetworkInterface nis2) {
                    return Integer.compare(nis1.getIndex(), nis2.getIndex());
                }
            });
            List<InetAddress> addresses = new ArrayList<>();
            InetAddress local = null;

            try {
                for (NetworkInterface ni : nis) {
                    if (ni.isUp() && !ni.isLoopback()) {
                        addresses.addAll(Collections.list(ni.getInetAddresses()));
                    }
                }
                local = findValidateIp(addresses);
            } catch (Exception e) {
                // ignore
            }
            if (local != null) {
                local = local;
                return;
            }
        } catch (SocketException e) {
            // ignore it
        }

        local = InetAddress.getLoopbackAddress();
    }
}
