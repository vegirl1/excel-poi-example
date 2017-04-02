package com.compname.lob.utils;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.compname.lob.domain.config.AbstractProperties;
import com.compname.lob.domain.report.DbParameter;

/**
 * DatasourceUtils
 * 
 * @author vegirl1
 * @since Jul 28, 2015
 * @version $Revision$
 */
public final class DatasourceUtils {

    public static final String SYS_REFCURSOR    = "SYS_REFCURSOR";
    public static final String VARCHAR          = "VARCHAR";
    public static final String NUMBER           = "NUMBER";
    public static final String DATE             = "DATE";

    public static final String OPEN_PARENTHESE  = "(";
    public static final String CLOSE_PARENTHESE = ")";

    public static final String OPEN_BRACKET     = "[";
    public static final String CLOSE_BRACKET    = "]";

    public static final String MAIN_CURSOR      = "maincursor";
    public static final String MAIN_OPTION      = "mainOption";

    public static String getStorProcName(String storProcSignature) {
        return StringUtils.substringBefore(storProcSignature, OPEN_PARENTHESE);
    }

    public static List<DbParameter> getStorProcParameters(String storProcSignature, List<DbParameter> parameters) {
        // result list
        List<DbParameter> spParameterValues = Lists.newArrayList();

        String spParameter = StringUtils.substringAfter(storProcSignature, OPEN_PARENTHESE);
        spParameter = StringUtils.remove(spParameter, CLOSE_PARENTHESE);

        if (StringUtils.isNotEmpty(spParameter)) {
            List<String> spParameters = Lists.newLinkedList(Arrays.asList(spParameter.split(AbstractProperties.COMMA)));
            for (final String s : spParameters) {
                // if parameter is not present it will throw an exception
                DbParameter value = Iterables.find(parameters, new Predicate<DbParameter>() {
                    public boolean apply(DbParameter input) {
                        return StringUtils.equalsIgnoreCase(s, input.getName());
                    };
                });

                spParameterValues.add(value);
            }
        }
        return spParameterValues;
    }

    public static <T extends AbstractProperties> void setDbParameterValues(T config, Map<String, Object> values) {
        for (DbParameter param : config.getParameters()) {
            Object value = null;

            if (StringUtils.contains(param.getValueSource(), MAIN_CURSOR)) {
                value = values.get(StringUtils.substringAfter(param.getValueSource(), MAIN_CURSOR + AbstractProperties.DOT));
            } else if (StringUtils.contains(param.getValueSource(), MAIN_OPTION)) {
                value = config.getMainOptions().get(param.getValueSource());
            } else {
                value = param.getValue();
            }

            param.setValue(value);
        }
    }

    private static Map<String, String> getParameterValues(List<DbParameter> parameters) {
        Map<String, String> paramValues = Maps.newHashMap();
        for (DbParameter param : parameters) {
            paramValues.put(param.getName(), param.getValue() != null ? param.getValue().toString() : StringUtils.EMPTY);
        }
        return paramValues;
    }

    public static String buildFileName(String fileNameTemplate, List<DbParameter> parameters) {

        if (StringUtils.isEmpty(fileNameTemplate) || CollectionUtils.isEmpty(parameters)) {
            return fileNameTemplate;
        }

        StrSubstitutor sub = new StrSubstitutor(getParameterValues(parameters), OPEN_BRACKET, CLOSE_BRACKET);
        String fileName = sub.replace(fileNameTemplate);
        return MessageFormat.format(fileName, new GregorianCalendar().getTime());
    }

}
