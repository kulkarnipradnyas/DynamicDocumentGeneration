package com.example.demo.controller;


import com.example.demo.service.DownloadService;
import com.example.demo.service.UploadService;
import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@Validated
@CrossOrigin(origins = "*")
public class UploadDownloadController {

    private static final Logger logger = LoggerFactory.getLogger(UploadDownloadController.class);

    @Autowired
    private UploadService uploadService;

    @Autowired
    private DownloadService downloadService;

    @RequestMapping(value = {"/uploadDownload"}, method = {RequestMethod.POST}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<byte[]> downloadTemplate(@RequestPart("docTemplate") MultipartFile docTemplate,
                                                   @RequestPart("xmlInput") MultipartFile xmlInput,
                                                   @RequestParam String name
    ) throws IOException {
       CopyOnWriteArrayList<ConcurrentHashMap<String, String>> allRowsInfoWithTitle = uploadService.readXlsTitleRow(xmlInput);
       //  List<Map<String, String>> allRowsInfoWithTitle = UploadDownloadUtils.readXlsTitleRow(xmlInput);
       // byte[] offerLetters = UploadDownloadUtils.readDocTemplate1(docTemplate, allRowsInfoWithTitle, name);
        byte[] offerLetters = downloadService.readDocTemplate(docTemplate, allRowsInfoWithTitle, name);
        HttpHeaders headers = new HttpHeaders();

        Response response = new Response();
        response.setContentType("application/zip");
        headers.setContentDispositionFormData("attachment", "file.zip");
        return new ResponseEntity<>(offerLetters, headers, HttpStatus.OK);
    }


}
