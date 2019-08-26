package cn.ruoshy.myorm.orm.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public void setSql(String sql) {
        this.sql = sql;
    }

    /**
     * 获得预编译参数列表
     */
    public List<Object> getPreParams() {
        return preParams;
    }

    public void setPreParams(List<Object> preParam) {
        this.preParams = preParam;
    }

}