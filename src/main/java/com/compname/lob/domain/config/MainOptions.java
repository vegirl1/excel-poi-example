package com.compname.lob.domain.config;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * MainOptions
 *
 * @author vegirl1
 * @since Sep 4, 2015
 * @version $Revision$
 */
public enum MainOptions {

    OPTION_ELIGIBILITY("eligibility"), OPTION_CLAIMS("claims"), OPTION_MASTERAPP("masterapp"), OPTION_LOAD_WORKORDER(
            "load_workorder"), OPTION_CLAIM_PREDETERMINATION("predetermination"), OPTION_CLAIM_REVERSAL("reversal"), OPTION_OUTPUT_REPORTS(
            "output_reports");

    private static Map<String, MainOptions> m = Maps.newHashMap();

    static {
        for (MainOptions mainOptions : MainOptions.values()) {
            m.put(mainOptions.getValue(), mainOptions);
        }
    }

    private String                          value;

    private MainOptions(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public final static MainOptions getFromValue(String value) {
        return m.get(value);
    }
}
