package com.lmx.xcall.client.spring;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Created by limingxin on 2017/8/14.
 */
public class XcallBeanDefinitionParser implements BeanDefinitionParser {

    private final Class<?> beanClass;

    private final boolean required;

    public XcallBeanDefinitionParser(Class<?> beanClass, boolean required) {
        this.beanClass = beanClass;
        this.required = required;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        RootBeanDefinition rootBeanDefinition = new RootBeanDefinition();
        rootBeanDefinition.setBeanClass(beanClass);
        BeanDefinitionRegistry factory = parserContext.getRegistry();
        String id = element.getAttribute("id");
        String interface_ = element.getAttribute("interface");
        factory.registerBeanDefinition(id, rootBeanDefinition);
        try {
            rootBeanDefinition.getPropertyValues().addPropertyValue("classObj", Class.forName(interface_));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return rootBeanDefinition;
    }
}
