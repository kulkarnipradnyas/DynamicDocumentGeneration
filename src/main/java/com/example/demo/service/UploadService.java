package com.example.demo.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

@Service
public class UploadService {

    private void setHeader(XSSFSheet sheet, CopyOnWriteArrayList<String> titles){
        Row headerRow = sheet.getRow(0);
        Iterator<Cell> cells = headerRow.cellIterator();
        // read all the titles and keep it in array list.
        while (cells.hasNext()) {
            Cell currentCell = cells.next();
            if (currentCell != null) {
                String cellValue = currentCell.toString();
                if (!cellValue.isEmpty()) {
                    titles.add("{" + cellValue + "}");
                }
            }
        }
    }
    private CopyOnWriteArrayList<ConcurrentHashMap<String, String>> getAllRowsTiedWithHeader(int rowsCount,  XSSFSheet sheet, CopyOnWriteArrayList<String> titles){
        CopyOnWriteArrayList<ConcurrentHashMap<String, String>> allRowsInfoWithTitle = new CopyOnWriteArrayList<>();

        IntStream.range(1, rowsCount).forEach(i->{
            Row row = sheet.getRow(i);
            Iterator<Cell> rowCells = row.cellIterator();
            ConcurrentHashMap<String, String> titleValueMap = new ConcurrentHashMap<>();
            int count = 0;
            // each column of row iteration
            while (rowCells.hasNext()) {
                // cell of selected row
                Cell currentCell = rowCells.next();

                if (currentCell != null) {
                    //cell value of selected row
                    String cellValue = currentCell.toString();
                    // count is 0 to tileList so all 1st title will be 1st cell of row, like wise storing values
                    titleValueMap.put(titles.get(count).toString(), cellValue);
                    //each row with title captured

                    count++;
                }

            }
            allRowsInfoWithTitle.add(titleValueMap);
        });
        return  allRowsInfoWithTitle;
    }
    public CopyOnWriteArrayList<ConcurrentHashMap<String, String>> readXlsTitleRow(MultipartFile xlsInput) {

        try {
            InputStream inputStream = xlsInput.getInputStream();
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            //Get first/desired sheet from the workbook
            XSSFSheet sheet = workbook.getSheetAt(0);
            int rowsCount = sheet.getPhysicalNumberOfRows();
            CopyOnWriteArrayList<String> titles = new CopyOnWriteArrayList<>();
            //setting header
            setHeader(sheet, titles);
            return getAllRowsTiedWithHeader( rowsCount, sheet, titles);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

}
