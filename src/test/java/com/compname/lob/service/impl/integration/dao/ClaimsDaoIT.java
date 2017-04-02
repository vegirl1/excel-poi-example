package com.compname.lob.service.impl.integration.dao;

import java.sql.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import com.compname.lob.beans.SpringConfiguration;
import com.compname.lob.domain.config.ClaimsConfig;
import com.compname.lob.service.impl.dao.claims.ClaimsDao;

/**
 * ClaimsDaoIT
 * 
 * @author vegirl1
 * @since Aug 11, 2015
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringConfiguration.class })
public class ClaimsDaoIT implements InitializingBean {

    @Autowired
    private ClaimsDao claimsDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.assertNotNull("claimsDao bean can't be null", claimsDao);
    }

    @Test
    public void testSetWorkOrderProcessStatusToError() throws ServiceException {
        claimsDao.setWorkOrderProcessStatusToError(new Long(888889), ClaimsConfig.INITIAL);
        Assert.assertTrue(true);
    }

    @Test
    public void testGetCompassBatchDateFromDW() throws ServiceException {
        Date date = claimsDao.getCompassBatchDate();
        Assert.assertNotNull(date);
    }

    @Test
    public void testLogError() throws ServiceException {
        claimsDao
                .logError(new Long(888889), "ClaimsDao Integration Test Error Message", "testLogError", "INITIAL_PREDETERMINATION");
        Assert.assertTrue(true);
    }
}
