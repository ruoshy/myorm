package cn.ruoshy.myorm.orm.proxy;

import cn.ruoshy.myorm.orm.annotation.Param;
import cn.ruoshy.myorm.orm.annotation.Select;
import cn.ruoshy.myorm.orm.pool.PooledConnection;
import cn.ruoshy.myorm.orm.util.SqlAnalysis;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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