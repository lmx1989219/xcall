package com.lmx.xcall.client.springboot;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by limingxin on 2017/11/24.
 */
@Configuration
//这里特殊声明一下属性源，因为当前项目不是springboot启动
@PropertySource("classpath:client-config.properties")
@ConfigurationProperties(prefix = "xcall")
public class XcallProperties {
    private String registryAddress;

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }
}
