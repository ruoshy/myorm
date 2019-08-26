package cn.ruoshy.myorm.orm.util;

import cn.ruoshy.myorm.orm.factory.MapperFactoryBean;
import cn.ruoshy.myorm.orm.register.MapperScannerRegister;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RegistryBean implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {

    private static ApplicationContext context = null;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        List<String> mappers = MapperScannerRegister.getMappers();
        for (String inf : mappers) {
            try {
                Class<?> clazz = Class.forName(inf);
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
                GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();
                // bean接口类型
                definition.getPropertyValues().add("interfaceClass", clazz);
                definition.setBeanClass(MapperFactoryBean.class);
                // 根据类型注入
                definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
                beanDefinitionRegistry.registerBeanDefinition(clazz.getTypeName(), definition);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    /**
     * 获得bean容器
     */
    public static <T> Object getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }
}
