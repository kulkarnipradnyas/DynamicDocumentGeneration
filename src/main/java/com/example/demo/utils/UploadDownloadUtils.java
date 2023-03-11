package com.example.demo.utils;

import de.phip1611.Docx4JSRUtil;
import net.lingala.zip4j.ZipFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.web.multipart.MultipartFile;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;


public class UploadDownloadUtils {

    public static List<Map<String, String>> readXlsTitleRow(MultipartFile xlsInput) {
        List<Map<String, String>> allRowsInfoWithTitle = new ArrayList<>();

        try {
            InputStream inputStream = xlsInput.getInputStream();
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            //Get first/desired sheet from the workbook
            XSSFSheet sheet = workbook.getSheetAt(0);

            int rowsCount = sheet.getPhysicalNumberOfRows();
            Row headerRow = sheet.getRow(0);
            Iterator<Cell> cells = headerRow.cellIterator();
            List<String> titles = new ArrayList<>();
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
            for (int i = 1; i < rowsCount; i++) {
                Row row = sheet.getRow(i);
                Iterator<Cell> rowCells = row.cellIterator();
                Map<String, String> titleValueMap = new HashMap<>();
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
            }


            return allRowsInfoWithTitle;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public static void handleStreamClosing(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
    public static void deleteDirectory(File file)
    {
        // store all the paths of files and folders present
        // inside directory
        for (File subfile : file.listFiles()) {

            // if it is a subfolder,e.g Rohan and Ritik,
            //  recursively call function to empty subfolder
            if (subfile.isDirectory()) {
                deleteDirectory(subfile);
            }

            // delete files and empty subfolders
            subfile.delete();
        }
    }

    public static byte[] readDocTemplate1(MultipartFile docTemplate, List<Map<String, String>> allRowsInfoWithTitle, String fileName) throws IOException {


        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String folderName = String.valueOf(timestamp.getTime());

        Path path = Paths.get(folderName);
        if (!Files.exists(path)) {
            path=  Files.createDirectory(path);
        }

        allRowsInfoWithTitle.stream().forEach(row -> {
            InputStream inputStream = null;
            try {
                inputStream = docTemplate.getInputStream();

                WordprocessingMLPackage template = WordprocessingMLPackage.load(inputStream);
                //search and replace in doc
                Docx4JSRUtil.searchAndReplace(template, row);
                String firstKey = row.keySet().stream().findFirst().toString();
                String name = row.get("{" + fileName + "}");
                // if name is not empty
                Path filePath = !name.isEmpty() ?
                        Paths.get(folderName + File.separator + name) : Paths.get(folderName + File.separator + firstKey);
                // create file inside timestamp folder with name field present in xlsx
                if (!Files.exists(filePath)) {
                    filePath = Files.createFile(filePath);
                }
                // create file object out of above path
                File file = filePath.toFile();
                // save template in the form of outputstream
                template.save(file);
            } catch (IOException e) {
                handleStreamClosing(inputStream);
                throw new RuntimeException(e);
            } catch (Docx4JException e) {
                handleStreamClosing(inputStream);
                throw new RuntimeException(e);
            } finally {
                handleStreamClosing(inputStream);

            }
        });
        //create zip file and add folder into it
        File f = new File("newZip.zip");
        ZipFile zipfile = new ZipFile(f);
        zipfile.addFolder(path.toFile());

        FileInputStream fis = new FileInputStream(f);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read = -1;
        while ((read = fis.read(buffer)) != -1) {
            baos.write(buffer, 0, read);
        }
        // call deleteDirectory function to delete
        // subdirectory and files
        File directory= path.toFile();
        deleteDirectory(directory);
        directory.delete();
        f.delete();
        return baos.toByteArray();
    }


}
