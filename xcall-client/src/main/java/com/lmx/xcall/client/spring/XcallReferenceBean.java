package com.lmx.xcall.client.spring;

import com.google.common.collect.Lists;
import com.lmx.xcall.client.RpcProxy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by limingxin on 2017/8/14.
 */
public class XcallReferenceBean implements FactoryBean, ApplicationContextAware, InitializingBean {
    Class classObj;
    RpcProxy proxy;
    Object object;

    public Class getClassObj() {
        return classObj;
    }

    public void setClassObj(Class classObj) {
        this.classObj = classObj;
    }

    @Override
    public Object getObject() throws Exception {
        if (object == null) {
            proxy.getServiceDiscovery().subScribe(Lists.newArrayList(classObj.getName()));
            return object = proxy.create(classObj);
        }
        return object;
    }

    @Override
    public Class<?> getObjectType() {
        return classObj;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        proxy = (RpcProxy) applicationContext.getBean("rpcProxy");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        getObject();
    }
}
