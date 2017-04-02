package com.compname.lob.service.impl.integration.dao;

import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import com.google.common.collect.Lists;
import com.compname.lob.beans.SpringConfiguration;
import com.compname.lob.domain.report.DbParameter;
import com.compname.lob.service.impl.dao.datasource.StorProcRefCursorDao;
import com.compname.lob.utils.SqlDateUtils;

/**
 * StorProcRefCursorDao Integration tests
 * 
 * @author vegirl1
 * @since Jun 15, 2015
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringConfiguration.class })
public class StorProcRefCursorDaoIT implements InitializingBean {

    @Autowired
    private StorProcRefCursorDao dwStorProcRefCursorDao;

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.assertNotNull("StorProcRefCursorDao bean can't be null", dwStorProcRefCursorDao);
    }

    @Test
    public void testExtractGroupsList() throws ServiceException {

        List<DbParameter> storProcParameters = Lists.newArrayList();
        DbParameter p1 = new DbParameter("p_rec_type", "VARCHAR", null, "CLAIM");
        DbParameter p2 = new DbParameter("p_rec_stat", "VARCHAR", null, "SUCCESS");
        DbParameter p3 = new DbParameter("po_result", "SYS_REFCURSOR", null, "SYS_REFCURSOR");
        storProcParameters.add(p1);
        storProcParameters.add(p2);
        storProcParameters.add(p3);

        List<Map<String, Object>> cursorValues = dwStorProcRefCursorDao.getRefCursorValues(
                "SL_GLH_HLT_DTL_CLM_RPT.ExtractGroupsList", storProcParameters);
        Assert.assertNotNull(cursorValues);
    }

    @Test
    public void testExtractPredetReportData() throws ServiceException {

        List<DbParameter> storProcParameters = Lists.newArrayList();
        DbParameter p1 = new DbParameter("p_wo_key", "NUMBER", null, 99999);
        DbParameter p2 = new DbParameter("p_Extraction_type", "VARCHAR", null, "INITIAL");
        DbParameter p3 = new DbParameter("p_sl_gr_num", "VARCHAR", null, "00001");
        DbParameter p4 = new DbParameter("p_init_rqst_run_dt", "DATE", null, SqlDateUtils.stringToSqlDate("20150713",
                SqlDateUtils.SQL_DATE_FORMAT));
        DbParameter p5 = new DbParameter("po_result", "SYS_REFCURSOR", null, "SYS_REFCURSOR");
        storProcParameters.add(p1);
        storProcParameters.add(p2);
        storProcParameters.add(p3);
        storProcParameters.add(p4);
        storProcParameters.add(p5);

        List<Map<String, Object>> cursorValues = dwStorProcRefCursorDao.getRefCursorValues(
                "SL_GLH_HLT_DTL_CLM_RPT.ExtractPredetReportData", storProcParameters);
        Assert.assertNotNull(cursorValues);
        // should have at least one row with a column headers
        Assert.assertTrue(cursorValues.size() > 0);
    }

    @Test
    public void testExtractClaimRevesalReportData() throws ServiceException {

        List<DbParameter> storProcParameters = Lists.newArrayList();
        DbParameter p1 = new DbParameter("p_wo_key", "NUMBER", null, 99999);
        DbParameter p2 = new DbParameter("p_sl_gr_num", "VARCHAR", null, "00001");
        DbParameter p3 = new DbParameter("p_init_rqst_run_dt", "DATE", null, SqlDateUtils.stringToSqlDate("20150713",
                SqlDateUtils.SQL_DATE_FORMAT));
        DbParameter p4 = new DbParameter("p_delta_rqst_run_dt", "DATE", null, SqlDateUtils.stringToSqlDate("20150713",
                SqlDateUtils.SQL_DATE_FORMAT));
        DbParameter p5 = new DbParameter("po_result", "SYS_REFCURSOR", null, "SYS_REFCURSOR");
        storProcParameters.add(p1);
        storProcParameters.add(p2);
        storProcParameters.add(p3);
        storProcParameters.add(p4);
        storProcParameters.add(p5);

        List<Map<String, Object>> cursorValues = dwStorProcRefCursorDao.getRefCursorValues(
                "SL_GLH_HLT_DTL_CLM_RPT.ExtractClaimRevesalReportData", storProcParameters);
        Assert.assertNotNull(cursorValues);
        // should have at least one row with a column headers
        Assert.assertTrue(cursorValues.size() > 0);
    }
}
