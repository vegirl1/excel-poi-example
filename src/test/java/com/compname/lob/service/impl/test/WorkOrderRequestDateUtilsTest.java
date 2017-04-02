package com.compname.lob.service.impl.test;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


import com.google.common.collect.Lists;
import com.compname.lob.domain.workorder.AbstractWorkOrder;
import com.compname.lob.domain.workorder.EligibilityWorkOrder;
import com.compname.lob.domain.workorder.WorkOrderRequestDate;
import com.compname.lob.service.mock.MockDataBuilder;
import com.compname.lob.utils.WorkOrderRequestDateUtils;

/**
 * WorkOrderRequestDateUtilsTest
 * 
 * @author vegirl1
 * @since Oct 20, 2015
 * @version $Revision$
 */
@RunWith(MockitoJUnitRunner.class)
public class WorkOrderRequestDateUtilsTest {

    List<WorkOrderRequestDate> runDates;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testSetEligibilityRunDatesScen1() throws ServiceException {
        EligibilityWorkOrder workOrder = MockDataBuilder.mockEligibilityWorkOrder();
        workOrder.setReRunInitialRequest("No");

        WorkOrderRequestDate initialRequestRunDate = new WorkOrderRequestDate("20151010", WorkOrderRequestDate.CURRENT);
        List<WorkOrderRequestDate> deltaRequestRunDates = Lists.newLinkedList();

        WorkOrderRequestDate deltaDate1 = new WorkOrderRequestDate("20151015", WorkOrderRequestDate.DELTA);
        WorkOrderRequestDate deltaDate2 = new WorkOrderRequestDate("20151020", WorkOrderRequestDate.DELTA);

        deltaRequestRunDates.add(deltaDate1);
        deltaRequestRunDates.add(deltaDate2);

        workOrder.setInitialRequestRunDate(initialRequestRunDate);
        workOrder.setDeltaRequestRunDates(deltaRequestRunDates);

        runDates = WorkOrderRequestDateUtils.buildRunDateToProcess(workOrder);

        Assert.assertNotNull(workOrder);
        Assert.assertNull(workOrder.getExistingInitialRequestRunDate());
    }

    @Test
    public void testSetEligibilityRunDatesScen2() throws ServiceException {
        EligibilityWorkOrder workOrder = MockDataBuilder.mockEligibilityWorkOrder();

        workOrder.setReRunInitialRequest("Yes");
        workOrder.setExistingWorkOrderId(Long.valueOf(99));

        WorkOrderRequestDate initialRequestRunDate = new WorkOrderRequestDate("20151010", WorkOrderRequestDate.CURRENT);
        List<WorkOrderRequestDate> deltaRequestRunDates = Lists.newLinkedList();

        WorkOrderRequestDate deltaDate1 = new WorkOrderRequestDate("20151015", WorkOrderRequestDate.DELTA);
        WorkOrderRequestDate deltaDate2 = new WorkOrderRequestDate("20151020", WorkOrderRequestDate.DELTA);

        deltaRequestRunDates.add(deltaDate1);
        deltaRequestRunDates.add(deltaDate2);

        workOrder.setInitialRequestRunDate(initialRequestRunDate);
        workOrder.setDeltaRequestRunDates(deltaRequestRunDates);

        //
        WorkOrderRequestDate existingInitialRequestRunDate = WorkOrderRequestDate.createWith(Long.valueOf(1), "20151010",
                WorkOrderRequestDate.CURRENT, "20151012", AbstractWorkOrder.RECORD_STATUS_SUCCESS);

        workOrder.setExistingInitialRequestRunDate(existingInitialRequestRunDate);

        List<WorkOrderRequestDate> existingDeltaRequestRunDates = Lists.newLinkedList();

        WorkOrderRequestDate existDeltaDate1 = WorkOrderRequestDate.createWith(Long.valueOf(2), "20151015",
                WorkOrderRequestDate.DELTA, "20151018", AbstractWorkOrder.RECORD_STATUS_SUCCESS);
        WorkOrderRequestDate existDeltaDate2 = WorkOrderRequestDate.createWith(Long.valueOf(3), "20151020",
                WorkOrderRequestDate.DELTA, "20151022", AbstractWorkOrder.RECORD_STATUS_SUCCESS);

        existingDeltaRequestRunDates.add(existDeltaDate1);
        existingDeltaRequestRunDates.add(existDeltaDate2);

        workOrder.setExistingDeltaRequestRunDates(existingDeltaRequestRunDates);

        runDates = WorkOrderRequestDateUtils.buildRunDateToProcess(workOrder);

        Assert.assertNotNull(workOrder);
        Assert.assertTrue(existingInitialRequestRunDate.isDeleteMemberes());

        for (WorkOrderRequestDate existDeltaDate : workOrder.getExistingDeltaRequestRunDates()) {
            Assert.assertTrue(existDeltaDate.isDeleteMemberes());
        }

    }

