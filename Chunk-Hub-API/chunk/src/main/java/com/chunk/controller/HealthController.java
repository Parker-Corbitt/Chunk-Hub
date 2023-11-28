package com.chunk.controller;

import com.chunk.model.Photo;
import com.chunk.repository.PhotoRepository;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
class HealthController {

    @Autowired
    PhotoRepository photoRepository;

    @GetMapping(value = "/health")
    public String health() {
        return ("UP");
    }

    @PostMapping(value = "/photo")
    public String storePhoto(@RequestBody() MultipartFile image) throws IOException {
        photoRepository.save(new Photo("0", "test", "testUser",
                new Binary(BsonBinarySubType.BINARY, image.getBytes())));
        return("Stored");
    }

}
