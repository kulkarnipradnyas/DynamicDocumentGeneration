package com.example.demo.service;


import com.example.demo.utils.UploadDownloadUtils;
import de.phip1611.Docx4JSRUtil;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class DownloadService {

    private  ByteArrayOutputStream createZip(Path path) throws IOException {
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
        f.delete();
        return baos;
    }


    public  byte[] readDocTemplate(MultipartFile docTemplate, CopyOnWriteArrayList<ConcurrentHashMap<String, String>> allRowsInfoWithTitle, String fileName) throws IOException {

        // this folder for collective cleaning
        Path mydocsPath = Paths.get("mydoc");
        if (!Files.exists(mydocsPath)) {
            mydocsPath=  Files.createDirectory(mydocsPath);
        }
        String folderName = String.valueOf(UUID.randomUUID());

        Path timeStampFolder = Paths.get("mydoc"+ File.separator +folderName);
        // this folder if collision occurs so create folder with uuid
        if (!Files.exists(timeStampFolder)) {
            timeStampFolder=  Files.createDirectory( timeStampFolder);
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
                try {
                    // if name is not empty
                    Path filePath = !name.isEmpty() ?
                            Paths.get("mydoc" + File.separator + folderName + File.separator + name) : Paths.get("mydoc" + File.separator + folderName + File.separator + firstKey);
                    // create file inside timestamp folder with name field present in xlsx
                    if (!Files.exists(filePath)) {
                        filePath = Files.createFile(filePath);
                    } else {
                        System.out.println("**************" + filePath);
                    }
                    // create file object out of above path
                    File file = filePath.toFile();
                    // save template in the form of outputstream
                    template.save(file);
                }catch(Exception e){
                    System.out.println(name +"*********namename");
                }
            } catch (IOException e) {

                throw new RuntimeException(e);
            } catch (Docx4JException e) {

                throw new RuntimeException(e);
            } finally {
                UploadDownloadUtils.handleStreamClosing(inputStream);
            }
        });
        ByteArrayOutputStream zipOut= createZip(timeStampFolder);
        FileUtils.deleteDirectory(mydocsPath.toFile());
        return zipOut.toByteArray();
    }


}
