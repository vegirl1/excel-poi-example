package com.compname.lob.service.impl.dao.datasource;

import java.sql.Date;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import oracle.jdbc.OracleTypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import com.compname.lob.domain.config.AbstractProperties;
import com.compname.lob.domain.workorder.AbstractWorkOrder;

/**
 * AbstractConversionDaoImpl
 * 
 * @author vegirl1
 * @since Aug 14, 2015
 * @version $Revision$
 */
public abstract class AbstractConversionDaoImpl implements AbstractConversionDao {

    private static final Logger              LOG = LoggerFactory.getLogger(AbstractConversionDaoImpl.class);

    private final DataSource                 dataSource;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /*
     * Class constructor.
     */
    public AbstractConversionDaoImpl(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.impl.dao.datasource.AbstractConversionDao#getCompassBatchDate()
     */
    @Override
    public Long getWorkOrderId() {

        String sql = "select wo_key_seq.nextval from dual";
        return namedParameterJdbcTemplate.getJdbcOperations().queryForObject(sql, Long.class);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.impl.dao.datasource.AbstractConversionDao#getCompassBatchDate()
     */
    @Override
    public Date getCompassBatchDate() {
        String sql = "select SL_GLH_CNVR_UTILS_PKG.Fnc_getCompassBatchDate from dual";
        return namedParameterJdbcTemplate.getJdbcOperations().queryForObject(sql, Date.class);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.impl.dao.datasource.AbstractConversionDao#logError(java.lang.Long, java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void logError(Long workOrderKey, String errorMessage, String methodName, String processName) {
        LOG.debug("Set Error for WorkOrder '{}', error message '{}', thrown in '{}'", workOrderKey, errorMessage, methodName);

        String insertSql = "insert into sl_error_t (error_key, wo_key, error_desc, package_nm, "
                + " procedure_nm, error_dt, error_type, procs_desc, batch_dt)"
                + " values(error_key_seq.nextval, :wokey, :errordesc, :pckgname, :procname, "
                + "sysdate, :errtype, :procsdesc, :batchdate)";

        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("wokey", workOrderKey);
        namedParameters.addValue("errordesc", errorMessage);
        namedParameters.addValue("pckgname", "Java Module");
        namedParameters.addValue("procname", methodName);
        namedParameters.addValue("errtype", AbstractProperties.ERROR_TYPE_ERROR);
        namedParameters.addValue("procsdesc", processName);
        namedParameters.addValue("batchdate", getCompassBatchDate());

        int updatesCount = namedParameterJdbcTemplate.update(insertSql, namedParameters);
        LOG.info("Inserted '{}' row(s) into SL_ERROR_T table", updatesCount);

    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.impl.dao.datasource.AbstractConversionDao#isValidSlacGroupNumber(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public boolean isValidSlacGroupNumber(String slacGroup) {
        String sql = "select SL_GLH_ELIG_CNVR_WO_LOAD_PKG.Fnc_checkSlacGroupExists(?) from dual";
        int result = namedParameterJdbcTemplate.getJdbcOperations().queryForObject(sql, Integer.class, slacGroup);

        return result == 1 ? true : false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.impl.dao.datasource.AbstractConversionDao#isValidSlacGroupNumber(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public boolean isSAGGroup(String slacGroup) {
        String sql = "select SL_GLH_ELIG_CNVR_WO_LOAD_PKG.Fnc_isSAGGroup(?) from dual";
        int result = namedParameterJdbcTemplate.getJdbcOperations().queryForObject(sql, Integer.class, slacGroup);

        return result == 1 ? true : false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.impl.dao.datasource.AbstractConversionDao#isWorkOrderExists(java.lang.String, java.lang.String)
     */
    @Override
    public boolean isWorkOrderExists(String slacGroup, String recordType) {
        String sql = "select SL_GLH_ELIG_CNVR_WO_LOAD_PKG.Fnc_checkWorkOrderExists(?,?) from dual";
        int result = namedParameterJdbcTemplate.getJdbcOperations().queryForObject(sql, Integer.class, slacGroup, recordType);

        return result == 1 ? true : false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.impl.dao.datasource.AbstractConversionDao#retrieveExistingWorkOrder(java.lang.String,
     *      java.lang.String, com.compname.lob.domain.workorder.AbstractWorkOrder)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends AbstractWorkOrder> void retrieveExistingWorkOrder(String slacGroup, String recordType, T existingWorkOrder) {

        if (!isWorkOrderExists(slacGroup, recordType)) {
            return;
        }

        SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(dataSource)
                .withCatalogName("SL_GLH_ELIG_CNVR_WO_LOAD_PKG")
                .withProcedureName("PRC_GETWORKORDERRUNDATES")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(new SqlParameter("PV_SLACGROUP", Types.VARCHAR),
                        new SqlParameter("PV_WORKORDERTYPE", Types.VARCHAR),
                        new SqlOutParameter("PO_WORKORDERROW", OracleTypes.CURSOR))
                .returningResultSet("PO_WORKORDERROW", new WorkOrderRowMapper<T>(existingWorkOrder));

        Map<String, Object> inParamMap = new HashMap<String, Object>();
        inParamMap.put("PV_SLACGROUP", slacGroup);
        inParamMap.put("PV_WORKORDERTYPE", recordType);

        SqlParameterSource inSqlParamSource = new MapSqlParameterSource(inParamMap);
        Map<String, Object> results = simpleJdbcCall.execute(inSqlParamSource);

        existingWorkOrder = ((List<T>) results.get("PO_WORKORDERROW")).get(0);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.impl.dao.datasource.AbstractConversionDao#getSlacAndWorkOrdPlanDiff(java.lang.String)
     */
    @Override
    public String getSlacAndWorkOrdPlanDiff(String slacGroup) {
        String sql = "select SL_GLH_ELIG_CNVR_WO_LOAD_PKG.Fnc_getSlacAndWorkOrdPlanDiff(?) from dual";
        return namedParameterJdbcTemplate.getJdbcOperations().queryForObject(sql, String.class, slacGroup);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.impl.dao.datasource.AbstractConversionDao#getCertMappingAsList(java.lang.Long)
     */
    @Override
    public String getCertMappingAsList(Long woKey) {
        String sql = "select SL_GLH_ELIG_CNVR_WO_LOAD_PKG.Fnc_getCertMappingAsClobList(?) from dual";
        return namedParameterJdbcTemplate.getJdbcOperations().queryForObject(sql, String.class, woKey);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.impl.dao.datasource.AbstractConversionDao#getDeductibleClassesAsList(java.lang.String)
     */
    @Override
    public String getDeductibleClassesAsList(String slacGroup) {
        String sql = "select SL_GLH_ELIG_CNVR_WO_LOAD_PKG.Fnc_getDeductibleClassesAsList(?) from dual";
        return namedParameterJdbcTemplate.getJdbcOperations().queryForObject(sql, String.class, slacGroup);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.impl.dao.claims.ClaimsDao#setWorkOrderProcessStatusToError(java.lang.Long, java.lang.String)
     */
    @Override
    public void setWorkOrderProcessStatusToError(Long workOrderKey, String extractionType) {
        LOG.debug("Set WorkOrder '{}'  Process Status to 'ERROR' for the extraction type '{}'", workOrderKey, extractionType);
        StringBuilder sql = new StringBuilder("update sl_extct_wo_t set ");

        if (AbstractProperties.INITIAL.equalsIgnoreCase(extractionType)) {
            sql.append(" prces_init_stat_ind = :errorCode ");
        } else if (AbstractProperties.DELTA.equalsIgnoreCase(extractionType)) {
            sql.append(" prces_delta_stat_ind = :errorCode ");
        } else if (AbstractProperties.FUTURE.equalsIgnoreCase(extractionType)) {
            // Future transactions are considered as a Delta :-( DBMS Champion and Analyst decision...
            sql.append(" prces_future_stat_ind = :errorCode ");
        } else {
            return;
        }

        sql.append("where wo_key = :workorderkey");

        MapSqlParameterSource namedParameters = new MapSqlParameterSource();

        namedParameters.addValue("errorCode", AbstractProperties.ERROR_TYPE_ERROR);
        namedParameters.addValue("workorderkey", workOrderKey);

        getNamedParameterJdbcTemplate().update(sql.toString(), namedParameters);
        LOG.info("Updated Status for work order key '{}' into SL_EXTCT_WO_T table", workOrderKey);

    }

    /**
     * Getter method of the <code>"dataSource"</code> class attribute.
     * 
     * @return the dataSource.
     */
    public DataSource getDataSource() {
        return this.dataSource;
    }

    /**
     * Getter method of the <code>"namedParameterJdbcTemplate"</code> class attribute.
     * 
     * @return the namedParameterJdbcTemplate.
     */
    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return this.namedParameterJdbcTemplate;
    }

}
