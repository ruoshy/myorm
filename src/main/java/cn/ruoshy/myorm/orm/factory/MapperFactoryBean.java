package cn.ruoshy.myorm.orm.factory;

import cn.ruoshy.myorm.orm.proxy.MapperProxy;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

public class MapperFactoryBean<T> implements FactoryBean<T> {

    private Class<T> interfaceClass;

    /**
     * 返回的对象实例
     */
    @Override
    @SuppressWarnings("unchecked")
    public T getObject() {
        Class<?> interfaceType = interfaceClass;
        // 动态代理Mapper接口
        Object object = Proxy.newProxyInstance(interfaceType.getClassLoader(), new Class[]{interfaceType}, new MapperProxy());
        return (T) object;
    }

    @Override
    public Class<T> getObjectType() {
        return interfaceClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setInterfaceClass(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

}