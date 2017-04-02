package com.compname.lob.domain.config;

/**
 * MasterAppConfig
 * 
 * @author vegirl1
 * @since Jun 9, 2015
 * @version $Revision$
 */
public class MasterAppConfig extends AbstractProperties {

    private static final long  serialVersionUID = -2181685793038263977L;

    public static final String STREAM_NAME      = "masterapp";

    /**
     * Class constructor.
     * 
     */
    public MasterAppConfig() {
        super(STREAM_NAME);
    }
}
