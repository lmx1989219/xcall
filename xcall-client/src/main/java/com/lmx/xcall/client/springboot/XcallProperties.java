package com.lmx.xcall.client.springboot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by limingxin on 2017/11/24.
 */
@ConfigurationProperties(prefix = "xcall")
public class XcallProperties {
    String registryAddress;

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }
}