    @Test
    public void testSetEligibilityRunDatesScen3() throws ServiceException {
        EligibilityWorkOrder workOrder = MockDataBuilder.mockEligibilityWorkOrder();

        workOrder.setReRunInitialRequest("No");
        workOrder.setReRunDeltaRequest("No");

        workOrder.setExistingWorkOrderId(Long.valueOf(99));

        WorkOrderRequestDate initialRequestRunDate = new WorkOrderRequestDate("20151010", WorkOrderRequestDate.CURRENT);
        List<WorkOrderRequestDate> deltaRequestRunDates = Lists.newLinkedList();

        WorkOrderRequestDate deltaDate1 = new WorkOrderRequestDate("20151015", WorkOrderRequestDate.DELTA);
        WorkOrderRequestDate deltaDate2 = new WorkOrderRequestDate("20151020", WorkOrderRequestDate.DELTA);

        deltaRequestRunDates.add(deltaDate1);
        deltaRequestRunDates.add(deltaDate2);

        workOrder.setInitialRequestRunDate(initialRequestRunDate);
        workOrder.setDeltaRequestRunDates(deltaRequestRunDates);

        //
        WorkOrderRequestDate existingInitialRequestRunDate = WorkOrderRequestDate.createWith(Long.valueOf(1), "20151010",
                WorkOrderRequestDate.CURRENT, null, null);

        workOrder.setExistingInitialRequestRunDate(existingInitialRequestRunDate);

        List<WorkOrderRequestDate> existingDeltaRequestRunDates = Lists.newLinkedList();

        WorkOrderRequestDate existDeltaDate1 = WorkOrderRequestDate.createWith(Long.valueOf(2), "20151015",
                WorkOrderRequestDate.DELTA, null, null);
        WorkOrderRequestDate existDeltaDate2 = WorkOrderRequestDate.createWith(Long.valueOf(3), "20151020",
                WorkOrderRequestDate.DELTA, null, null);

        existingDeltaRequestRunDates.add(existDeltaDate1);
        existingDeltaRequestRunDates.add(existDeltaDate2);

        workOrder.setExistingDeltaRequestRunDates(existingDeltaRequestRunDates);

        runDates = WorkOrderRequestDateUtils.buildRunDateToProcess(workOrder);

        Assert.assertNotNull(workOrder);
        Assert.assertFalse(existingInitialRequestRunDate.isDeleteMemberes());
        for (WorkOrderRequestDate existDeltaDate : workOrder.getExistingDeltaRequestRunDates()) {
            Assert.assertFalse(existDeltaDate.isDeleteMemberes());
        }
    }

    @Test
    public void testSetEligibilityRunDatesScen4() throws ServiceException {
        EligibilityWorkOrder workOrder = MockDataBuilder.mockEligibilityWorkOrder();

        workOrder.setReRunInitialRequest("No");
        workOrder.setReRunDeltaRequest("Yes");

        workOrder.setExistingWorkOrderId(Long.valueOf(99));

        WorkOrderRequestDate initialRequestRunDate = new WorkOrderRequestDate("20151010", WorkOrderRequestDate.CURRENT);
        List<WorkOrderRequestDate> deltaRequestRunDates = Lists.newLinkedList();

        WorkOrderRequestDate deltaDate1 = new WorkOrderRequestDate("20151015", WorkOrderRequestDate.DELTA);
        WorkOrderRequestDate deltaDate2 = new WorkOrderRequestDate("20151020", WorkOrderRequestDate.DELTA);

        deltaRequestRunDates.add(deltaDate1);
        deltaRequestRunDates.add(deltaDate2);

        workOrder.setInitialRequestRunDate(initialRequestRunDate);
        workOrder.setDeltaRequestRunDates(deltaRequestRunDates);

        // Case 1: The 2 work order Delta dates are equals to the existing Delta dates
        WorkOrderRequestDate existingInitialRequestRunDate = WorkOrderRequestDate.createWith(Long.valueOf(1), "20151010",
                WorkOrderRequestDate.CURRENT, "20151012", AbstractWorkOrder.RECORD_STATUS_SUCCESS);

        workOrder.setExistingInitialRequestRunDate(existingInitialRequestRunDate);

        List<WorkOrderRequestDate> existingDeltaRequestRunDates = Lists.newLinkedList();

        WorkOrderRequestDate existDeltaDate1 = WorkOrderRequestDate.createWith(Long.valueOf(2), "20151015",
                WorkOrderRequestDate.DELTA, "20151017", AbstractWorkOrder.RECORD_STATUS_SUCCESS);
        WorkOrderRequestDate existDeltaDate2 = WorkOrderRequestDate.createWith(Long.valueOf(3), "20151020",
                WorkOrderRequestDate.DELTA, "20151022", AbstractWorkOrder.RECORD_STATUS_SUCCESS);

        existingDeltaRequestRunDates.add(existDeltaDate1);
        existingDeltaRequestRunDates.add(existDeltaDate2);

        workOrder.setExistingDeltaRequestRunDates(existingDeltaRequestRunDates);

        runDates = WorkOrderRequestDateUtils.buildRunDateToProcess(workOrder);

        Assert.assertNotNull(workOrder);
        Assert.assertFalse(existingInitialRequestRunDate.isDeleteMemberes());
        Assert.assertTrue(workOrder.getExistingDeltaRequestRunDates().get(1).isDeleteMemberes());
    }

