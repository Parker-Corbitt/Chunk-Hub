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
import java.util.Objects;
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
        if (!Objects.equals(image.getContentType(), "image/png")) {
            throw new IOException("Only PNG images are supported.");
        }
        String userValue = user.toLowerCase();
        photoRepository.save(new Photo(id, userValue,
                new Binary(BsonBinarySubType.BINARY, image.getBytes())));
        return("Stored photo" + id + " for user " + user + " successfully.");
    }

    @PutMapping(value = "/photo")
    public String storePhotoWithoutID(@RequestBody MultipartFile image, String user) throws IOException {
        if (!Objects.equals(image.getContentType(), "image/png")) {
            throw new IOException("Only PNG images are supported.");
        }
        System.out.println(image.getContentType());
        String userValue = user.toLowerCase();
        photoRepository.save(new Photo(userValue,
                new Binary(BsonBinarySubType.BINARY, image.getBytes())));
        return("Stored photo for user " + user + " successfully.");
    }

    @GetMapping(value = "/photo", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody byte[] getPhoto(@RequestParam String user) throws IOException {
        String userValue = user.toLowerCase();
        Photo queryResult = photoRepository.findPhotoByUser(userValue);
        if (queryResult == null) {
            throw new IOException("No photo found for user " + user + ".");
        }
            return queryResult.getImage().getData();
    }

    @GetMapping(value = "/photo-zip", produces = "application/zip")
    public @ResponseBody byte[] getAllPhotosZip(@RequestParam String user) throws IOException {
        String userValue = user.toLowerCase();
        List<Photo> queryResult = photoRepository.findAllPhotosForUser(userValue);
        if (queryResult == null) {
            throw new IOException("No photos found for user " + user + ".");
        }

        File zipFile = new File("test.zip");
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile));
        for (Photo photo : queryResult) {
            ZipEntry entry = new ZipEntry(photo.getId() + ".png");
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
