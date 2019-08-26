package cn.ruoshy.myorm.orm.pool;

import cn.ruoshy.myorm.orm.util.RegistryBean;

import javax.sql.DataSource;
import java.sql.Connection;

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
