package com.compname.lob.service.impl.integration;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import com.compname.lob.beans.SpringConfiguration;
import com.compname.lob.domain.config.ClaimsConfig;
import com.compname.lob.domain.config.EligibilityConfig;
import com.compname.lob.domain.config.MasterAppConfig;
import com.compname.lob.service.PropertiesConfigService;
import com.compname.lob.utils.ValidationUtils;

/**
 * PropertiesConfigServiceIT
 * 
 * @author vegirl1
 * @since Jun 16, 2015
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringConfiguration.class })
public class PropertiesConfigServiceIT implements InitializingBean {

    @Autowired
    @Qualifier("propertiesConfigService")
    private PropertiesConfigService propertiesConfigService;

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.assertNotNull("PropertiesConfigService can't be null", propertiesConfigService);
    }

    @Test
    public void testGetEligibilityConfiguration() throws ServiceException {
        EligibilityConfig config = propertiesConfigService.getEligibilityConfiguration();
        Assert.assertNotNull(config);
        Assert.assertTrue(config.getStreamName().equals(EligibilityConfig.STREAM_NAME));
        ValidationUtils.validateStreamProperties(config);
        Assert.assertTrue(true);
    }

    @Test
    public void testGetMasterAppConfiguration() throws ServiceException {
        MasterAppConfig config = propertiesConfigService.getMasterAppConfiguration();
        Assert.assertNotNull(config);
        Assert.assertTrue(config.getStreamName().equals(MasterAppConfig.STREAM_NAME));
        ValidationUtils.validateStreamProperties(config);
        Assert.assertTrue(true);
    }

    @Test
    public void testGetClaimsConfiguration() throws ServiceException {
        ClaimsConfig config = propertiesConfigService.getClaimsConfiguration();
        Assert.assertNotNull(config);
        Assert.assertTrue(config.getStreamName().equals(ClaimsConfig.STREAM_NAME));
        Assert.assertTrue(config.getFileNames().size() > 2);
        ValidationUtils.validateStreamProperties(config);
        Assert.assertTrue(true);
    }
}
