package com.compname.lob.domain.config;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * WorkOrderSheetNames
 * 
 * @author vegirl1
 * @since Aug 31, 2015
 * @version $Revision$
 */
public enum WorkOrderSheetNames {

    eligibility_work_order("Eligibility Work Order", true), certificate_mapping("Cert Conversions", true), claim_work_order(
            "Claims History", true), per_script_deductible("Per Script Deductible", true), benefit_mapping(
            "Benefit Mapping Manuconnect", false);

    private static Map<String, WorkOrderSheetNames> m = Maps.newHashMap();

    static {
        for (WorkOrderSheetNames workOrderSheetNames : WorkOrderSheetNames.values()) {
            m.put(workOrderSheetNames.getSheetName(), workOrderSheetNames);
        }
    }

    private String                                  sheetName;

    private boolean                                 isSheetMandatory;

    private WorkOrderSheetNames(String sheetName, boolean isSheetMandatory) {
        this.setSheetName(sheetName);
        this.setIsSheetMandatory(isSheetMandatory);
    }

    public final static WorkOrderSheetNames getBySheetName(String sheetName) {
        return m.get(sheetName);
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public boolean isIsSheetMandatory() {
        return this.isSheetMandatory;
    }

    public void setIsSheetMandatory(boolean isSheetMandatory) {
        this.isSheetMandatory = isSheetMandatory;
    }

}
