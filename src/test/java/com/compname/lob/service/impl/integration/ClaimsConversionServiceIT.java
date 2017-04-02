package com.compname.lob.service.impl.integration;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import com.google.common.collect.Lists;
import com.compname.lob.beans.SpringConfiguration;
import com.compname.lob.domain.config.ClaimsConfig;
import com.compname.lob.domain.report.ReportData;
import com.compname.lob.service.impl.ClaimsConversionService;

/**
 * ClaimsConversion Service integration tests
 * 
 * @author vegirl1
 * @since Jul 28, 2015
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringConfiguration.class })
public class ClaimsConversionServiceIT implements InitializingBean {

    @Autowired
    ClaimsConversionService claimsService;

    @Autowired
    @Qualifier("claimsConfig")
    private ClaimsConfig    claimsConfig;

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.assertNotNull("ClaimsConversionService can't be null", claimsService);
        Assert.assertNotNull("ClaimsConfig bean can't be null", claimsConfig);

    }

    @Before
    public void setUp() {
        List<String> filesToCreate = Lists.newArrayList("predetermination", "reversal");
        claimsConfig.setFilesToCreate(filesToCreate);
    }

    @Test
    public void testGetReportData() throws ServiceException {
        ReportData reportData = claimsService.getInitialReportData(claimsConfig);
        Assert.assertNotNull(reportData);
    }

    @Test
    public void testCreateResultReport() throws ServiceException {
        ReportData reportData = claimsService.getInitialReportData(claimsConfig);
        Assert.assertNotNull(reportData);
        claimsService.createResultReport(reportData);
        Assert.assertTrue(true);
    }
}
