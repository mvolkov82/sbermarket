package com.okeandra.demo.services.processing;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.okeandra.demo.services.parsers.ExcelParser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;

@Component
public class ExcelWarehouseLimiter extends ExcelParser {

    public boolean setStockLimits(String xlsFileName, int okeandraLimit, List<String> exceptionVendors) {
        int correctedItemsCount = 0;

        String tmpFile = getTempFileName(xlsFileName);

        try (HSSFWorkbook excelBook = new HSSFWorkbook(new FileInputStream(xlsFileName));
            FileOutputStream out = new FileOutputStream(tmpFile);) {
            Sheet myExcelSheet = excelBook.getSheet("TDSheet");
            int lastRowIndex = myExcelSheet.getLastRowNum();

            String itemId;
            String itemName = null;
            String vendor = null;

            for (int i = 0; i < lastRowIndex; i++) {
                itemId = null;
                Row row = myExcelSheet.getRow(i);

                try {
                    itemId = row.getCell(0).getStringCellValue();
                    itemName = row.getCell(1).getStringCellValue();
                    vendor = row.getCell(3).getStringCellValue();
                } catch (NullPointerException e) {
                    if (itemId == null) {
                        break;
                    }
                    System.out.println(String.format("%s %s - NPE Exception. Stop parsing", itemId, itemName));
                }

                int amountOkeandra = (int) row.getCell(15).getNumericCellValue();
                int amountVendor = (int) row.getCell(16).getNumericCellValue();

                if (!exceptionVendors.contains(vendor) && amountOkeandra <= okeandraLimit && amountVendor <= 0) {
//                  Attention! Don't use shiftRows()!!! It has a mistake and crash outgoing xls file
//                  myExcelSheet.shiftRows(i + 1, lastRowIndex-1, -1, false, false);
                    row.getCell(15).setCellValue(0);
                    correctedItemsCount++;
                    System.out.println(String.format("%s danger amount %d -> 0 %s : %s", itemId, amountOkeandra, vendor, itemName));
                }
            }

            excelBook.write(out);

        } catch (IOException e) {
            System.out.println("I/O exception in setStockLimits()");
            return false;
        } catch (Exception e) {
            System.out.println("Exception in setStockLimits(): " + e);
            return false;
        }
        System.out.println("-------Corrected " + correctedItemsCount + "----------");


        try {
            copyFileFromTo(tmpFile, xlsFileName);
        } catch (IOException e) {
            System.out.println("Ошибка при подмене временного файла");
            return false;
        }
        return true;
    }

    private void copyFileFromTo(String sourceFile, String destinationFile) throws IOException {
        Files.copy(Paths.get(sourceFile), Paths.get(destinationFile), StandardCopyOption.REPLACE_EXISTING);
    }

    private String getTempFileName(String xlsFileName) {
        String tempFileName = xlsFileName.substring(0, xlsFileName.lastIndexOf(".")) + "_tmp";
        String tempExtension = xlsFileName.substring(xlsFileName.length() - 4);
        return tempFileName + tempExtension;
    }
}