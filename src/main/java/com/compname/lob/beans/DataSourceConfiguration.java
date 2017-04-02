package com.compname.lob.beans;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;


import com.compname.lob.utils.ValidationUtils;

/**
 * java based DataSourceConfigurations
 * 
 * @author vegirl1
 * @since May 26, 2015
 * @version $Revision$
 */
@Configuration
public class DataSourceConfiguration {

    @Autowired
    private Environment             env;

    private final String            runEnv;
    private static final Properties CONNECTION_PROPERTIES = new Properties();

    public DataSourceConfiguration() {
        runEnv = ValidationUtils.getRuntimeEnvironment();
    }

    static {
        CONNECTION_PROPERTIES.setProperty("defaultAutoCommit", "false");
        CONNECTION_PROPERTIES.setProperty("maxActive", "50");
        CONNECTION_PROPERTIES.setProperty("maxIdle", "20");
    }

    @Bean(name = { "dataSourceCompas" })
    public DataSource getDataSourceCompas() throws FatalException {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        ds.setUrl(env.getProperty(runEnv + ".giCompasDb.url"));
        ds.setUsername(getGiCompasDbUserName());
        ds.setPassword(getGiCompasDbPassword());
        ds.setConnectionProperties(CONNECTION_PROPERTIES);
        return ds;
    }

    @Bean(name = { "dataSourceDW" })
    public DataSource getDataSourceDW() throws FatalException {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        ds.setUrl(env.getProperty(runEnv + ".giDWDb.url"));
        ds.setUsername(getGiDWDbUserName());
        ds.setPassword(getGiDWDbPassword());
        return ds;
    }

    // NetSyst(UserName, Password reader)
    private SystemUserManager getSystemUserManager() throws FatalException {
        return new SystemUserManager();
    }

    private SystemUser getGiCompasDbSystemUser() throws FatalException {
        return getSystemUserManager().getSystemUser(env.getProperty(runEnv + ".security.giCompasDb.credentials"));
    }

    private String getGiCompasDbUserName() throws FatalException {
        return getGiCompasDbSystemUser().getLogin();
    }

    private String getGiCompasDbPassword() throws FatalException {
        return getGiCompasDbSystemUser().getPassword();
    }

    private SystemUser getGiDWDbSystemUser() throws FatalException {
        return getSystemUserManager().getSystemUser(env.getProperty(runEnv + ".security.giDWDb.credentials"));
    }

    private String getGiDWDbUserName() throws FatalException {
        return getGiDWDbSystemUser().getLogin();
    }

    private String getGiDWDbPassword() throws FatalException {
        return getGiDWDbSystemUser().getPassword();
    }
}
