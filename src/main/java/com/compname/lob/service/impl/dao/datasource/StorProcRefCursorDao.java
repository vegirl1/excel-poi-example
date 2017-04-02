package com.compname.lob.service.impl.dao.datasource;

import java.util.List;
import java.util.Map;

import com.compname.lob.domain.report.DbParameter;

/**
 * StorProcRefCursorDao
 * 
 * @author vegirl1
 * @since Jun 10, 2015
 * @version $Revision$
 */
public interface StorProcRefCursorDao {

    List<Map<String, Object>> getRefCursorValues(String storProcName, List<DbParameter> storProcParameters);

}
