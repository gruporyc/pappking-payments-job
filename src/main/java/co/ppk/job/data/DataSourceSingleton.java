/******************************************************************
 *
 * This code is for the Pappking service project.
 *
 *
 * © 2018, Pappking Management All rights reserved.
 *
 *
 ******************************************************************/
package co.ppk.job.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Optional;

@Component
public class DataSourceSingleton {

    private static DataSource instance = null;

    public DataSourceSingleton() {}

    public static synchronized DataSource getInstance() {
        if (instance == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(Optional.ofNullable(System.getenv("PAYMENTS_DB_URL"))
                    .orElse("jdbc:mysql://localhost:3306/ppk_payments?useSSL=false"));
            config.setUsername(Optional.ofNullable(System.getenv("PAYMENTS_JDBC_USERNAME"))
                    .orElse("ppkpaymentsuser"));
            config.setPassword(Optional.ofNullable(System.getenv("PAYMENTS_JDBC_PASSWORD"))
                    .orElse("ppkpayments"));
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("characterEncoding", "UTF-8");
            config.addDataSourceProperty("useUnicode", "true");
            config.addDataSourceProperty("serverTimezone", "UTC");

            instance = new HikariDataSource(config);
        }
        return instance;
    }
}

