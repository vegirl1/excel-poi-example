package com.compname.lob.service.impl.integration;

import java.io.File;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.compname.lob.beans.SpringConfiguration;
import com.compname.lob.domain.config.AbstractProperties;
import com.compname.lob.domain.config.EligibilityConfig;
import com.compname.lob.service.NotificationService;
import com.compname.lob.utils.WorkOrderUtils;

/**
 * WorkOrder Service integration tests
 * 
 * @author vegirl1
 * @since May 28, 2015
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringConfiguration.class })
public class NotificationServiceIT implements InitializingBean {

    @Autowired
    NotificationService       notificationService;

    @Autowired
    @Qualifier("eligibilityConfig")
    private EligibilityConfig eligibilityConfig;

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.assertNotNull("NotificationService can't be null", notificationService);
        Assert.assertNotNull("EligibilityConfig bean can't be null", eligibilityConfig);
    }

    @Test
    public void testSendEligibilityLoadResultNotification() throws ServiceException {
        List<File> processedFiles = Lists.newArrayList();
        Map<File, String> failedFiles = Maps.newLinkedHashMap();
        Map<File, String> warningFiles = Maps.newLinkedHashMap();

        String inDir = "work/eligibility/in";

        processedFiles.add(new File(FilenameUtils.concat(inDir, "file1")));
        processedFiles.add(new File(FilenameUtils.concat(inDir, "file2")));

        List<ErrorInfo> errorInfos = Lists.newArrayList();
        errorInfos.add(ErrorInfo.createWith("EligibilityValidationRules.validateSlacGroup()",
                AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR,
                "Provided Slac Group Number (12345) doesn't exist in COMPAS db."));

        ServiceException ex = new ServiceException("WorkOrder data's are not valid", errorInfos);
        failedFiles
                .put(new File(FilenameUtils.concat(inDir, "file3")), WorkOrderUtils.getErrorInfoDescriptions(ex.getErrorInfos()));

        errorInfos.add(ErrorInfo.createWith("EligibilityValidationRules.validateRequestDates()",
                AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR,
                "The Eligibility Delta Request date (20150101 ) < The Eligibility Initial date (20150801)"));
        ex = new ServiceException("WorkOrder data's are not valid", errorInfos);
        failedFiles
                .put(new File(FilenameUtils.concat(inDir, "file4")), WorkOrderUtils.getErrorInfoDescriptions(ex.getErrorInfos()));

        warningFiles.put(new File(FilenameUtils.concat(inDir, "file2")), "Following Accounts – Classes are present "
                + "in WorkOrders and are not defined in Compass system: 100 - 003, 101 - 003, "
                + "102 - 003, 200 - 002, 200 - 003, 201 - 001, 201 - 003, 202 - 002, 202 - 003");

        notificationService.sendEligibilityLoadResultNotification(eligibilityConfig, processedFiles, failedFiles, warningFiles);
        Assert.assertTrue(true);
    }
}
