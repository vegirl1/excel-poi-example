package com.compname.lob.utils;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

/**
 * DateUtils
 * 
 * @author vegirl1
 * @since Aug 19, 2015
 * @version $Revision$
 */
public final class SqlDateUtils {

    public static final String SQL_DATE_FORMAT = "yyyyMMdd";

    public static Date stringToSqlDate(String stringDate, String dateFormat) {

        if (StringUtils.isEmpty(stringDate) || StringUtils.isEmpty(dateFormat)) {
            return null;
        }

        Date sqlDate = null;
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

        try {
            sdf.setLenient(false);
            java.util.Date date = sdf.parse(stringDate);
            sqlDate = new Date(date.getTime());
        } catch (ParseException ex) {
            sqlDate = null;
        }
        return sqlDate;
    }

    public static String sqlDateToString(Date sqlDate, String dateFormat) {

        if (sqlDate == null || StringUtils.isEmpty(dateFormat)) {
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(sqlDate);
    }

    public static Date sqlSysdate() {
        Calendar today = Calendar.getInstance();
        // Flush time
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        return new Date((today.getTime()).getTime());
    }

}
