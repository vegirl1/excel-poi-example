package com.compname.lob.domain.config;

/**
 * ClaimsConfig
 * 
 * @author vegirl1
 * @since Jun 12, 2015
 * @version $Revision$
 */
public class ClaimsConfig extends AbstractProperties {

    private static final long  serialVersionUID = 7107596093106837614L;

    public static final String STREAM_NAME      = "claims";

    public ClaimsConfig() {
        super(STREAM_NAME);
    }

}
