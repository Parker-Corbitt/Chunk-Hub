package com.chunk.controller;

import com.chunk.model.Photo;
import com.chunk.model.UserPhoto;
import com.chunk.service.UserService;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.bson.BsonBinarySubType;
import org.bson.json.JsonObject;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.json.JSONObject;
import org.json.JSONException;

@RestController
class PhotoController {

    @Autowired
    UserService userService;

    @PostMapping(value = "/photo")
    public String storePhotoForUser(@RequestBody MultipartFile image, String username, String filename, String[] tags) throws IOException {
        String userValue = username.toLowerCase();
        String filenameValue = filename.toLowerCase();
        UserPhoto userCheck = null;
        userCheck = userService.findUserByUsername(userValue);
        if (userCheck == null) {
            userService.insertUserPhoto(new UserPhoto(userValue, new Photo[]{new Photo(userValue, filenameValue,
                    new Binary(BsonBinarySubType.BINARY, image.getBytes()), tags)}));
            return("User document created for user " + username + " successfully.");
        } else {
            userService.insertPhoto(userCheck.getId(), new Photo(userValue, filenameValue,
                    new Binary(BsonBinarySubType.BINARY, image.getBytes()), tags));
            return("User document updated for user " + username + ".");
        }
    }

    @PutMapping(value = "/photo")
    public String storePhotoForUserBytes(@RequestBody byte[] image,@RequestBody String username,@RequestBody String filename,@RequestBody String[] tags) throws IOException {
        String userValue = username.toLowerCase();
        String filenameValue = filename.toLowerCase();
        UserPhoto userCheck = null;
        userCheck = userService.findUserByUsername(userValue);
        if (userCheck == null) {
            userService.insertUserPhoto(new UserPhoto(userValue, new Photo[]{new Photo(userValue, filenameValue,
                    new Binary(BsonBinarySubType.BINARY, image), tags)}));
            return("User document created for user " + username + " successfully.");
        } else {
            userService.insertPhoto(userCheck.getId(), new Photo(userValue, filenameValue,
                    new Binary(BsonBinarySubType.BINARY, image), tags));
            return("User document updated for user " + username + ".");
        }
    }

        @PutMapping(value = "/photos")
        public String dubby(@RequestBody String username) throws IOException {
            System.out.println(username);
            Scanner scanner = new Scanner(username).useDelimiter(",");
            String userValue = scanner.next();
            String filenameValue = scanner.next();
            String tagsValue = scanner.next();
            String[] tags = tagsValue.split(" ");
            System.out.println(userValue);
            System.out.println(filenameValue);
            System.out.println(tagsValue);
            String imageValue = scanner.useDelimiter("\\A").next();
            System.out.println(imageValue);

            return "Bingus Dick";
        }

    @GetMapping(value = "/photo-tags")
    public String getPhotosByTags(@RequestParam String tag) throws IOException {
        String tagValue = tag.toLowerCase();
        List<Photo> queryResult = userService.findPhotosbyTag(tagValue);
        for (Photo photo : queryResult) {
            System.out.println(photo.getFilename());
        }
        if (queryResult == null) {
            throw new IOException("No photos found for tag " + tag + ".");
        }
        return queryResult.toString();
    }



    @GetMapping(value = "/photo-zip", produces = "application/zip")
    public @ResponseBody byte[] getAllPhotosZip(@RequestParam String username) throws IOException {
        String usernameValue = username.toLowerCase();
        List<Photo> queryResult = userService.findPhotosByUsername(usernameValue);
        if (queryResult == null) {
            throw new IOException("No photos found for user " + username + ".");
        }


        File zipFile = new File(username);
        List<String> filenames = new ArrayList<>();
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile));
        int counter = 0;
        for (Photo photo : queryResult) {
            counter++;
            ZipEntry entry;
            if (filenames.contains(photo.getFilename())) {
                entry = new ZipEntry(photo.getFilename() + counter + ".png");
            } else {
                entry = new ZipEntry(photo.getFilename() +  ".png");
            }
                zip.putNextEntry(entry);
                zip.write(photo.getImage().getData());
                zip.closeEntry();
            filenames.add(photo.getFilename());
        }
        zip.close();

        FileInputStream fis = new FileInputStream(zipFile);
        byte[] result = new byte[(int) zipFile.length()];
        fis.read(result);
        fis.close();
        return result;
    }
}
