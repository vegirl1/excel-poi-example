package com.compname.lob.service.impl.dao.datasource;

import java.sql.Types;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import oracle.jdbc.OracleTypes;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;

import com.google.common.collect.Maps;
import com.compname.lob.domain.report.DbParameter;
import com.compname.lob.utils.DatasourceUtils;

/**
 * StorProcRefCursorDaoImpl
 * 
 * @author vegirl1
 * @since Jun 10, 2015
 * @version $Revision$
 */
public class StorProcRefCursorDaoImpl implements StorProcRefCursorDao {

    private static final Logger LOG = LoggerFactory.getLogger(StorProcRefCursorDaoImpl.class);

    private final JdbcTemplate  jdbcTemplate;

    /**
     * Class constructor.
     * 
     */
    @Autowired
    public StorProcRefCursorDaoImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.impl.dao.datasource.StorProcDao#getRefCursorValues(java.lang.String)
     */
    @Override
    public List<Map<String, Object>> getRefCursorValues(String storProcName, List<DbParameter> storProcParameters) {

        LOG.debug("Get RefCursor Values from '{}' storProc using '{}' parameters", storProcName, storProcParameters);

        RetrieveStorProcRefCursor retrieveStorProcRefCursor = new RetrieveStorProcRefCursor(jdbcTemplate, storProcName,
                storProcParameters);

        return retrieveStorProcRefCursor.retrieveCursorValues();
    }

    // inner class extending StoredProcedure allows to pass the stored procedure name
    private class RetrieveStorProcRefCursor extends StoredProcedure {

        private final Map<String, Object> parameters      = Maps.newHashMap();

        private String                    P_OUT_REFCURSOR = StringUtils.EMPTY;

        /**
         * Class constructor.
         * 
         * @param dataSource
         */
        public RetrieveStorProcRefCursor(JdbcTemplate jdbcTemplate, String storProcName, List<DbParameter> storProcParameters) {
            super(jdbcTemplate, storProcName);

            for (DbParameter param : storProcParameters) {
                if (DatasourceUtils.SYS_REFCURSOR.equals(param.getDataType())) {
                    declareParameter(new SqlOutParameter(param.getName(), OracleTypes.CURSOR));
                    P_OUT_REFCURSOR = param.getName();
                } else if (DatasourceUtils.VARCHAR.equals(param.getDataType())) {
                    declareParameter(new SqlParameter(param.getName(), Types.VARCHAR));
                    parameters.put(param.getName(), param.getValue());
                } else if (DatasourceUtils.NUMBER.equals(param.getDataType())) {
                    declareParameter(new SqlParameter(param.getName(), Types.NUMERIC));
                    parameters.put(param.getName(), param.getValue());
                } else if (DatasourceUtils.DATE.equals(param.getDataType())) {
                    declareParameter(new SqlParameter(param.getName(), Types.DATE));
                    parameters.put(param.getName(), param.getValue());
                }
            }

            compile();
        }

        public List<Map<String, Object>> retrieveCursorValues() {

            Map<String, Object> results = execute(parameters);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cursorValues = (List<Map<String, Object>>) results.get(P_OUT_REFCURSOR);

            return cursorValues;

        }
    }
}
