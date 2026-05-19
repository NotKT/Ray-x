package com.xraymod.client;

import com.xraymod.client.config.XRayConfig;

public final class XRayState {
    private XRayState() {}
    public static volatile boolean active = false;
    public static XRayConfig config;
}
