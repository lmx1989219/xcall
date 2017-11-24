package com.lmx.xcall.client.springboot;

import com.lmx.xcall.client.RpcProxy;
import com.lmx.xcall.client.ServiceDiscovery;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import com.lmx.dis.LmxProcessor;

/**
 * Created by limingxin on 2017/11/24.
 */
@Configuration
@ConditionalOnClass({ServiceDiscovery.class, RpcProxy.class/*, LmxProcessor.class*/})
@EnableConfigurationProperties(XcallProperties.class)
public class XcallAutoConfigurationBean {
    XcallProperties xcallProperties;

    public XcallAutoConfigurationBean(XcallProperties xcallProperties) {
        this.xcallProperties = xcallProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceDiscovery serviceDiscovery() {
        return new ServiceDiscovery(xcallProperties.getRegistryAddress());
    }

    @Bean
    @ConditionalOnMissingBean
    public RpcProxy rpcProxy(ServiceDiscovery serviceDiscovery) {
        return new RpcProxy(serviceDiscovery);
    }

/*    @Bean
    @ConditionalOnMissingBean
    public LmxProcessor rpcProxy() {
        return new LmxProcessor();
    }*/

}
