## 概述
&emsp;&emsp;在SpringBoot中使用MyBatis的时候我们能够在dao接口的方法上使用注解对数据库进行操作，只需要在处理的类中注入接口就能够使用，而在我们调用dao接口的时候他其实使用了动态代理的方式获取注解中的信息来对数据进行操作。

接下来我们创建一些类来简单的实现这些功能。
##### 一、创建操作所需要的注解
###### @Select
```java
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Select {
    String value();
}
```
###### @Param
```java
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
    String value();
}
```
###### @Mapper
```java
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mapper {
}
```
###### @MapperScan
```java
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(MapperScannerRegister.class)
public @interface MapperScan {
    String[] basePackages();
}
```
##### 二、扫描自定义注解@Mapper
###### MapperScannerRegister
```java
public class MapperScannerRegister implements ImportBeanDefinitionRegistrar {
    private static List<String> mappers = new ArrayList<>();

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 获得包路径
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(MapperScan.class.getName());
        String[] basePackages = (String[]) attributes.get("basePackages");
        // 扫包
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, false);
        scanner.addIncludeFilter((metadataReader, metadataReaderFactory) -> {
            boolean flag = metadataReader.getAnnotationMetadata().hasAnnotation(Mapper.class.getName());
            if (flag) {
                // 添加@Mapper注解的类位置
                mappers.add(metadataReader.getClassMetadata().getClassName());
            }
            return false;
        });
        // 开始扫包
        scanner.scan(basePackages);
    }

    public static List<String> getMappers() {
        return mappers;
    }
}
```
##### 三、注册Mapper接口到Bean容器
###### RegistryBean
```java
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
```
###### MapperFactoryBean
```java
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
```
###### MapperProxy
```java
public class MapperProxy implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String sql = null;
        // 处理方法注解
        for (Annotation at : method.getAnnotations()) {
            if (at instanceof Select) {
                sql = ((Select) at).value();
                break;
            }
        }
        // 缓存参数 参数映射
        Map<String, Object> paramMap = new HashMap<>();
        // 处理方法参数
        Parameter[] params = method.getParameters();
        for (int i = 0; i < params.length; i++) {
            // 是否存在Param注解
            boolean existenceParamAnnotation = params[i].isAnnotationPresent(Param.class);
            if (existenceParamAnnotation) {
                Param pam = params[i].getAnnotation(Param.class);
                paramMap.put(pam.value(), args[i]);
            } else {
                String parameterName = method.getParameters()[i].getName();
                paramMap.put(parameterName, args[i]);
            }
        }
        // 填充SQL
        SqlAnalysis sa = new SqlAnalysis(sql, paramMap).handle();
        String preSql = sa.getSql();
        // 获得预编译参数
        List<Object> preParams = sa.getPreParams();
        // 获得连接
        Connection conn = PooledConnection.getConnection();
        PreparedStatement preparedStatement = null;
        try {
            // 声明预编译参数
            preparedStatement = conn.prepareStatement(preSql);
            for (int i = 1; i <= preParams.size(); i++) {
                preparedStatement.setObject(i, preParams.get(i - 1));
            }
            // 获得结果集
            ResultSet rs = preparedStatement.executeQuery();
            // 获取结果集列数
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            // 返回值是否是集合
            boolean isList = false;
            List<Object> entitys = new ArrayList<>();
            // 返回值类型
            Type returnType = method.getGenericReturnType();
            if (returnType instanceof ParameterizedType) {
                isList = true;
            }
            Object entity;
            // 填充参数
            while (rs.next()) {
                // 实例化数据库实体类
                if (isList) {
                    Type[] actualTypeArguments = ((ParameterizedType) returnType).getActualTypeArguments();
                    entity = Class.forName(actualTypeArguments[0].getTypeName()).getDeclaredConstructor().newInstance();
                } else {
                    entity = method.getReturnType().getDeclaredConstructor().newInstance();
                }
                // 注入变量值
                for (int i = 1; i <= columnCount; i++) {
                    // 字段名
                    String columnName = rsmd.getColumnName(i);
                    // 字段类型
                    int type = rsmd.getColumnType(i);
                    if (rs.getObject(i) == null) {
                        continue;
                    }
                    // 按参数类型set方法注入变量值
                    Method setMethod;
                    String fieldName = "set" + columnName;
                    switch (type) {
                        case Types.VARCHAR:
                            setMethod = entity.getClass().getMethod(fieldName, String.class);
                            setMethod.invoke(entity, rs.getString(i));
                            break;
                        case Types.INTEGER:
                            setMethod = entity.getClass().getMethod(fieldName, Integer.class);
                            setMethod.invoke(entity, rs.getInt(i));
                            break;
                        case Types.DATE:
                            setMethod = entity.getClass().getMethod(fieldName, Date.class);
                            setMethod.invoke(entity, rs.getDate(i));
                            break;
                    }
                }
                // 添加到实体类列表
                entitys.add(entity);
            }
            // 输出实体类列表或实体类
            if (isList) {
                return entitys;
            } else {
                return entitys.get(0);
            }
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

}
```
##### 四、创建工具类
###### SqlAnalysis
```java
public class SqlAnalysis {
    private String sql = null;
    private Map<String, Object> paramMap = null;
    // 预编译参数列表
    private List<Object> preParams = null;

    /**
     * @param sql      SQL语句
     * @param paramMap 参数映射
     */
    public SqlAnalysis(String sql, Map<String, Object> paramMap) {
        this.sql = sql;
        this.paramMap = paramMap;
        this.preParams = new ArrayList<>();
    }

    /**
     * 拼接SQL
     */
    public void boundSql() throws Exception {
        String symbol = null;
        // 参数名
        String paramName = null;
        // 处理替换参数
        Pattern pattern = Pattern.compile("\\$\\{.*?\\}");
        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {
            symbol = matcher.group();
            paramName = symbol.substring(2, symbol.length() - 1);
            // 获得参数
            Object param = getParam(paramName);
            // 处理参数
            if (String.class.equals(param.getClass())) {
                param = "'" + param + "'";
            }
            // 替换标记为参数
            this.sql = this.sql.replace(symbol, param.toString());
            // 从参数映射删除参数
            paramMap.remove(paramName);
        }
        // 处理预编译参数
        pattern = Pattern.compile("#\\{.*?\\}");
        matcher = pattern.matcher(sql);
        while (matcher.find()) {
            symbol = matcher.group();
            paramName = symbol.substring(2, symbol.length() - 1);
            // 获得参数
            Object param = getParam(paramName);
            // 替换标记为预编译标记
            this.sql = this.sql.replace(symbol, "?");
            // 添加到预编译参数列表
            this.preParams.add(param);
        }
    }


    private Object getParam(String paramName) throws Exception {
        // 获得参数
        Object param = this.paramMap.get(paramName);
        if (param == null) {
            throw new Exception("没有找到对应的参数");
        }
        return param;
    }

    /**
     * 处理拼接
     */
    public SqlAnalysis handle() throws Exception {
        boundSql();
        return this;
    }

    /**
     * 获得SQL语句
     */
    public String getSql() {
        return sql;
    }


    /**
     * 获得预编译参数列表
     */
    public List<Object> getPreParams() {
        return preParams;
    }

}
```
###### PooledConnection
```java
public class PooledConnection {
    private static DataSource dataSource = null;

    public static Connection getConnection() throws Exception {
        if (dataSource == null) {
            dataSource = getDataSource();
        }
        return dataSource.getConnection();
    }

    private static DataSource getDataSource() {
        dataSource = (DataSource) RegistryBean.getBean(DataSource.class);
        return dataSource;
    }
}

```
##### 五、创建Mapper类比添加相应注解
```java
@Mapper
public interface StoreMapper {

    @Select("SELECT * FROM Store WHERE Store_Name=#{name}")
    Store findByName(String name);
}
```

##### 六、在启动类上添加@MapperScan填入Mapper接口包路径
```java
@MapperScan(basePackages = {"cn.ruoshy.myorm.mapper"})
@SpringBootApplication
public class MyormApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyormApplication.class, args);
    }

}
```

##### 七、测试
```java
@RestController
public class DemoController {

    @Resource
    private StoreMapper storeMapper;

    @RequestMapping("/store")
    public String getStore() {
        return JSON.toJSONString(storeMapper.findByName("Apple Store 官方旗舰店"));
    }

}
```
![image.png](https://upload-images.jianshu.io/upload_images/18713780-6a27beb32f58d717.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/600)