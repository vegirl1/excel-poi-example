package com.compname.lob.service.impl.dao.eligibility;

import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.compname.lob.domain.config.AbstractProperties;
import com.compname.lob.domain.workorder.AbstractWorkOrder;
import com.compname.lob.domain.workorder.ClaimsWorkOrder;
import com.compname.lob.domain.workorder.DrugClaimsWorkOrder;
import com.compname.lob.domain.workorder.EligibilityWorkOrder;
import com.compname.lob.domain.workorder.WorkOrderRequestDate;
import com.compname.lob.service.impl.dao.datasource.AbstractConversionDaoImpl;
import com.compname.lob.utils.SqlDateUtils;
import com.compname.lob.utils.WorkOrderRequestDateUtils;

/**
 * WorkOrderDao Implementations
 * 
 * @author vegirl1
 * @since May 26, 2015
 * @version $Revision$
 */
public class EligibilityDaoImpl extends AbstractConversionDaoImpl implements EligibilityDao {

    private static final Logger LOG                        = LoggerFactory.getLogger(EligibilityDaoImpl.class);

    private static final String SQL_MERGE_ELIG_WO          = "merge into sl_extct_wo_t wo using (select :exist_wo_key wo_pk_tmp from dual) tmp "
                                                                   + " on (wo.wo_key = tmp.wo_pk_tmp) "
                                                                   + "when matched then update set rec_stat = :rec_stat "
                                                                   + ",data_integrity_rpt_run_dt = :data_integrity_rpt_run_dt "
                                                                   + ",actv_clas_div_xcpt_rpt_run_dt = :actv_clas_div_xcpt_rpt_run_dt "
                                                                   + ",pharmacare_rpt_run_dt = :pharmacare_rpt_run_dt "
                                                                   + ",wav_prem_rpt_run_dt = :wav_prem_rpt_run_dt "
                                                                   + ",gipsy_email_report_run_dt = :gipsy_email_report_run_dt "
                                                                   + ",rerun_delta_ind = :rerun_delta_ind ,fut_trxn_rpt_run_dt = :fut_trxn_rpt_run_dt "
                                                                   + ",cert_mapping_rpt_run_dt = :cert_mapping_rpt_run_dt "
                                                                   + ",cert_cnvr_req_ind = :cert_cnvr_req_ind ,cert_auto_gen_ind = :cert_auto_gen_ind "
                                                                   + ",cert_strt_seq = :cert_strt_seq ,cli_nm = :cli_nm ,cnvr_live_dt = :cnvr_live_dt "
                                                                   + ",vload_frmt_ind = :vload_frmt_ind ,gipsy_frmt_ind = :gipsy_frmt_ind "
                                                                   + ",sag_frmt_ind = :sag_frmt_ind ,rerun_ind = :rerun_ind "
                                                                   + ",pharmacare_enrol_dt = :pharmacare_enrol_dt, slac_trmn_dt = :slac_trmn_dt "
                                                                   + ",init_rqst_run_dt = :init_rqst_run_dt, delta_rqst_run_dt = :delta_rqst_run_dt "
                                                                   + "  when not matched then insert "
                                                                   + " (wo_key ,gr_num ,rec_stat, data_integrity_rpt_run_dt ,actv_clas_div_xcpt_rpt_run_dt "
                                                                   + " ,pharmacare_rpt_run_dt ,wav_prem_rpt_run_dt ,gipsy_email_report_run_dt "
                                                                   + " ,rerun_delta_ind ,fut_trxn_rpt_run_dt ,cert_mapping_rpt_run_dt,cert_cnvr_req_ind "
                                                                   + " ,cert_auto_gen_ind ,cert_strt_seq ,cli_nm ,cnvr_live_dt ,vload_frmt_ind "
                                                                   + " ,gipsy_frmt_ind, sag_frmt_ind, rerun_ind, rec_typ "
                                                                   + " ,pharmacare_enrol_dt, slac_trmn_dt, init_rqst_run_dt, delta_rqst_run_dt ) "
                                                                   + " values(:wo_key ,:gr_num ,:rec_stat "
                                                                   + ",:data_integrity_rpt_run_dt ,:actv_clas_div_xcpt_rpt_run_dt ,:pharmacare_rpt_run_dt "
                                                                   + ",:wav_prem_rpt_run_dt ,:gipsy_email_report_run_dt, :rerun_delta_ind "
                                                                   + ",:fut_trxn_rpt_run_dt ,:cert_mapping_rpt_run_dt,:cert_cnvr_req_ind ,:cert_auto_gen_ind"
                                                                   + ",:cert_strt_seq ,:cli_nm ,:cnvr_live_dt ,:vload_frmt_ind ,:gipsy_frmt_ind ,:sag_frmt_ind "
                                                                   + ",:rerun_ind ,:rec_typ ,:pharmacare_enrol_dt, :slac_trmn_dt"
                                                                   + " , :init_rqst_run_dt, :delta_rqst_run_dt)";

