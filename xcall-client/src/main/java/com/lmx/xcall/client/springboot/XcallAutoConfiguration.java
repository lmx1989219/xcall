package com.lmx.xcall.client.springboot;

import com.lmx.xcall.client.RpcProxy;
import com.lmx.xcall.client.ServiceDiscovery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by limingxin on 2017/11/24.
 */
@Configuration
@ConditionalOnClass({ServiceDiscovery.class, RpcProxy.class})
@EnableConfigurationProperties(XcallProperties.class)
public class XcallAutoConfiguration {
    @Autowired
    private XcallProperties xcallProperties;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "xcall", value = "registryAddress")
    public ServiceDiscovery serviceDiscovery() {
        return new ServiceDiscovery(xcallProperties.getRegistryAddress());
    }

    @Bean
    @ConditionalOnMissingBean
    public RpcProxy rpcProxy(ServiceDiscovery serviceDiscovery) {
        return new RpcProxy(serviceDiscovery);
    }

}
