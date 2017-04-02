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
import com.compname.lob.domain.config.MasterAppConfig;
import com.compname.lob.service.impl.MasterAppConversionService;

/**
 * MasterApp Service integration tests
 * 
 * @author vegirl1
 * @since Jun 1, 2015
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringConfiguration.class })
public class MasterAppConversionServiceIT implements InitializingBean {

    @Autowired
    MasterAppConversionService masterAppService;

    @Autowired
    @Qualifier("masterAppConfig")
    private MasterAppConfig    masterAppConfig;

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.assertNotNull("MasterAppService can't be null", masterAppService);
        Assert.assertNotNull("MasterAppConfig bean can't be null", masterAppConfig);
    }

    @Test
    public void testCreateResultReport() throws ServiceException {
        // vegirl1: No reports for Phase 1
        // masterAppService.createResultReport(null);
        Assert.assertTrue(true);

    }

}
