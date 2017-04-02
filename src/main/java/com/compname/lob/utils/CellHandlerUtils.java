package com.compname.lob.utils;

import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.cellwalk.CellHandler;
import org.apache.poi.ss.util.cellwalk.CellWalkContext;

/**
 * CellHandler from :
 * http://code.google.com/p/codelabor/source/browse/trunk/poi-example/src/main/java/org/codelabor/example/poi/ss/util
 * /cellwalk/EmpCellHandler.java?spec=svn5547&r=5547
 * 
 * @author adapted by vegirl1
 * @since May 28, 2015
 * @version $Revision$
 */
public class CellHandlerUtils implements CellHandler {

    // vegirl1: new Map should be provided each time Handler is used
    private Map<String, String> cellValues;

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.poi.ss.util.cellwalk.CellHandler#onCell(org.apache.poi.ss.usermodel.Cell,
     *      org.apache.poi.ss.util.cellwalk.CellWalkContext)
     */
    @Override
    public void onCell(Cell cell, CellWalkContext ctx) {

        CellReference cellRef = new CellReference(cell);
        cellValues.put(cellRef.formatAsString(), ExcelUtils.getCellContentAsString(cell));
    }

    /**
     * Getter method of the <code>"cellValues"</code> class attribute.
     * 
     * @return the cellValues.
     */
    public Map<String, String> getCellValues() {
        return this.cellValues;
    }

    /**
     * Setter method of the <code>"cellValues"</code> class attribute.
     * 
     * @param cellValues the cellValues to set.
     */
    public void setCellValues(Map<String, String> aCellValues) {
        this.cellValues = aCellValues;
    }

}
