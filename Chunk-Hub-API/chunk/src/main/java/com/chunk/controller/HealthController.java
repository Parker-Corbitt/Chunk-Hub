package com.chunk.controller;

import com.chunk.model.Photo;
import com.chunk.repository.PhotoRepository;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
class HealthController {

    @Autowired
    PhotoRepository photoRepository;

    @GetMapping(value = "/health")
    public String health() {
        return ("UP");
    }

    @PostMapping(value = "/photo")
    public String storePhoto(@RequestBody MultipartFile image, String id, String user) throws IOException {
        photoRepository.save(new Photo(id, user,
                new Binary(BsonBinarySubType.BINARY, image.getBytes())));
        return("Stored");
    }

    @GetMapping(value = "/photo", produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody byte[] getPhoto(@RequestParam String user) throws IOException {
        Photo queryResult = photoRepository.findPhotoByUser(user);
            return queryResult.getImage().getData();
    }

    @GetMapping(value = "/photo-zip", produces = "application/zip")
    public @ResponseBody byte[] getAllPhotosZip(@RequestParam String user) throws IOException {
        List<Photo> queryResult = photoRepository.findAllPhotosForUser(user);

        File zipFile = new File("test.zip");
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile));
        for (Photo photo : queryResult) {
            ZipEntry entry = new ZipEntry(photo.getId() + ".jpg");
            zip.putNextEntry(entry);
            zip.write(photo.getImage().getData());
            zip.closeEntry();
        }
        zip.close();

        FileInputStream fis = new FileInputStream(zipFile);
        byte[] result = new byte[(int) zipFile.length()];
        fis.read(result);
        fis.close();
        return result;
    }


}
