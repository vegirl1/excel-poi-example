package com.compname.lob.domain.config;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * EnvironmentType
 * 
 * @author vegirl1
 * @since Sep 2, 2015
 * @version $Revision$
 */
public enum EnvironmentTypes {

    LOCAL("local"), TEST("test"), INT("int"), SYST("syst"), ACC("acc"), PROD("prod");

    private static Map<String, EnvironmentTypes> m = Maps.newHashMap();

    static {
        for (EnvironmentTypes environmentTypes : EnvironmentTypes.values()) {
            m.put(environmentTypes.getValue(), environmentTypes);
        }
    }

    private String                               value;

    private EnvironmentTypes(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public final static EnvironmentTypes getFromValue(String value) {
        return m.get(value);
    }

}
