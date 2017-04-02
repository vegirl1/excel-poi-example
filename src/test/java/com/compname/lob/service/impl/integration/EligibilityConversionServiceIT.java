package com.compname.lob.service.impl.integration;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import com.compname.lob.beans.SpringConfiguration;
import com.compname.lob.domain.config.EligibilityConfig;
import com.compname.lob.service.impl.EligibilityConversionService;

/**
 * WorkOrder Service integration tests
 * 
 * @author vegirl1
 * @since May 28, 2015
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringConfiguration.class })
public class EligibilityConversionServiceIT implements InitializingBean {

    @Autowired
    EligibilityConversionService eligibilityService;

    @Autowired
    @Qualifier("eligibilityConfig")
    private EligibilityConfig    eligibilityConfig;

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.assertNotNull("EligibilityService can't be null", eligibilityService);
        Assert.assertNotNull("EligibilityConfig bean can't be null", eligibilityConfig);
    }

    @Test
    public void testGetWorkOrderFiles() throws ServiceException {
        eligibilityConfig.setInputPath("src/test/resources/sample-workorder/eligibility");
        List<File> files = eligibilityService.getWorkOrderFiles(eligibilityConfig);
        Assert.assertNotNull(files);
        Assert.assertTrue(files.size() > 0);
    }

    @Test
    public void testProcessWorkOrder() throws ServiceException {
        // !!! to test should have a WorkOrder here eligibility.in.path=${rootPath}/temp/eligibility/in
        List<File> files = eligibilityService.getWorkOrderFiles(eligibilityConfig);
        Assert.assertNotNull(files);
        eligibilityService.processWorkOrder(files);
        Assert.assertTrue(true);
    }

    @Test
    public void testCreateEligibilityResultReport() throws ServiceException {
        // vegirl1: No reports for Phase 1
        // eligibilityService.createResultReport(eligibilityConfig);
        Assert.assertTrue(true);
    }

}