    private static final String SQL_INSERT_PLAN_MAPP       = "insert into sl_elig_wo_plan_map_t (wo_plan_map_key, wo_key, slac_gr_num, "
                                                                   + " slac_acct, slac_clas, ml_cli_num, ml_loc_div, ml_clas, ml_plan, rec_typ)"
                                                                   + " values (wo_plan_map_key_seq.nextval, :wo_key, :slac_gr_num, :slac_acct, :slac_clas,"
                                                                   + " :ml_cli_num, :ml_loc_div, :ml_clas, :ml_plan, :rec_typ)";

    private static final String SQL_INSERT_CERT_MAPP       = "insert into sl_elig_wo_cert_chg_map_t (wo_cert_chg_key, wo_key, old_cert, new_cert) "
                                                                   + "values (wo_cert_chg_key_seq.nextval, :wo_key, :old_cert, :new_cert)";

    private static final String SQL_INSERT_BNFT_MAPP       = "insert into sl_elig_wo_bnft_map_t (wo_bnft_map_key, wo_key, slac_bnft_cd, "
                                                                   + " slac_bnft_desc, ml_bnft_cd, ml_bnft_desc)"
                                                                   + " values (wo_bnft_map_key_seq.nextval, :wo_key, :slac_bnft_cd, :slac_bnft_desc, "
                                                                   + " :ml_bnft_cd, :ml_bnft_desc)";

    private static final String SQL_MERGE_WO_RUN_DATE      = "merge into sl_wo_run_actv_t rd "
                                                                   + " using (select :run_key as runKey from dual) tmp on (rd.run_key = tmp.runKey) "
                                                                   + " when matched then "
                                                                   + "   update set rd.rqst_run_dt  =:rqst_run_dt, rd.prces_run_dt =:prces_run_dt "
                                                                   + "             ,rd.prces_run_stat =:prces_run_stat when not matched then "
                                                                   + " insert (run_key, wo_key, rqst_run_dt, run_typ) "
                                                                   + " values (run_key_seq.nextval, :wo_key, :rqst_run_dt, :run_typ)";

    private static final String SQL_INSERT_DRUGC_WO        = "insert into sl_extct_wo_t wo (wo_key,gr_num,cli_nm,cnvr_live_dt"
                                                                   + ",clm_bckdt_eff_dt,clm_reimbursement,clm_pay_dir,init_rqst_run_dt,rerun_ind"
                                                                   + ",delta_rqst_run_dt,rerun_delta_ind,rec_stat,rec_typ,prces_init_strt_dt"
                                                                   + ",prces_init_stat_ind) "
                                                                   + "values(:wo_key,:gr_num,:cli_nm,:cnvr_live_dt,:clm_bckdt_eff_dt,:clm_reimbursement"
                                                                   + ",:clm_pay_dir,:init_rqst_run_dt,:rerun_ind,:delta_rqst_run_dt,:rerun_delta_ind"
                                                                   + ",:rec_stat,:rec_typ,:prces_init_strt_dt,:prces_init_stat_ind)";

    private static final String SQL_INSERT_DEDUCTIBLE_MAPP = "merge into sl_clm_wo_psdeduc_t t "
                                                                   + "  using (select :gr_num exist_gr_num, :clas_num exist_clas_num from dual) tmp "
                                                                   + "     on (t.gr_num = tmp.exist_gr_num "
                                                                   + "        and  "
                                                                   + "        t.clas_num = tmp.exist_clas_num) "
                                                                   + "   when matched then  "
                                                                   + " update set t.wo_key = :wo_key "
                                                                   + "           ,t.perscrip_ddct = :perscrip_ddct "
                                                                   + "   when not matched then  "
                                                                   + " insert (wo_psd_key, wo_key, gr_num, clas_num, perscrip_ddct) "
                                                                   + " values (wo_psd_key_seq.nextval, :wo_key, :gr_num, :clas_num, :perscrip_ddct)";

