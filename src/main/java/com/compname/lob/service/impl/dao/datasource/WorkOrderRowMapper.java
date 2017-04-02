package com.compname.lob.service.impl.dao.datasource;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.compname.lob.domain.workorder.AbstractWorkOrder;
import com.compname.lob.domain.workorder.WorkOrderRequestDate;

/**
 * WorkOrderRowMapper
 * 
 * @author vegirl1
 * @since Oct 14, 2015
 * @version $Revision$
 */
public class WorkOrderRowMapper<T extends AbstractWorkOrder> implements RowMapper<T> {

    T workOrder;

    /**
     * Class constructor.
     * 
     */
    public WorkOrderRowMapper(T workOrder) {
        this.workOrder = workOrder;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
     */
    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        this.workOrder.setExistingWorkOrderId(rs.getLong("WO_KEY"));
        do {
            if (WorkOrderRequestDate.CURRENT.equals(rs.getString("RUN_TYP"))) {
                WorkOrderRequestDate initialDate = WorkOrderRequestDate.createWith(rs.getLong("RUN_KEY"),
                        rs.getString("RQST_RUN_DT"), rs.getString("RUN_TYP"), rs.getString("PRCES_RUN_DT"),
                        rs.getString("PRCES_RUN_STAT"));

                workOrder.setExistingInitialRequestRunDate(initialDate);
            } else if (WorkOrderRequestDate.DELTA.equals(rs.getString("RUN_TYP"))) {
                WorkOrderRequestDate deltaDate = WorkOrderRequestDate.createWith(rs.getLong("RUN_KEY"),
                        rs.getString("RQST_RUN_DT"), rs.getString("RUN_TYP"), rs.getString("PRCES_RUN_DT"),
                        rs.getString("PRCES_RUN_STAT"));
                workOrder.getExistingDeltaRequestRunDates().add(deltaDate);
            }
        } while (rs.next());
        return workOrder;
    }
}
