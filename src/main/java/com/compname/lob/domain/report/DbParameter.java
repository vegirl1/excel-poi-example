package com.compname.lob.domain.report;


/**
 * DbParameter
 * 
 * @author vegirl1
 * @since Jul 27, 2015
 * @version $Revision$
 */
public class DbParameter extends DomainModelObject {

    private static final long serialVersionUID = 8115787805842685819L;

    private String            name;
    private String            dataType;
    private String            valueSource;
    private Object            value;

    /**
     * Class constructor.
     * 
     */
    public DbParameter() {

    }

    public DbParameter(String name, String dataType, String valueSource, Object value) {
        this.name = name;
        this.dataType = dataType;
        this.valueSource = valueSource;
        this.value = value;
    }

    /**
     * Getter method of the <code>"name"</code> class attribute.
     * 
     * @return the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter method of the <code>"name"</code> class attribute.
     * 
     * @param Name the name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter method of the <code>"dataType"</code> class attribute.
     * 
     * @return the dataType.
     */
    public String getDataType() {
        return this.dataType;
    }

    /**
     * Setter method of the <code>"dataType"</code> class attribute.
     * 
     * @param DataType the dataType to set.
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * Getter method of the <code>"valueSource"</code> class attribute.
     * 
     * @return the valueSource.
     */
    public String getValueSource() {
        return this.valueSource;
    }

    /**
     * Setter method of the <code>"valueSource"</code> class attribute.
     * 
     * @param ValueSource the valueSource to set.
     */
    public void setValueSource(String valueSource) {
        this.valueSource = valueSource;
    }

    /**
     * Getter method of the <code>"value"</code> class attribute.
     * 
     * @return the value.
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * Setter method of the <code>"value"</code> class attribute.
     * 
     * @param Value the value to set.
     */
    public void setValue(Object value) {
        this.value = value;
    }

}