    @Test
    public void testSetEligibilityRunDatesScen5() throws ServiceException {
        EligibilityWorkOrder workOrder = MockDataBuilder.mockEligibilityWorkOrder();

        workOrder.setReRunInitialRequest("No");
        workOrder.setReRunDeltaRequest("Yes");

        workOrder.setExistingWorkOrderId(Long.valueOf(99));

        WorkOrderRequestDate initialRequestRunDate = new WorkOrderRequestDate("20151010", WorkOrderRequestDate.CURRENT);
        List<WorkOrderRequestDate> deltaRequestRunDates = Lists.newLinkedList();

        WorkOrderRequestDate deltaDate1 = new WorkOrderRequestDate("20151016", WorkOrderRequestDate.DELTA);
        WorkOrderRequestDate deltaDate2 = new WorkOrderRequestDate("20151027", WorkOrderRequestDate.DELTA);

        deltaRequestRunDates.add(deltaDate1);
        deltaRequestRunDates.add(deltaDate2);

        workOrder.setInitialRequestRunDate(initialRequestRunDate);
        workOrder.setDeltaRequestRunDates(deltaRequestRunDates);

        // Case 2: First work order Delta date is NOT equals to the existing first Delta date
        WorkOrderRequestDate existingInitialRequestRunDate = WorkOrderRequestDate.createWith(Long.valueOf(1), "20151010",
                WorkOrderRequestDate.CURRENT, "20151012", AbstractWorkOrder.RECORD_STATUS_SUCCESS);

        workOrder.setExistingInitialRequestRunDate(existingInitialRequestRunDate);

        List<WorkOrderRequestDate> existingDeltaRequestRunDates = Lists.newLinkedList();

        WorkOrderRequestDate existDeltaDate1 = WorkOrderRequestDate.createWith(Long.valueOf(2), "20151015",
                WorkOrderRequestDate.DELTA, "20151017", AbstractWorkOrder.RECORD_STATUS_SUCCESS);
        WorkOrderRequestDate existDeltaDate2 = WorkOrderRequestDate.createWith(Long.valueOf(3), "20151020",
                WorkOrderRequestDate.DELTA, "20151022", AbstractWorkOrder.RECORD_STATUS_SUCCESS);

        existingDeltaRequestRunDates.add(existDeltaDate1);
        existingDeltaRequestRunDates.add(existDeltaDate2);

        workOrder.setExistingDeltaRequestRunDates(existingDeltaRequestRunDates);

        runDates = WorkOrderRequestDateUtils.buildRunDateToProcess(workOrder);

        Assert.assertNotNull(workOrder);
        Assert.assertFalse(existingInitialRequestRunDate.isDeleteMemberes());
        Assert.assertTrue(workOrder.getExistingDeltaRequestRunDates().get(1).isDeleteMemberes());
    }

