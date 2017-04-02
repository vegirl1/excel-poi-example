package com.compname.lob.service.impl.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.compname.lob.domain.config.EligibilityConfig;
import com.compname.lob.domain.workorder.EligibilityWorkOrder;
import com.compname.lob.utils.ExcelUtils;
import com.compname.lob.utils.WorkOrderUtils;

/**
 * WorkOrderUtilsTest
 * 
 * @author vegirl1
 * @since Jun 12, 2015
 * @version $Revision$
 */
@RunWith(MockitoJUnitRunner.class)
public class WorkOrderUtilsTest {

    File eligibilityFileName;

    @Before
    public void setUp() throws Exception {
        eligibilityFileName = new File("src/test/resources/sample-workorder/eligibility/ITWORKorder 99999.xlsx");
    }

    @Test
    public void testGetWorkOrderInputStream() {
        InputStream in = WorkOrderUtils.getWorkOrderInputStream(eligibilityFileName);
        Assert.assertNotNull(in);
    }

    @Test
    public void testMoveFiles() {

        String fileName = "tmpWorkOrderFile.xlsx";
        String inDir = "work/eligibility/in";
        String archiveDir = "work/eligibility/archive";

        File tmpWorkOrderFile = new File(FilenameUtils.concat(inDir, fileName));
        try {
            Assert.assertTrue(tmpWorkOrderFile.createNewFile());
        } catch (IOException ex) {
            Assert.assertTrue(false);
        }

        List<File> sourceFiles = Lists.newArrayList(tmpWorkOrderFile);
        WorkOrderUtils.moveFiles(sourceFiles, archiveDir, false);
        Assert.assertTrue(true);

        FileUtils.deleteQuietly(new File(FilenameUtils.concat(archiveDir, fileName)));
        Assert.assertTrue(true);
    }

