package com.compname.lob.utils;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.compname.lob.domain.workorder.AbstractWorkOrder;
import com.compname.lob.domain.workorder.EligibilityWorkOrder;
import com.compname.lob.domain.workorder.WorkOrderRequestDate;

/**
 * WorkOrderRequestDateUtils
 * 
 * @author vegirl1
 * @since Oct 20, 2015
 * @version $Revision$
 */
public class WorkOrderRequestDateUtils {

    public static boolean isEmptyExistingDeltaProcessDate(List<WorkOrderRequestDate> existingDeltaRequestRunDates) {
        Iterable<WorkOrderRequestDate> processedDates = Iterables.filter(existingDeltaRequestRunDates,
                new Predicate<WorkOrderRequestDate>() {
                    public boolean apply(WorkOrderRequestDate input) {
                        return (StringUtils.isEmpty(input.getProcessedRunDate()));
                    }
                });

        return Iterables.size(processedDates) == existingDeltaRequestRunDates.size();
    }

    public static void setDeltaRunDateToProcess(List<WorkOrderRequestDate> deltaRunDates,
            List<WorkOrderRequestDate> existingDeltaRequestRunDates) {
        int cntDelta = existingDeltaRequestRunDates.size();

        if (deltaRunDates.size() < existingDeltaRequestRunDates.size()) {
            // current WO has less delta Date that exists in DB -> set to remove them from DB
            cntDelta = deltaRunDates.size();

            for (int i = cntDelta; i < existingDeltaRequestRunDates.size(); i++) {
                existingDeltaRequestRunDates.get(i).setDeleteRunDate(true);
            }
        }

        for (int i = 0; i < cntDelta; i++) {
            deltaRunDates.get(i).setRequestRunId(existingDeltaRequestRunDates.get(i).getRequestRunId());
            if (existingDeltaRequestRunDates.get(i).isDeleteMemberes()) {
                deltaRunDates.get(i).setProcessedRunDate(null);
                deltaRunDates.get(i).setProcessedRunStatus(AbstractWorkOrder.RECORD_STATUS_UNPROCESSED);

            } else {
                deltaRunDates.get(i).setProcessedRunDate(existingDeltaRequestRunDates.get(i).getProcessedRunDate());
                deltaRunDates.get(i).setProcessedRunStatus(
                        StringUtils.defaultIfEmpty(existingDeltaRequestRunDates.get(i).getProcessedRunStatus(),
                                AbstractWorkOrder.RECORD_STATUS_UNPROCESSED));
            }
        }
    }

    // vegirl1: managing multiple DELTA dates overly complicated the processing code
    public static void setLastDeltaRunDateToProcess(List<WorkOrderRequestDate> deltaRunDates,
            List<WorkOrderRequestDate> existingDeltaRequestRunDates) {
        // 1. set what to remove
        setDeltaRunDateToProcess(deltaRunDates, existingDeltaRequestRunDates);

        int result = compareCurrentDeltaDatesWithExisting(deltaRunDates, existingDeltaRequestRunDates);

        if (result < 0) {
            for (WorkOrderRequestDate existDate : existingDeltaRequestRunDates) {
                existDate.setDeleteMemberes(true);
            }

            for (WorkOrderRequestDate lastDeltaDate : deltaRunDates) {
                lastDeltaDate.setProcessedRunDate(null);
                lastDeltaDate.setProcessedRunStatus(AbstractWorkOrder.RECORD_STATUS_UNPROCESSED);
            }

        } else if (result == 0) {
            Collections.reverse(deltaRunDates);
            for (WorkOrderRequestDate lastDeltaDate : deltaRunDates) {
                if (StringUtils.isNotEmpty(lastDeltaDate.getProcessedRunDate())) {
                    lastDeltaDate.setProcessedRunDate(null);
                    lastDeltaDate.setProcessedRunStatus(AbstractWorkOrder.RECORD_STATUS_UNPROCESSED);
                    for (WorkOrderRequestDate existDate : existingDeltaRequestRunDates) {
                        if ((existDate.getRequestRunId().compareTo(lastDeltaDate.getRequestRunId()) == 0)) {
                            existDate.setDeleteMemberes(true);
                        }
                    }
                    break;
                }
            }
        } else {
            // result > 0
            for (int i = 1; i < deltaRunDates.size(); i++) {
                if (!StringUtils.equals(deltaRunDates.get(i).getRequestRunDate(), existingDeltaRequestRunDates.get(i)
                        .getRequestRunDate())) {
                    deltaRunDates.get(i).setProcessedRunDate(null);
                    deltaRunDates.get(i).setProcessedRunStatus(AbstractWorkOrder.RECORD_STATUS_UNPROCESSED);
                    existingDeltaRequestRunDates.get(i).setDeleteMemberes(true);
                }
            }
        }
    }

