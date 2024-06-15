package com.bili.api;

import Service.FileService;
import Service.Util.FDFSUtil;
import domain.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class FileApi {
    @Autowired(required = false)
    private FileService fileService;

    @Autowired(required = false)
    FDFSUtil fdfsUtil;

    @PutMapping("/file-slices")
    public JsonResponse<String> uploadFileBySlices(MultipartFile slice,
                                                   String fileMd5,
                                                   Integer sliceNo,
                                                   Integer totalSliceNo) throws IOException {
        String filePath = fileService.uploadFileBySlices(slice, fileMd5, sliceNo, totalSliceNo);
        return new JsonResponse<>(filePath);
    }

    @GetMapping("/slices")
    public void slices(MultipartFile file) throws IOException {
        fdfsUtil.convertFileToSlices(file);
    }

    @PostMapping("/md5files")
    public JsonResponse<String> getFileMd5(MultipartFile file) throws IOException {
        String fileMd5 = fileService.getFileMd5(file);
        return new JsonResponse<>(fileMd5);
    }
}
