package com.compname.lob.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.compname.lob.aop.LogExceptionAspect;
import com.compname.lob.domain.config.ClaimsConfig;
import com.compname.lob.domain.config.EligibilityConfig;
import com.compname.lob.domain.config.MasterAppConfig;
import com.compname.lob.domain.workorder.ClaimsWorkOrder;
import com.compname.lob.domain.workorder.DrugClaimsWorkOrder;
import com.compname.lob.domain.workorder.EligibilityWorkOrder;
import com.compname.lob.service.NotificationService;
import com.compname.lob.service.PropertiesConfigService;
import com.compname.lob.service.WorkOrderValidationService;
import com.compname.lob.service.impl.ClaimsConversionService;
import com.compname.lob.service.impl.EligibilityConversionService;
import com.compname.lob.service.impl.MasterAppConversionService;
import com.compname.lob.service.impl.config.PropertiesConfigServiceImpl;
import com.compname.lob.service.impl.dao.claims.ClaimsDao;
import com.compname.lob.service.impl.dao.claims.ClaimsDaoImpl;
import com.compname.lob.service.impl.dao.datasource.StorProcRefCursorDao;
import com.compname.lob.service.impl.dao.datasource.StorProcRefCursorDaoImpl;
import com.compname.lob.service.impl.dao.eligibility.EligibilityDao;
import com.compname.lob.service.impl.dao.eligibility.EligibilityDaoImpl;
import com.compname.lob.service.impl.dao.masterapp.MasterappDaoImpl;
import com.compname.lob.service.impl.dao.transaction.TransactionService;
import com.compname.lob.service.impl.dao.transaction.TransactionServiceImpl;
import com.compname.lob.service.impl.mail.NotificationServiceImpl;
import com.compname.lob.service.impl.validation.ClaimValidationServiceImpl;
import com.compname.lob.service.impl.validation.DrugClaimValidationServiceImpl;
import com.compname.lob.service.impl.validation.EligibilityValidationServiceImpl;

/**
 * Java based SpringConfiguration
 * 
 * @author vegirl1
 * @since May 26, 2015
 * @version $Revision$
 */
@Configuration
@ComponentScan("ca.compname.lob.*")
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import(DataSourceConfiguration.class)
@PropertySource(value = { "classpath:conversionAppConfig.properties", "classpath:eligibilityConfig.properties",
        "classpath:masterAppConfig.properties", "classpath:claimsConfig.properties" })
public class SpringConfiguration {

    @Autowired
    Environment             env;

    @Autowired
    DataSourceConfiguration dataSourceConfig;

    @Bean
    @Lazy(value = true)
    public PlatformTransactionManager transactionManager() throws FatalException {
        return new DataSourceTransactionManager(dataSourceConfig.getDataSourceCompas());
    }

    @Bean
    @Lazy(value = true)
    public TransactionDefinition transactionDefinition() {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        definition.setReadOnly(false);
        return definition;
    }

    @Bean
    @Lazy(value = true)
    public TransactionService transactionService() throws FatalException {
        return new TransactionServiceImpl(transactionManager(), transactionDefinition());
    }

    // Stream service beans
    @Bean(name = { "eligibilityConversionService" })
    @Lazy(value = true)
    public EligibilityConversionService eligibilityConversionService() throws FatalException {
        return new EligibilityConversionService(compasStorProcRefCursorDao());
    }

    @Bean(name = { "masterappConversionService" })
    @Lazy(value = true)
    public MasterAppConversionService masterappConversionService() throws FatalException {
        return new MasterAppConversionService(compasStorProcRefCursorDao());
    }

    @Bean(name = { "claimsConversionService" })
    @Lazy(value = true)
    public ClaimsConversionService claimsConversionService() throws FatalException {
        return new ClaimsConversionService(dwStorProcRefCursorDao());
    }

    // validation beans
    @Bean(name = { "eligibilityValidation" })
    @Lazy(value = true)
    public WorkOrderValidationService<EligibilityWorkOrder> eligibilityWorkOrderValidation() throws FatalException {
        return new EligibilityValidationServiceImpl(eligibilityDao());
    }

    @Bean(name = { "claimValidation" })
    @Lazy(value = true)
    public WorkOrderValidationService<ClaimsWorkOrder> claimWorkOrderValidation() throws FatalException {
        return new ClaimValidationServiceImpl(eligibilityDao());
    }

    @Bean(name = { "drugClaimValidation" })
    @Lazy(value = true)
    public WorkOrderValidationService<DrugClaimsWorkOrder> drugClaimWorkOrderValidation() throws FatalException {
        return new DrugClaimValidationServiceImpl(eligibilityDao());
    }

    // DAO beans
    @Bean
    @Lazy(value = true)
    public EligibilityDao eligibilityDao() throws FatalException {
        return new EligibilityDaoImpl(dataSourceConfig.getDataSourceCompas());
    }

    @Bean
    @Lazy(value = true)
    public ClaimsDao claimsDao() throws FatalException {
        return new ClaimsDaoImpl(dataSourceConfig.getDataSourceDW());
    }

    @Bean
    @Lazy(value = true)
    public MasterappDaoImpl masterappDao() throws FatalException {
        return new MasterappDaoImpl(dataSourceConfig.getDataSourceCompas());
    }

    @Bean(name = { "compasStorProcRefCursorDao" })
    @Lazy(value = true)
    public StorProcRefCursorDao compasStorProcRefCursorDao() throws FatalException {
        return new StorProcRefCursorDaoImpl(dataSourceConfig.getDataSourceCompas());
    }

    @Bean(name = { "dwStorProcRefCursorDao" })
    @Lazy(value = true)
    public StorProcRefCursorDao dwStorProcRefCursorDao() throws FatalException {
        return new StorProcRefCursorDaoImpl(dataSourceConfig.getDataSourceDW());
    }

    // Property beans
    @Bean
    @Lazy(value = true)
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    @Lazy(value = true)
    public PropertiesConfigService propertiesConfigService() throws ServiceException {
        return new PropertiesConfigServiceImpl();
    }

    @Bean(name = { "eligibilityConfig" })
    @Lazy(value = true)
    public EligibilityConfig getWorkOrderConfiguration() throws ServiceException {
        return propertiesConfigService().getEligibilityConfiguration();
    }

    @Bean(name = { "masterAppConfig" })
    @Lazy(value = true)
    public MasterAppConfig getMasterAppConfiguration() throws ServiceException {
        return propertiesConfigService().getMasterAppConfiguration();
    }

    @Bean(name = { "claimsConfig" })
    @Lazy(value = true)
    public ClaimsConfig getClaimsConfiguration() throws ServiceException {
        return propertiesConfigService().getClaimsConfiguration();
    }

    // email beans
    @Bean
    @Lazy(value = false)
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(env.getProperty("email.host"));
        return mailSender;
    }

    @Bean
    @Lazy(value = true)
    public NotificationService notificationService() {
        return new NotificationServiceImpl();
    }

    @Bean
    public LogExceptionAspect logThrownException() {
        return new LogExceptionAspect();
    }

}
