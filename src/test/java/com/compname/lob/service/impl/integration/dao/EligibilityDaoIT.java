package com.compname.lob.service.impl.integration.dao;

import java.sql.Date;
import java.util.Calendar;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import com.google.common.collect.Lists;
import com.compname.lob.beans.SpringConfiguration;
import com.compname.lob.domain.workorder.ClaimsWorkOrder;
import com.compname.lob.domain.workorder.DrugClaimsWorkOrder;
import com.compname.lob.domain.workorder.EligibilityWorkOrder;
import com.compname.lob.domain.workorder.WorkOrderRequestDate;
import com.compname.lob.service.impl.dao.eligibility.EligibilityDao;
import com.compname.lob.service.mock.MockDataBuilder;

/**
 * EligibilityDaoIT
 * 
 * @author vegirl1
 * @since Aug 4, 2015
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringConfiguration.class })
public class EligibilityDaoIT implements InitializingBean {

    @Autowired
    private EligibilityDao       eligibilityDao;

    private EligibilityWorkOrder eligibilityWorkOrder;

    @Before
    public void setUp() throws Exception {
        eligibilityWorkOrder = MockDataBuilder.mockEligibilityWorkOrder();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.assertNotNull("eligibilityDao bean can't be null", eligibilityDao);
    }

    @Test
    public void testSetEligibilityWorkOrder() throws ServiceException {

        eligibilityWorkOrder.getClaimsWorkOrder().setSlacGroupNumber(eligibilityWorkOrder.getSlacGroupNumber());

        EligibilityWorkOrder existEligWorkOrder = new EligibilityWorkOrder();

        eligibilityDao.retrieveExistingWorkOrder(eligibilityWorkOrder.getSlacGroupNumber(), EligibilityWorkOrder.RECORD_TYPE_ELIG,
                existEligWorkOrder);

        if (existEligWorkOrder.getExistingWorkOrderId() != null) {

            setEligibilityExistingWorkOrder(existEligWorkOrder);

            // update Eligibility WorkOrder
            eligibilityDao.setEligibilityWorkOrder(eligibilityWorkOrder);
            eligibilityDao.deleteLoadedWorkOrderInfo(eligibilityWorkOrder);

            eligibilityDao.addEligibilityWorkOrderPlanMapping(eligibilityWorkOrder);
            eligibilityDao.addEligibilityWorkOrderBenefitMapping(eligibilityWorkOrder);
            eligibilityDao.addEligibilityWorkOrderCertificateMapping(eligibilityWorkOrder);

            // update Claims WorkOrder
            setClaimExistingWorkOrder();
            setDrugExistingWorkOrder();

            // update Claims WorkOrder (CLAIM & DRUGC) if exists
            if (eligibilityWorkOrder.getClaimsWorkOrder().getExistingWorkOrderId() != null) {
                eligibilityDao.setClaimsWorkOrder(eligibilityWorkOrder);
            }

        } else {
            // set Eligibility WorkOrder
            eligibilityWorkOrder.setWorkOrderId(eligibilityDao.getWorkOrderId());
            eligibilityDao.setEligibilityWorkOrder(eligibilityWorkOrder);
            eligibilityDao.addEligibilityWorkOrderPlanMapping(eligibilityWorkOrder);
            eligibilityDao.addEligibilityWorkOrderBenefitMapping(eligibilityWorkOrder);
            eligibilityDao.addEligibilityWorkOrderCertificateMapping(eligibilityWorkOrder);
            // set Claims WorkOrder
            eligibilityWorkOrder.getClaimsWorkOrder().setWorkOrderId(eligibilityDao.getWorkOrderId());
            eligibilityDao.setClaimsWorkOrder(eligibilityWorkOrder);
        }
        Assert.assertTrue(true);
    }

    private void setClaimExistingWorkOrder() {
        ClaimsWorkOrder existingClaimsWorkOrder = new ClaimsWorkOrder();
        eligibilityDao.retrieveExistingWorkOrder(eligibilityWorkOrder.getSlacGroupNumber(), ClaimsWorkOrder.RECORD_TYPE_CLAIM,
                existingClaimsWorkOrder);

        eligibilityWorkOrder.getClaimsWorkOrder().setExistingWorkOrderId(existingClaimsWorkOrder.getExistingWorkOrderId());
        eligibilityWorkOrder.getClaimsWorkOrder().setExistingInitialRequestRunDate(
                existingClaimsWorkOrder.getExistingInitialRequestRunDate());

        eligibilityWorkOrder.getClaimsWorkOrder().setExistingDeltaRequestRunDates(
                Lists.newArrayList(existingClaimsWorkOrder.getExistingDeltaFirstRequestRunDate()));
        eligibilityWorkOrder.getClaimsWorkOrder().setClientName("Updated on " + Calendar.getInstance().getTime());
    }

    private void setDrugExistingWorkOrder() {
        DrugClaimsWorkOrder existingDrugWorkOrder = new DrugClaimsWorkOrder();
        eligibilityDao.retrieveExistingWorkOrder(eligibilityWorkOrder.getSlacGroupNumber(), DrugClaimsWorkOrder.RECORD_TYPE_DRUGC,
                existingDrugWorkOrder);

        eligibilityWorkOrder.getDrugClaimsWorkOrder().setExistingWorkOrderId(existingDrugWorkOrder.getExistingWorkOrderId());
        eligibilityWorkOrder.getDrugClaimsWorkOrder().setExistingInitialRequestRunDate(
                existingDrugWorkOrder.getExistingInitialRequestRunDate());
        eligibilityWorkOrder.getDrugClaimsWorkOrder().setExistingDeltaRequestRunDates(
                Lists.newArrayList(existingDrugWorkOrder.getExistingDeltaFirstRequestRunDate()));

        eligibilityWorkOrder.getDrugClaimsWorkOrder().setClientName("Updated on " + Calendar.getInstance().getTime());
    }

    private void setEligibilityExistingWorkOrder(EligibilityWorkOrder existEligWorkOrder) {
        eligibilityWorkOrder.setExistingWorkOrderId(existEligWorkOrder.getExistingWorkOrderId());
        eligibilityWorkOrder.setExistingInitialRequestRunDate(existEligWorkOrder.getExistingInitialRequestRunDate());

        eligibilityWorkOrder.setExistingDeltaRequestRunDates(existEligWorkOrder.getExistingDeltaRequestRunDates());

        eligibilityWorkOrder.setClientName("Updated on " + Calendar.getInstance().getTime());
    }

    @Test
    public void testGetCompassBatchDateFromCompas() throws ServiceException {
        Date date = eligibilityDao.getCompassBatchDate();
        Assert.assertNotNull(date);
    }

    @Test
    public void testIsValidSlacGroupNumber() throws ServiceException {
        Assert.assertTrue(eligibilityDao.isValidSlacGroupNumber("32048"));
        Assert.assertFalse(eligibilityDao.isValidSlacGroupNumber("99999"));
    }

    @Test
    public void testRetrieveExistingWorkOrder() throws ServiceException {
        EligibilityWorkOrder existEligWorkOrder = new EligibilityWorkOrder();
        eligibilityDao.retrieveExistingWorkOrder("12345", EligibilityWorkOrder.RECORD_TYPE_ELIG, existEligWorkOrder);
        Assert.assertNotNull(existEligWorkOrder);
        Assert.assertTrue(existEligWorkOrder.getInitialRequestRunDate().getRequestRunType().equals(WorkOrderRequestDate.CURRENT));
        Assert.assertTrue(existEligWorkOrder.getDeltaRequestRunDates().size() == 2);
    }

}
