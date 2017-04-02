package com.compname.lob.domain.config;

/**
 * Plan Mapping Length in DB (SLCMP_INTERF_GB.SL_ELIG_WO_PLAN_MAP_T table)
 * 
 * @author vegirl1
 * @since Sep 4, 2015
 * @version $Revision$
 */
public enum PlanMappingLength {

    slac_gr_num("0", "7"), slac_acct("1", "3"), slac_clas("2", "3"), ml_cli_num("3", "7"), ml_loc_div("4", "3"), ml_clas("5", "3"), ml_plan(
            "6", "3");

    private String tabIndex;
    private String fieldLength;

    private PlanMappingLength(String tabIndex, String fieldLength) {
        this.setTabIndex(tabIndex);
        this.setFieldLength(fieldLength);
    }

    public String getTabIndex() {
        return tabIndex;
    }

    public void setTabIndex(String tabIndex) {
        this.tabIndex = tabIndex;
    }

    public String getFieldLength() {
        return fieldLength;
    }

    public void setFieldLength(String fieldLength) {
        this.fieldLength = fieldLength;
    }

}