    /**
     * compare Current WorkOrder Delta dates with the existing in DB
     * 
     * @return - 0 if all Current WorkOrder Delta dates are equals to the Existing
     * 
     *         a value less than 0 if first(Current Delta Date) <> first(ExistingDelta Date)
     * 
     *         a value greater than 0 if first(Current Delta Date) = first(ExistingDelta Date) and other a not equal
     * 
     */
    public static int compareCurrentDeltaDatesWithExisting(List<WorkOrderRequestDate> deltaRunDates,
            List<WorkOrderRequestDate> existingDeltaRequestRunDates) {
        int countEquals = 0;
        for (WorkOrderRequestDate runDate : deltaRunDates) {
            WorkOrderRequestDate existDeltaRun = getWorkOrderRequestDateById(runDate.getRequestRunId(),
                    existingDeltaRequestRunDates);
            if (runDate.equals(existDeltaRun)) {
                countEquals++;
            } else {
                if (countEquals == 0) {
                    return -1;
                }
            }
        }

        return (deltaRunDates.size() == countEquals) ? 0 : 1;
    }

    public static WorkOrderRequestDate getWorkOrderRequestDateById(final Long id, List<WorkOrderRequestDate> runDates) {
        Optional<WorkOrderRequestDate> runDate = Iterables.tryFind(runDates, new Predicate<WorkOrderRequestDate>() {
            public boolean apply(WorkOrderRequestDate input) {
                return input.getRequestRunId().compareTo(id) == 0;
            }
        });

        if (runDate.isPresent()) {
            return runDate.get();
        } else {
            return null;
        }
    }

    public static List<WorkOrderRequestDate> joinRunDates(WorkOrderRequestDate runDate, List<WorkOrderRequestDate> listRunDates) {

        List<WorkOrderRequestDate> runDates = Lists.newLinkedList();
        runDates.add(runDate);
        runDates.addAll(listRunDates);

        return runDates;
    }

    public static List<WorkOrderRequestDate> buildRunDateToProcess(EligibilityWorkOrder workOrder) {

        List<WorkOrderRequestDate> runDates = Lists.newArrayList();

        if (workOrder.getExistingWorkOrderId() == null) {
            runDates = joinRunDates(workOrder.getInitialRequestRunDate(), workOrder.getDeltaRequestRunDates());
        } else {
            // CURRENT RUN TYPE
            if ((AbstractWorkOrder.NO_FLAGS_LIST.contains(workOrder.getReRunInitialRequest()) && StringUtils.isEmpty(workOrder
                    .getExistingInitialRequestRunDate().getProcessedRunDate()))
                    || AbstractWorkOrder.YES_FLAGS_LIST.contains(workOrder.getReRunInitialRequest())) {
                // ReRun = 'No' and not converted Or ReRun = 'Yes' then update with WO date existing initRunDate

                WorkOrderRequestDate initDate = workOrder.getInitialRequestRunDate();
                initDate.setRequestRunId(workOrder.getExistingInitialRequestRunDate().getRequestRunId());
                initDate.setProcessedRunDate(null);
                initDate.setProcessedRunStatus(AbstractWorkOrder.RECORD_STATUS_UNPROCESSED);
                runDates.add(initDate);

                // if it's a ReRun will need to delete all data associated with that RunDate
                if (AbstractWorkOrder.YES_FLAGS_LIST.contains(workOrder.getReRunInitialRequest())) {
                    workOrder.getExistingInitialRequestRunDate().setDeleteMemberes(true);
                }
            }

            // DELTA
            List<WorkOrderRequestDate> deltaRunDates = Lists.newLinkedList();
            // If Initial ReRun = 'Yes' - need to reset all DELTA Dates
            if (AbstractWorkOrder.YES_FLAGS_LIST.contains(workOrder.getReRunInitialRequest())) {

                for (WorkOrderRequestDate existDeltaDate : workOrder.getExistingDeltaRequestRunDates()) {
                    existDeltaDate.setDeleteMemberes(true);
                }
                deltaRunDates = workOrder.getDeltaRequestRunDates();
                WorkOrderRequestDateUtils.setDeltaRunDateToProcess(deltaRunDates, workOrder.getExistingDeltaRequestRunDates());

            } else if (AbstractWorkOrder.NO_FLAGS_LIST.contains(workOrder.getReRunDeltaRequest())
                    && WorkOrderRequestDateUtils.isEmptyExistingDeltaProcessDate(workOrder.getExistingDeltaRequestRunDates())) {
                deltaRunDates = workOrder.getDeltaRequestRunDates();
                WorkOrderRequestDateUtils.setDeltaRunDateToProcess(deltaRunDates, workOrder.getExistingDeltaRequestRunDates());

            } else if (AbstractWorkOrder.YES_FLAGS_LIST.contains(workOrder.getReRunDeltaRequest())
                    && !WorkOrderRequestDateUtils.isEmptyExistingDeltaProcessDate(workOrder.getExistingDeltaRequestRunDates())) {
                deltaRunDates = workOrder.getDeltaRequestRunDates();
                WorkOrderRequestDateUtils.setLastDeltaRunDateToProcess(deltaRunDates, workOrder.getExistingDeltaRequestRunDates());
            }
            runDates.addAll(deltaRunDates);
        }

        return runDates;
    }
}
