package com.lmx.xcall.client.springboot;

import com.google.common.collect.Maps;
import com.lmx.xcall.client.spring.XcallReferenceBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 远程调用bean的注册器
 */
@Component
public class XcallRpcClientRegistry implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {
    private Map<String, Object> beans = Maps.newHashMap();

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        beans.forEach((name, bean) -> {
            RootBeanDefinition rootBeanDefinition = new RootBeanDefinition();
            rootBeanDefinition.setBeanClass(XcallReferenceBean.class);
            Class<?> cls = bean.getClass();
            RpcClient rpcClient = cls.getDeclaredAnnotation(RpcClient.class);
            String beanId = rpcClient.id();
            Class className = rpcClient.value();
            rootBeanDefinition.getPropertyValues().addPropertyValue("classObj", className);
            registry.registerBeanDefinition(beanId, rootBeanDefinition);
        });
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        beans = applicationContext.getBeansWithAnnotation(RpcClient.class);
    }
}