    /**
     * Class constructor.
     * 
     */
    @Autowired
    public EligibilityDaoImpl(@Qualifier("dataSourceCompas") DataSource dataSource) {
        super(dataSource);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.impl.dao.eligibility.EligibilityDao#setEligibilityWorkOrder
     *      (com.compname.lob.domain.workorder.EligibilityWorkOrder)
     */
    @Override
    public void setEligibilityWorkOrder(EligibilityWorkOrder eligibilityWorkOrder) {

        MapSqlParameterSource namedParameters = new MapSqlParameterSource();

        namedParameters.addValue("wo_key", eligibilityWorkOrder.getWorkOrderId());
        namedParameters.addValue("gr_num", eligibilityWorkOrder.getSlacGroupNumber());
        namedParameters.addValue("rec_stat", AbstractWorkOrder.RECORD_STATUS_SUCCESS);

        namedParameters.addValue("init_rqst_run_dt", SqlDateUtils.stringToSqlDate(eligibilityWorkOrder.getInitialRequestRunDate()
                .getRequestRunDate(), SqlDateUtils.SQL_DATE_FORMAT));

        namedParameters.addValue("delta_rqst_run_dt", SqlDateUtils.stringToSqlDate(eligibilityWorkOrder
                .getDeltaFirstRequestRunDate().getRequestRunDate(), SqlDateUtils.SQL_DATE_FORMAT));

        namedParameters.addValue("data_integrity_rpt_run_dt",
                SqlDateUtils.stringToSqlDate(eligibilityWorkOrder.getDataIntegrityReport(), SqlDateUtils.SQL_DATE_FORMAT));

        namedParameters.addValue("actv_clas_div_xcpt_rpt_run_dt", SqlDateUtils.stringToSqlDate(
                eligibilityWorkOrder.getActiveClassAndDivExcpRprtRunDate(), SqlDateUtils.SQL_DATE_FORMAT));

        namedParameters.addValue("pharmacare_rpt_run_dt",
                SqlDateUtils.stringToSqlDate(eligibilityWorkOrder.getPharmacareRptRunDate(), SqlDateUtils.SQL_DATE_FORMAT));

        namedParameters.addValue("wav_prem_rpt_run_dt",
                SqlDateUtils.stringToSqlDate(eligibilityWorkOrder.getWaiverOfPremiumRunDate(), SqlDateUtils.SQL_DATE_FORMAT));

        namedParameters.addValue("gipsy_email_report_run_dt",
                SqlDateUtils.stringToSqlDate(eligibilityWorkOrder.getGipsyEmailReportRunDate(), SqlDateUtils.SQL_DATE_FORMAT));

        namedParameters.addValue("rerun_delta_ind", StringUtils.substring(eligibilityWorkOrder.getReRunDeltaRequest(), 0, 1)
                .toUpperCase());

        namedParameters.addValue("fut_trxn_rpt_run_dt", SqlDateUtils.stringToSqlDate(
                eligibilityWorkOrder.getFutureTransactionReportRunDate(), SqlDateUtils.SQL_DATE_FORMAT));

        namedParameters.addValue("cert_mapping_rpt_run_dt", SqlDateUtils.stringToSqlDate(
                eligibilityWorkOrder.getCertifcateMappingReportRunDate(), SqlDateUtils.SQL_DATE_FORMAT));

        namedParameters.addValue("cert_cnvr_req_ind",
                StringUtils.substring(eligibilityWorkOrder.getCertConversionRequired().toUpperCase(), 0, 1));
        namedParameters.addValue("cert_auto_gen_ind",
                StringUtils.substring(eligibilityWorkOrder.getCertAutoGenerated().toUpperCase(), 0, 1));

        namedParameters.addValue("cert_strt_seq",
                Long.valueOf(StringUtils.isEmpty(eligibilityWorkOrder.getStartingCertNumber()) ? "0" : eligibilityWorkOrder
                        .getStartingCertNumber()));

        namedParameters.addValue("cli_nm", StringUtils.substring(eligibilityWorkOrder.getClientName(), 0, 30));

        namedParameters.addValue("cnvr_live_dt",
                SqlDateUtils.stringToSqlDate(eligibilityWorkOrder.getConversionDate(), SqlDateUtils.SQL_DATE_FORMAT));

        namedParameters.addValue("slac_trmn_dt",
                SqlDateUtils.stringToSqlDate(eligibilityWorkOrder.getTerminationDate(), SqlDateUtils.SQL_DATE_FORMAT));

        namedParameters
                .addValue("vload_frmt_ind", StringUtils.substring(eligibilityWorkOrder.getVLOADFormat(), 0, 1).toUpperCase());
        namedParameters
                .addValue("gipsy_frmt_ind", StringUtils.substring(eligibilityWorkOrder.getGipsyFormat(), 0, 1).toUpperCase());
        namedParameters.addValue("sag_frmt_ind", StringUtils.substring(eligibilityWorkOrder.getSAGFormat(), 0, 1).toUpperCase());
        namedParameters.addValue("rerun_ind", StringUtils.substring(eligibilityWorkOrder.getReRunInitialRequest(), 0, 1)
                .toUpperCase());
        namedParameters.addValue("rec_typ", EligibilityWorkOrder.RECORD_TYPE_ELIG);

        namedParameters.addValue("pharmacare_enrol_dt",
                SqlDateUtils.stringToSqlDate(eligibilityWorkOrder.getPharmaCareEnrollDate(), SqlDateUtils.SQL_DATE_FORMAT));
        namedParameters.addValue("exist_wo_key", eligibilityWorkOrder.getExistingWorkOrderId());

        // add ELIG work order
        mergeWorkOrder(SQL_MERGE_ELIG_WO, namedParameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws ServiceException
     * 
     * @see com.compname.lob.service.impl.dao.eligibility.EligibilityDao#addEligibilityWorkOrderPlanMapping(com.compname.lob.domain.workorder.EligibilityWorkOrder)
     */
    @Override
    public void addEligibilityWorkOrderPlanMapping(EligibilityWorkOrder eligibilityWorkOrder) {

        Map<String, Object>[] maps = getWorkOrderPlansBatchMap(getEligWoKey(eligibilityWorkOrder),
                eligibilityWorkOrder.getPlanMaps());

        insertIntoMappingTable(SQL_INSERT_PLAN_MAPP, maps, "SL_ELIG_WO_PLAN_MAP_T");
    }

    private Map<String, Object>[] getWorkOrderPlansBatchMap(Long workOrderId, Map<String, List<List<String>>> planMaps) {

        @SuppressWarnings("unchecked")
        Map<String, Object>[] maps = new HashMap[getBatchArraySize(planMaps)];

        int i = 0;

        for (String key : planMaps.keySet()) {

            String slacGrNum = StringUtils.EMPTY;

            for (List<String> values : planMaps.get(key)) {
                Iterator<String> iterator = values.iterator();
                Map<String, Object> map = Maps.newHashMap();
                map.put("wo_key", workOrderId);

                // vegirl1; according to the functional specification the policy number is provided only in first line
                if (StringUtils.isEmpty(slacGrNum)) {
                    slacGrNum = StringUtils.leftPad(iterator.next(), AbstractProperties.SLAC_GROUP_LENGTH, "0");
                } else {
                    iterator.next();
                }

                map.put("slac_gr_num", slacGrNum);
                map.put("slac_acct", StringUtils.leftPad(iterator.next(), AbstractProperties.SLAC_DIVISION_LENGTH, "0"));
                map.put("slac_clas", StringUtils.leftPad(iterator.next(), AbstractProperties.SLAC_CLASS_LENGTH, "0"));
                map.put("ml_cli_num", StringUtils.leftPad(iterator.next(), AbstractProperties.MANU_GROUP_LENGTH, "0"));
                map.put("ml_loc_div", StringUtils.leftPad(iterator.next(), AbstractProperties.MANU_DIVISION_LENGTH, "0"));
                map.put("ml_clas", iterator.next());

                // vegirl1; according to the functional specification ML_PLAN might not be provided
                if (iterator.hasNext()) {
                    map.put("ml_plan", iterator.next());
                } else {
                    map.put("ml_plan", null);
                }

                map.put("rec_typ", StringUtils.substringAfter(key, AbstractProperties.WORK_ORDER_TABLE + AbstractProperties.DOT));
                maps[i++] = map;
            }
        }
        return maps;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.impl.dao.eligibility.EligibilityDao#addEligibilityWorkOrderCertificateMapping(com.compname.lob.domain.workorder.EligibilityWorkOrder)
     */
    @Override
    public void addEligibilityWorkOrderCertificateMapping(EligibilityWorkOrder eligibilityWorkOrder) {

        Map<String, Object>[] maps = getWorkOrderCertificatesBatchMap(getEligWoKey(eligibilityWorkOrder),
                eligibilityWorkOrder.getCertificateMaps());

        insertIntoMappingTable(SQL_INSERT_CERT_MAPP, maps, "SL_ELIG_WO_CERT_CHG_MAP_T");
    }

    /**
     * getEligWoKey
     * 
     * @param eligibilityWorkOrder
     * @return
     */
    private Long getEligWoKey(EligibilityWorkOrder eligibilityWorkOrder) {
        return (eligibilityWorkOrder.getWorkOrderId() == null ? eligibilityWorkOrder.getExistingWorkOrderId()
                : eligibilityWorkOrder.getWorkOrderId());
    }

    private Map<String, Object>[] getWorkOrderCertificatesBatchMap(Long workOrderId, Map<String, List<List<String>>> certificateMaps) {

        @SuppressWarnings("unchecked")
        Map<String, Object>[] maps = new HashMap[getBatchArraySize(certificateMaps)];

        int i = 0;
        for (String key : certificateMaps.keySet()) {
            for (List<String> values : certificateMaps.get(key)) {
                Iterator<String> iterator = values.iterator();
                Map<String, Object> map = Maps.newHashMap();
                map.put("wo_key", workOrderId);
                map.put("old_cert", StringUtils.leftPad(iterator.next(), AbstractProperties.CERTIFICATE_LENGTH, "0"));
                map.put("new_cert", StringUtils.leftPad(iterator.next(), AbstractProperties.CERTIFICATE_LENGTH, "0"));
                maps[i++] = map;
            }
        }
        return maps;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.impl.dao.eligibility.EligibilityDao#addEligibilityWorkOrderBenefitMapping(com.compname.lob.domain.workorder.EligibilityWorkOrder)
     */
    @Override
    public void addEligibilityWorkOrderBenefitMapping(EligibilityWorkOrder eligibilityWorkOrder) {

        Map<String, Object>[] maps = getWorkOrderBenefitsBatchMap(getEligWoKey(eligibilityWorkOrder),
                eligibilityWorkOrder.getBenefitMaps());

        insertIntoMappingTable(SQL_INSERT_BNFT_MAPP, maps, "SL_ELIG_WO_BNFT_MAP_T");
    }

    private Map<String, Object>[] getWorkOrderBenefitsBatchMap(Long workOrderId, Map<String, List<List<String>>> benefitMaps) {

        @SuppressWarnings("unchecked")
        Map<String, Object>[] maps = new HashMap[getBatchArraySize(benefitMaps)];

        String mlBenefitCode = StringUtils.EMPTY;
        String mlPrevBenefitDesc = StringUtils.EMPTY;

        int i = 0;
        for (String key : benefitMaps.keySet()) {
            for (List<String> values : benefitMaps.get(key)) {
                Iterator<String> iterator = values.iterator();
                Map<String, Object> map = Maps.newHashMap();
                map.put("wo_key", workOrderId);

                mlBenefitCode = StringUtils.substring(iterator.next(), 0, 5);

                if (StringUtils.isEmpty(mlBenefitCode) && i > 0) {
                    // if mlBenefitCode is null take it from previous line
                    mlBenefitCode = (maps[i - 1].get("ml_bnft_cd") == null ? StringUtils.EMPTY : maps[i - 1].get("ml_bnft_cd")
                            .toString());
                }
                map.put("ml_bnft_cd", mlBenefitCode);

                mlPrevBenefitDesc = iterator.next();
                if (StringUtils.isEmpty(mlPrevBenefitDesc) && i > 0) {
                    mlPrevBenefitDesc = (maps[i - 1].get("ml_bnft_desc") == null ? StringUtils.EMPTY : maps[i - 1].get(
                            "ml_bnft_desc").toString());
                }
                map.put("ml_bnft_desc", mlPrevBenefitDesc);

                map.put("slac_bnft_cd",
                        StringUtils.replace(StringUtils.substringBefore(iterator.next(), " "), "+", StringUtils.EMPTY));
                map.put("slac_bnft_desc", iterator.next());

                maps[i++] = map;
            }
        }
        return maps;
    }

    /**
     * getBatchArraySize()
     * 
     * @param planMaps
     * @return
     */
    private int getBatchArraySize(Map<String, List<List<String>>> mapValues) {
        int arraySize = 0;

        for (String key : mapValues.keySet()) {
            arraySize += mapValues.get(key).size();
        }
        return arraySize;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.impl.dao.eligibility.EligibilityDao#setClaimWorkOrder(com.compname.lob.domain.workorder.EligibilityWorkOrder)
     */
    @Override
    public void setClaimsWorkOrder(EligibilityWorkOrder eligibilityWorkOrder) {
        ClaimsWorkOrder workOrder = eligibilityWorkOrder.getClaimsWorkOrder();

        String insertSql = getSetClaimsWorkOrderSql(workOrder);

        MapSqlParameterSource namedParameters = new MapSqlParameterSource();

        namedParameters.addValue("wo_key", workOrder.getWorkOrderId());
        namedParameters.addValue("gr_num", workOrder.getSlacGroupNumber());
        namedParameters.addValue("cli_nm", StringUtils.substring(workOrder.getClientName(), 0, 30));
        namedParameters.addValue("cnvr_live_dt",
                SqlDateUtils.stringToSqlDate(workOrder.getConversionDate(), SqlDateUtils.SQL_DATE_FORMAT));
        namedParameters.addValue("clm_bckdt_eff_dt",
                SqlDateUtils.stringToSqlDate(workOrder.getClaimBackdatedEffectiveDate(), SqlDateUtils.SQL_DATE_FORMAT));
        namedParameters.addValue("clm_reimbursement", StringUtils.substring(workOrder.getClaimReimbursement(), 0, 1));
        namedParameters.addValue("clm_pay_dir", StringUtils.substring(workOrder.getClaimPayDirect(), 0, 1));
        namedParameters.addValue("init_rqst_run_dt", SqlDateUtils.stringToSqlDate(workOrder.getInitialRequestRunDate()
                .getRequestRunDate(), SqlDateUtils.SQL_DATE_FORMAT));
        namedParameters.addValue("rerun_ind", StringUtils.substring(workOrder.getReRunInitialRequest(), 0, 1));
        namedParameters.addValue("delta_rqst_run_dt", SqlDateUtils.stringToSqlDate(workOrder.getDeltaFirstRequestRunDate()
                .getRequestRunDate(), SqlDateUtils.SQL_DATE_FORMAT));
        namedParameters.addValue("rerun_delta_ind", StringUtils.substring(workOrder.getReRunDeltaRequest(), 0, 1));
        namedParameters.addValue("rec_stat", AbstractWorkOrder.RECORD_STATUS_SUCCESS);
        namedParameters.addValue("rec_typ", ClaimsWorkOrder.RECORD_TYPE_CLAIM);
        namedParameters.addValue("exist_wo_key", workOrder.getExistingWorkOrderId());
        namedParameters.addValue("prces_init_strt_dt", null);
        namedParameters.addValue("prces_delta_strt_dt", null);
        namedParameters.addValue("prces_init_stat_ind", null);
        namedParameters.addValue("prces_delta_stat_ind", null);

        // add CLAIM workOrder
        mergeWorkOrder(insertSql, namedParameters);
        //
        addDrugcWorkOrder(eligibilityWorkOrder, namedParameters);
    }

    /**
     * addDrugcWorkOrder
     * 
     * @param eligibilityWorkOrder
     * @param namedParameters
     */
    private void addDrugcWorkOrder(EligibilityWorkOrder eligibilityWorkOrder, MapSqlParameterSource namedParameters) {
        DrugClaimsWorkOrder drugcWorkOrder = eligibilityWorkOrder.getDrugClaimsWorkOrder();
        if (AbstractWorkOrder.YES_FLAGS_LIST.contains(namedParameters.getValue("clm_pay_dir"))
                && drugcWorkOrder.getExistingWorkOrderId() == null) {
            // add DRUGC workOrder

            if (drugcWorkOrder.getWorkOrderId() == null && drugcWorkOrder.getExistingWorkOrderId() == null) {
                // have an update for ELIG & CLAIM but insert for DRUGC
                drugcWorkOrder.setWorkOrderId(getWorkOrderId());
            }

            namedParameters.addValue("wo_key", drugcWorkOrder.getWorkOrderId());
            namedParameters.addValue("exist_wo_key", drugcWorkOrder.getExistingWorkOrderId());
            namedParameters.addValue("rec_typ", DrugClaimsWorkOrder.RECORD_TYPE_DRUGC);
            namedParameters.addValue("init_rqst_run_dt",
                    SqlDateUtils.stringToSqlDate(DrugClaimsWorkOrder.INIT_RQST_RUN_DT, SqlDateUtils.SQL_DATE_FORMAT));
            namedParameters.addValue("delta_rqst_run_dt", SqlDateUtils.sqlSysdate());
            namedParameters.addValue("prces_init_strt_dt",
                    SqlDateUtils.stringToSqlDate(DrugClaimsWorkOrder.INIT_RQST_RUN_DT, SqlDateUtils.SQL_DATE_FORMAT));
            namedParameters.addValue("prces_init_stat_ind", AbstractWorkOrder.RECORD_STATUS_SUCCESS);
            mergeWorkOrder(SQL_INSERT_DRUGC_WO, namedParameters);
        }
    }

    /**
     * mergeWorkOrder
     * 
     * @param mergeSql
     * @param namedParameters
     */
    private void mergeWorkOrder(String insertSql, MapSqlParameterSource namedParameters) {
        int updatesCount = super.getNamedParameterJdbcTemplate().update(insertSql, namedParameters);
        LOG.info("Merged '{}' row(s) into SL_EXTCT_WO_T table with a record type '{}'", updatesCount,
                namedParameters.getValue("rec_typ").toString());
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.impl.dao.eligibility.EligibilityDao#setWorkOrderRecordStatus(java.lang.Long, java.lang.String)
     */
    @Override
    public void setWorkOrderRecordStatus(Long workOrderKey, String recordStatus) {

        if (workOrderKey == null) {
            return;
        }

        LOG.debug("Set WorkOrder '{}'  Record Status to '{}'", workOrderKey, recordStatus);
        String sql = "update sl_extct_wo_t set rec_stat = :recordStatus where wo_key = :workorderkey";

        MapSqlParameterSource namedParameters = new MapSqlParameterSource();

        namedParameters.addValue("recordStatus", recordStatus);
        namedParameters.addValue("workorderkey", workOrderKey);

        super.getNamedParameterJdbcTemplate().update(sql.toString(), namedParameters);
        LOG.info("Updated Record Status for work order key '{}' into SL_EXTCT_WO_T table", workOrderKey);

    }

    /**
     * getSetClaimWorkOrderSql
     * 
     * @return
     */
    private String getSetClaimsWorkOrderSql(ClaimsWorkOrder workOrder) {

        StringBuilder sql = new StringBuilder();

        sql.append("merge into sl_extct_wo_t wo using (select :exist_wo_key wo_pk from dual) tmp on (wo.wo_key = tmp.wo_pk)"
                + " when matched then update set rec_stat = :rec_stat ,cli_nm = :cli_nm"
                + " ,cnvr_live_dt = :cnvr_live_dt, clm_bckdt_eff_dt = :clm_bckdt_eff_dt"
                + " ,clm_reimbursement = :clm_reimbursement, clm_pay_dir = :clm_pay_dir"
                + " ,init_rqst_run_dt = :init_rqst_run_dt, rerun_ind = :rerun_ind"
                + " ,delta_rqst_run_dt = :delta_rqst_run_dt , rerun_delta_ind = :rerun_delta_ind ");

        if (AbstractWorkOrder.YES_FLAGS_LIST.contains(workOrder.getReRunInitialRequest())) {
            sql.append(" ,lst_dw_clm_load_id = null ");
            sql.append(" ,prces_init_strt_dt = :prces_init_strt_dt , prces_init_stat_ind = :prces_init_stat_ind");
            sql.append(" ,prces_delta_strt_dt = :prces_delta_strt_dt , prces_delta_stat_ind = :prces_delta_stat_ind ");
        } else if (AbstractWorkOrder.YES_FLAGS_LIST.contains(workOrder.getReRunDeltaRequest())) {
            sql.append(" ,prces_delta_strt_dt = :prces_delta_strt_dt , prces_delta_stat_ind = :prces_delta_stat_ind ");
        }

        sql.append(" when not matched then insert (wo_key, gr_num, cli_nm, "
                + " cnvr_live_dt, clm_bckdt_eff_dt, clm_reimbursement, clm_pay_dir, "
                + " init_rqst_run_dt, rerun_ind, delta_rqst_run_dt,rerun_delta_ind, rec_stat, rec_typ) "
                + " values (:wo_key, :gr_num, :cli_nm, :cnvr_live_dt, :clm_bckdt_eff_dt, "
                + " :clm_reimbursement, :clm_pay_dir,  :init_rqst_run_dt, :rerun_ind, :delta_rqst_run_dt, "
                + " :rerun_delta_ind, :rec_stat, :rec_typ)");

        return sql.toString();
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.impl.dao.eligibility.EligibilityDao#deleteLoadedWorkOrderInfo(com.compname.lob.domain.workorder.EligibilityWorkOrder)
     */
    @Override
    public void deleteLoadedWorkOrderInfo(EligibilityWorkOrder workOrder) {

        if (workOrder.getExistingWorkOrderId() == null) {
            return;
        }

        SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(super.getDataSource()).withCatalogName("SL_GLH_ELIG_CNVR_WO_LOAD_PKG")
                .withProcedureName("PRC_DELETELOADEDWORKORDERINFO").withoutProcedureColumnMetaDataAccess()
                .declareParameters(new SqlParameter("PN_WORKORDERKEY", Types.NUMERIC));

        Map<String, Object> inParamMap = new HashMap<String, Object>();
        inParamMap.put("PN_WORKORDERKEY", workOrder.getExistingWorkOrderId());

        SqlParameterSource inSqlParamSource = new MapSqlParameterSource(inParamMap);
        simpleJdbcCall.execute(inSqlParamSource);

        LOG.info("Deleted Loaded WorkOrder Info for wo_key = '{}' using "
                + "Stored Proc = 'SL_GLH_ELIG_CNVR_WO_LOAD_PKG.Prc_deleteLoadedWorkOrderInfo()'",
                workOrder.getExistingWorkOrderId());

    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.impl.dao.eligibility.EligibilityDao#setEligibilityRunDates(com.compname.lob.domain.workorder.EligibilityWorkOrder)
     */
    @Override
    public void setEligibilityRunDates(EligibilityWorkOrder workOrder) {

        List<WorkOrderRequestDate> runDates = WorkOrderRequestDateUtils.buildRunDateToProcess(workOrder);

        @SuppressWarnings("unchecked")
        Map<String, Object>[] paramMaps = new HashMap[runDates.size()];
        setWorkOrderDateSqlParams(paramMaps, runDates, getEligWoKey(workOrder));

        deleteRunDateInfo(workOrder);

        SqlParameterSource[] batchList = SqlParameterSourceUtils.createBatch(paramMaps);
        int[] updatesCount = super.getNamedParameterJdbcTemplate().batchUpdate(SQL_MERGE_WO_RUN_DATE, batchList);

        LOG.info("Merged '{}' row(s) into sl_wo_run_actv_t table", updatesCount.length);
    }

    /**
     * setWorkOrderDatesSqlParams
     * 
     * @param paramList
     * @param runDates
     * @param woKey
     */
    private void setWorkOrderDateSqlParams(Map<String, Object>[] paramMaps, List<WorkOrderRequestDate> runDates, Long woKey) {
        int i = 0;
        for (WorkOrderRequestDate runDate : runDates) {
            Map<String, Object> map = Maps.newHashMap();
            map.put("wo_key", woKey);
            map.put("run_key", runDate.getRequestRunId());
            map.put("rqst_run_dt", SqlDateUtils.stringToSqlDate(runDate.getRequestRunDate(), SqlDateUtils.SQL_DATE_FORMAT));
            map.put("run_typ", runDate.getRequestRunType());
            map.put("prces_run_dt", SqlDateUtils.stringToSqlDate(runDate.getProcessedRunDate(), SqlDateUtils.SQL_DATE_FORMAT));
            map.put("prces_run_stat", runDate.getProcessedRunStatus());
            paramMaps[i++] = map;
        }
    }

    private void deleteRunDateInfo(EligibilityWorkOrder workOrder) {
        if (workOrder.getExistingWorkOrderId() == null) {
            return;
        }

        List<WorkOrderRequestDate> runDates = WorkOrderRequestDateUtils.joinRunDates(workOrder.getExistingInitialRequestRunDate(),
                workOrder.getExistingDeltaRequestRunDates());

        deleteDeltaRunDates(runDates);
        deleteExtractedMembers(runDates);

    }

    private void deleteExtractedMembers(List<WorkOrderRequestDate> runDates) {
        Iterable<WorkOrderRequestDate> deleteDates = Iterables.filter(runDates, new Predicate<WorkOrderRequestDate>() {
            public boolean apply(WorkOrderRequestDate input) {
                return input.isDeleteMemberes();
            }
        });

        // deleting members by wo_key regardless the REC_TYP (can have only CURRENT for now),
        // will keep the run_key for case if DELTA will be back

        String sql = "delete from sl_elig_extct_mbr_t t where t.wo_key = (select rd.wo_key from sl_wo_run_actv_t rd "
                + "where rd.run_key = :run_key)";

        int cntRows = deleteRunDateByKey(sql, deleteDates);
        LOG.info("Deleted '{}' row(s) from sl_elig_extct_mbr_t table", cntRows);
    }

    private void deleteDeltaRunDates(List<WorkOrderRequestDate> runDates) {
        Iterable<WorkOrderRequestDate> deleteDates = Iterables.filter(runDates, new Predicate<WorkOrderRequestDate>() {
            public boolean apply(WorkOrderRequestDate input) {
                return input.isDeleteRunDate();
            }
        });
        String sql = "delete from sl_wo_run_actv_t t where t.run_key = :run_key";
        int cntRows = deleteRunDateByKey(sql, deleteDates);
        LOG.info("Deleted '{}' row(s) from sl_wo_run_actv_t table", cntRows);
    }

    private int deleteRunDateByKey(String sql, Iterable<WorkOrderRequestDate> deleteDates) {
        int cntRows = 0;
        if (Iterables.size(deleteDates) > 0) {

            @SuppressWarnings("unchecked")
            Map<String, Object>[] paramMaps = new HashMap[Iterables.size(deleteDates)];
            int i = 0;

            for (WorkOrderRequestDate runDate : deleteDates) {
                Map<String, Object> map = Maps.newHashMap();
                map.put("run_key", runDate.getRequestRunId());
                paramMaps[i++] = map;
            }

            SqlParameterSource[] batchList = SqlParameterSourceUtils.createBatch(paramMaps);
            int[] updatesCount = super.getNamedParameterJdbcTemplate().batchUpdate(sql, batchList);
            cntRows = updatesCount.length;
        }

        return cntRows;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.impl.dao.eligibility.EligibilityDao#setWorkOrderProcessFlags(com.compname.lob.domain.workorder.EligibilityWorkOrder)
     */
    @Override
    public void setWorkOrderProcessFlags(EligibilityWorkOrder workOrder) {
        if (workOrder.getExistingWorkOrderId() == null) {
            return;
        }

        SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(super.getDataSource()).withCatalogName("SL_GLH_ELIG_CNVR_WO_LOAD_PKG")
                .withProcedureName("PRC_SETWORKORDERPROCESSFLAGS").withoutProcedureColumnMetaDataAccess()
                .declareParameters(new SqlParameter("PN_WORKORDERKEY", Types.NUMERIC));

        Map<String, Object> inParamMap = new HashMap<String, Object>();
        inParamMap.put("PN_WORKORDERKEY", workOrder.getExistingWorkOrderId());

        SqlParameterSource inSqlParamSource = new MapSqlParameterSource(inParamMap);
        simpleJdbcCall.execute(inSqlParamSource);

        LOG.info("Updated  WorkOrder PRCES_* columns for wo_key = '{}' using "
                + "Stored Proc = 'SL_GLH_ELIG_CNVR_WO_LOAD_PKG.Prc_setWorkOrderProcessFlags()'", workOrder.getExistingWorkOrderId());

    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.impl.dao.eligibility.EligibilityDao#addPerScriptDeductible(com.compname.lob.domain.workorder.EligibilityWorkOrder)
     */
    @Override
    public void addPerScriptDeductible(EligibilityWorkOrder workOrder) {

        @SuppressWarnings("unchecked")
        Map<String, Object>[] maps = new HashMap[getBatchArraySize(workOrder.getDeductibleMaps())];

        Long claimWoId = (workOrder.getClaimsWorkOrder().getExistingWorkOrderId() != null ? workOrder.getClaimsWorkOrder()
                .getExistingWorkOrderId() : workOrder.getClaimsWorkOrder().getWorkOrderId());

        int i = 0;
        for (String key : workOrder.getDeductibleMaps().keySet()) {
            for (List<String> values : workOrder.getDeductibleMaps().get(key)) {
                Iterator<String> iterator = values.iterator();
                Map<String, Object> map = Maps.newHashMap();
                map.put("wo_key", claimWoId);
                map.put("gr_num", StringUtils.defaultIfBlank(iterator.next(), null));
                map.put("clas_num", StringUtils.leftPad(iterator.next(), AbstractProperties.SLAC_CLASS_LENGTH, "0"));
                map.put("perscrip_ddct", StringUtils.substring(iterator.next(), 0, 1).toUpperCase());
                maps[i++] = map;
            }
        }

        insertIntoMappingTable(SQL_INSERT_DEDUCTIBLE_MAPP, maps, "SL_CLM_WO_PSDEDUC_T");
    }

    private void insertIntoMappingTable(String sql, Map<String, Object>[] parameterMaps, String tableName) {
        SqlParameterSource[] batchList = SqlParameterSourceUtils.createBatch(parameterMaps);
        int[] updatesCount = super.getNamedParameterJdbcTemplate().batchUpdate(sql, batchList);
        LOG.info("Inserted {} row(s) into {} table", updatesCount.length, tableName);
    }

}