    @Test
    public void testSetEligibilityRunDatesScen6() throws ServiceException {
        EligibilityWorkOrder workOrder = MockDataBuilder.mockEligibilityWorkOrder();

        workOrder.setReRunInitialRequest("No");
        workOrder.setReRunDeltaRequest("Yes");

        workOrder.setExistingWorkOrderId(Long.valueOf(99));

        WorkOrderRequestDate initialRequestRunDate = new WorkOrderRequestDate("20151010", WorkOrderRequestDate.CURRENT);
        List<WorkOrderRequestDate> deltaRequestRunDates = Lists.newLinkedList();

        WorkOrderRequestDate deltaDate1 = new WorkOrderRequestDate("20151015", WorkOrderRequestDate.DELTA);
        WorkOrderRequestDate deltaDate2 = new WorkOrderRequestDate("20151027", WorkOrderRequestDate.DELTA);

        deltaRequestRunDates.add(deltaDate1);
        deltaRequestRunDates.add(deltaDate2);

        workOrder.setInitialRequestRunDate(initialRequestRunDate);
        workOrder.setDeltaRequestRunDates(deltaRequestRunDates);

        // Case 3: Only Second work order Delta date is NOT equals to the existing second Delta date
        WorkOrderRequestDate existingInitialRequestRunDate = WorkOrderRequestDate.createWith(Long.valueOf(1), "20151010",
                WorkOrderRequestDate.CURRENT, "20151012", AbstractWorkOrder.RECORD_STATUS_SUCCESS);

        workOrder.setExistingInitialRequestRunDate(existingInitialRequestRunDate);

        List<WorkOrderRequestDate> existingDeltaRequestRunDates = Lists.newLinkedList();

        WorkOrderRequestDate existDeltaDate1 = WorkOrderRequestDate.createWith(Long.valueOf(2), "20151015",
                WorkOrderRequestDate.DELTA, "20151017", AbstractWorkOrder.RECORD_STATUS_SUCCESS);
        WorkOrderRequestDate existDeltaDate2 = WorkOrderRequestDate.createWith(Long.valueOf(3), "20151020",
                WorkOrderRequestDate.DELTA, "20151022", AbstractWorkOrder.RECORD_STATUS_SUCCESS);

        existingDeltaRequestRunDates.add(existDeltaDate1);
        existingDeltaRequestRunDates.add(existDeltaDate2);

        workOrder.setExistingDeltaRequestRunDates(existingDeltaRequestRunDates);

        runDates = WorkOrderRequestDateUtils.buildRunDateToProcess(workOrder);

        Assert.assertNotNull(workOrder);
        Assert.assertFalse(existingInitialRequestRunDate.isDeleteMemberes());
        Assert.assertTrue(workOrder.getExistingDeltaRequestRunDates().get(1).isDeleteMemberes());
    }

    @Test
    public void testSetEligibilityRunDatesScen7() throws ServiceException {
        EligibilityWorkOrder workOrder = MockDataBuilder.mockEligibilityWorkOrder();

        workOrder.setReRunInitialRequest("No");
        workOrder.setReRunDeltaRequest("Yes");

        workOrder.setExistingWorkOrderId(Long.valueOf(99));

        WorkOrderRequestDate initialRequestRunDate = new WorkOrderRequestDate("20151010", WorkOrderRequestDate.CURRENT);
        List<WorkOrderRequestDate> deltaRequestRunDates = Lists.newLinkedList();

        WorkOrderRequestDate deltaDate1 = new WorkOrderRequestDate("20151015", WorkOrderRequestDate.DELTA);
        deltaRequestRunDates.add(deltaDate1);

        WorkOrderRequestDate deltaDate2 = new WorkOrderRequestDate("20151020", WorkOrderRequestDate.DELTA);
        deltaRequestRunDates.add(deltaDate2);

        workOrder.setInitialRequestRunDate(initialRequestRunDate);
        workOrder.setDeltaRequestRunDates(deltaRequestRunDates);

        // Case 4: Less DELTA Dates on current WorkOrder then have in DB
        WorkOrderRequestDate existingInitialRequestRunDate = WorkOrderRequestDate.createWith(Long.valueOf(1), "20151010",
                WorkOrderRequestDate.CURRENT, "20151012", AbstractWorkOrder.RECORD_STATUS_SUCCESS);

        workOrder.setExistingInitialRequestRunDate(existingInitialRequestRunDate);

        List<WorkOrderRequestDate> existingDeltaRequestRunDates = Lists.newLinkedList();

        WorkOrderRequestDate existDeltaDate1 = WorkOrderRequestDate.createWith(Long.valueOf(2), "20151015",
                WorkOrderRequestDate.DELTA, "20151017", AbstractWorkOrder.RECORD_STATUS_SUCCESS);
        WorkOrderRequestDate existDeltaDate2 = WorkOrderRequestDate.createWith(Long.valueOf(3), "20151020",
                WorkOrderRequestDate.DELTA, "20151022", AbstractWorkOrder.RECORD_STATUS_SUCCESS);

        WorkOrderRequestDate existDeltaDate3 = WorkOrderRequestDate.createWith(Long.valueOf(3), "20151025",
                WorkOrderRequestDate.DELTA, "20151027", AbstractWorkOrder.RECORD_STATUS_SUCCESS);

        existingDeltaRequestRunDates.add(existDeltaDate1);
        existingDeltaRequestRunDates.add(existDeltaDate2);
        existingDeltaRequestRunDates.add(existDeltaDate3);

        workOrder.setExistingDeltaRequestRunDates(existingDeltaRequestRunDates);

        runDates = WorkOrderRequestDateUtils.buildRunDateToProcess(workOrder);

        Assert.assertNotNull(workOrder);
        Assert.assertFalse(existingInitialRequestRunDate.isDeleteMemberes());
        Assert.assertTrue(workOrder.getExistingDeltaRequestRunDates().get(2).isDeleteRunDate());
    }

}
