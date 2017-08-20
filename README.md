# xcall
  简单服务发现服务

## 概述
  目的为理解spring的IOC和动态RPC
  
### 规范
    1.遵循spring spi规范，定义xsd文件，外部dtd
    2.服务调用方采用xml配置引入服务
    3.服务提供方采用annotation方式暴露服务
    4.rpc调用为长连接池，定时健康检查，基于zookeeper动态创建和删除连接