    @Test
    public void testGetWorkOrderContent() {
        InputStream in = null;
        try {
            in = WorkOrderUtils.getWorkOrderInputStream(eligibilityFileName);

            Map<String, List<String>> sheetCellsToProcess = Maps.newHashMap();
            sheetCellsToProcess.put("eligibility.sheet.eligibility_work_order", Arrays.asList("B3", "B6", "B9:B18", "B22:B25",
                    "table.MANU_CORE|A:G", "table.GIPSY_CORE|A:F", "table.GIPSY_CI|A:F"));

            sheetCellsToProcess.put("eligibility.sheet.benefit_mapping", Arrays.asList("table.BNFT_MAPP|A2:E2"));

            sheetCellsToProcess.put("eligibility.sheet.certificate_mapping", Arrays.asList("B2:B4", "table.CERT_MAPP|A8:B8"));

            Map<String, String> sheetTableDescriptions = Maps.newHashMap();
            sheetTableDescriptions.put("eligibility.sheet.eligibility_work_order.table.MANU_CORE",
                    "Manuconnect conversion|Core Plan (Health/Dental/Cost Plus)");
            sheetTableDescriptions.put("eligibility.sheet.eligibility_work_order.table.GIPSY_CORE",
                    "Gipsy conversion|Core Plan (Health/Dental/Cost Plus)");

            sheetTableDescriptions.put("eligibility.sheet.eligibility_work_order.table.GIPSY_CI",
                    "Gipsy conversion|Crticial Illness");

            EligibilityConfig eligibilityConfig = new EligibilityConfig();
            eligibilityConfig.setSheetToProcess(Arrays.asList("Eligibility Work Order", "Cert Conversions",
                    "Benefit Mapping Manuconnect"));
            eligibilityConfig.setSheetCells(sheetCellsToProcess);
            eligibilityConfig.setSheetTableDescriptions(sheetTableDescriptions);

            eligibilityConfig.setPlanMappingTables(Arrays.asList("table.MANU_CORE", "table.GIPSY_CORE"));
            eligibilityConfig.setBenefitMappingTables(Arrays.asList("table.BNFT_MAPP"));
            eligibilityConfig.setCertificateMappingTables(Arrays.asList("table.CERT_MAPP"));

            EligibilityWorkOrder eligibilityWorkOrder = WorkOrderUtils.getEligibilityWorkOrderContent(in, eligibilityConfig);

            Assert.assertNotNull(eligibilityWorkOrder);
            Assert.assertTrue("20200101".equals(eligibilityWorkOrder.getConversionDate()));

        } catch (Exception e) {
            Assert.assertTrue(false);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    @Test
    public void testGetCellTableContent() {
        InputStream in = null;
        XSSFWorkbook workbook = null;

        try {
            in = WorkOrderUtils.getWorkOrderInputStream(eligibilityFileName);
            workbook = ExcelUtils.getWorkBook(in);
            XSSFSheet sheet = workbook.getSheetAt(0);
            CellReference firstCellRef = new CellReference("A30");
            CellReference lastCellRef = new CellReference("G30");
            CellRangeAddress refCellRange = new CellRangeAddress(firstCellRef.getRow(), lastCellRef.getRow(),
                    firstCellRef.getCol(), lastCellRef.getCol());

            List<Map<String, String>> list = WorkOrderUtils.getTableCellContent(sheet, refCellRange);
            Assert.assertNotNull(list);
            Assert.assertTrue(list.size() > 0);

        } catch (Exception e) {
            Assert.assertTrue(false);
        } finally {
            IOUtils.closeQuietly(workbook);
            IOUtils.closeQuietly(in);
        }
    }

    @Test
    public void testGetTableCellRange() {
        InputStream in = null;
        XSSFWorkbook workbook = null;

        try {
            in = WorkOrderUtils.getWorkOrderInputStream(eligibilityFileName);
            workbook = ExcelUtils.getWorkBook(in);
            XSSFSheet sheet = workbook.getSheetAt(0);

            Map<String, String> sheetTableDescriptions = Maps.newHashMap();
            sheetTableDescriptions.put("eligibility.sheet.eligibility_work_order.table.MANU_CORE",
                    "Manuconnect conversion|Core Plan (Health/Dental/Cost Plus)");
            sheetTableDescriptions.put("eligibility.sheet.eligibility_work_order.table.GIPSY_CORE",
                    "Gipsy conversion|Core Plan (Health/Dental/Cost Plus)");

            CellRangeAddress cellRangeAddress = WorkOrderUtils.getTableCellRange(sheet, "eligibility.sheet.eligibility_work_order",
                    "table.MANU_CORE|A:G", sheetTableDescriptions);

            Assert.assertNotNull(cellRangeAddress);
            Assert.assertTrue(cellRangeAddress.getFirstRow() > 0);

            cellRangeAddress = WorkOrderUtils.getTableCellRange(sheet, "eligibility.sheet.eligibility_work_order",
                    "table.GIPSY_CORE|A:F", sheetTableDescriptions);

            Assert.assertNotNull(cellRangeAddress);
            Assert.assertTrue(cellRangeAddress.getFirstRow() > 0);

        } catch (Exception e) {
            Assert.assertTrue(false);
        } finally {
            IOUtils.closeQuietly(workbook);
            IOUtils.closeQuietly(in);
        }
    }

    @Test
    public void testIsCellRangeContentNotBlank() throws ServiceException {
        Map<String, String> mapValues = Maps.newHashMap();
        mapValues.put("B2", "some value for B2");
        mapValues.put("B3", "some value for B3");

        Assert.assertTrue(WorkOrderUtils.isCellRangeContentNotBlank(mapValues));

        mapValues = Maps.newHashMap();
        mapValues.put("B2", "");
        mapValues.put("B3", null);

        Assert.assertFalse(WorkOrderUtils.isCellRangeContentNotBlank(mapValues));
    }

    @Test
    public void testGetGroupNumberFromFileName() {
        String groupName = WorkOrderUtils.getGroupNumberFromFileName(eligibilityFileName.getName());
        Assert.assertNotNull(groupName);
        Assert.assertTrue("Group number from file name is: " + groupName, "99999".equals(groupName));
    }

    @Test
    public void testListToMap() {

        List<String> params = Arrays.asList("param1,param2,param3".split(","));

        Map<String, String> parameters = Maps.uniqueIndex(params, new Function<String, String>() {
            public String apply(String key) {
                return key;
            }
        });
        Assert.assertNotNull(parameters);
    }
}
