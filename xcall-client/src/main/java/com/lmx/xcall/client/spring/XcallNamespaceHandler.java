package com.lmx.xcall.client.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Created by limingxin on 2017/8/14.
 */
public class XcallNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("reference", new XcallBeanDefinitionParser(XcallReferenceBean.class, false));
    }
}
