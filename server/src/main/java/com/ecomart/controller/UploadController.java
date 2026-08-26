package com.ecomart.controller;

import com.ecomart.dto.response.MessageResponse;
import com.ecomart.service.UploadService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping
    public MessageResponse upload(@RequestParam("file") MultipartFile file,
                                  @RequestParam(defaultValue = "misc") String folder) {
        return uploadService.upload(file, folder);
    }
}
