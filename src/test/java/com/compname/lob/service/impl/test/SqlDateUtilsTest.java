package com.compname.lob.service.impl.test;

import java.sql.Date;
import java.text.ParseException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


import com.compname.lob.utils.SqlDateUtils;

/**
 * SqlDateUtilsTest
 * 
 * @author vegirl1
 * @since Aug 3, 2015
 * @version $Revision$
 */
@RunWith(MockitoJUnitRunner.class)
public class SqlDateUtilsTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testStringToSqlDate() {

        Date sqlDate = SqlDateUtils.stringToSqlDate("20150706", SqlDateUtils.SQL_DATE_FORMAT);
        System.out.println("SQL Date is " + sqlDate);

        Assert.assertNotNull(sqlDate);
        Assert.assertTrue(sqlDate.equals(Date.valueOf("2015-07-06")));

        sqlDate = SqlDateUtils.stringToSqlDate("20151333", SqlDateUtils.SQL_DATE_FORMAT);
        Assert.assertNull(sqlDate);

        sqlDate = SqlDateUtils.stringToSqlDate("", SqlDateUtils.SQL_DATE_FORMAT);
        Assert.assertNull(sqlDate);

        sqlDate = SqlDateUtils.stringToSqlDate("20150706", "");
        Assert.assertNull(sqlDate);
    }

    @Test
    public void testSqlSysdate() throws ServiceException, ParseException {

        Date initDate = SqlDateUtils.stringToSqlDate("20150101", SqlDateUtils.SQL_DATE_FORMAT);
        Date sysdate = SqlDateUtils.sqlSysdate();
        Assert.assertTrue(initDate.compareTo(sysdate) == -1);

    }

    @Test
    public void testSqlDateToString() throws ServiceException, ParseException {

        String strDate = SqlDateUtils.sqlDateToString(SqlDateUtils.stringToSqlDate("20150101", SqlDateUtils.SQL_DATE_FORMAT),
                SqlDateUtils.SQL_DATE_FORMAT);

        Assert.assertTrue("20150101".equals(strDate));

    }
}
