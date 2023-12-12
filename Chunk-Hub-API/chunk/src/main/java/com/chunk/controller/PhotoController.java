package com.chunk.controller;

import com.chunk.model.Photo;
import com.chunk.model.UserPhoto;
import com.chunk.service.UserService;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.json.JSONException;

@RestController
class PhotoController {

    @Autowired
    UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(value = "/photo")
    public String storePhotoForUser(@RequestBody MultipartFile image, String username, String filename, String[] tags) throws IOException {
        String userValue = username.toLowerCase();
        String filenameValue = filename.toLowerCase();
        UserPhoto userCheck = null;
        userCheck = userService.findUserByUsername(userValue);
        if (userCheck == null) {
            userService.insertUserPhoto(new UserPhoto(userValue, new Photo[]{new Photo(userValue, filenameValue,
                    new Binary(BsonBinarySubType.BINARY, image.getBytes()), tags)}));
            return ("User document created for user " + username + " successfully.");
        } else {
            userService.insertPhoto(userCheck.getId(), new Photo(userValue, filenameValue,
                    new Binary(BsonBinarySubType.BINARY, image.getBytes()), tags));
            return ("User document updated for user " + username + ".");
        }
    }

    @PutMapping(value = "/photo")
    public String storePhotoForUserBytes(@RequestBody byte[] image, @RequestBody String username, @RequestBody String filename, @RequestBody String[] tags) throws IOException {
        String userValue = username.toLowerCase();
        String filenameValue = filename.toLowerCase();
        UserPhoto userCheck = null;
        userCheck = userService.findUserByUsername(userValue);
        if (userCheck == null) {
            userService.insertUserPhoto(new UserPhoto(userValue, new Photo[]{new Photo(userValue, filenameValue,
                    new Binary(BsonBinarySubType.BINARY, image), tags)}));
            return ("User document created for user " + username + " successfully.");
        } else {
            userService.insertPhoto(userCheck.getId(), new Photo(userValue, filenameValue,
                    new Binary(BsonBinarySubType.BINARY, image), tags));
            return ("User document updated for user " + username + ".");
        }
    }

    @PutMapping(value = "/photos")
    public String dubby(@RequestBody String username) throws IOException, JSONException {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getJsonFactory();
        try {
            JsonParser parser = factory.createJsonParser(username);
            JsonNode Obj = mapper.readTree(parser);
            //System.out.println(Obj.get("username"));
            String userValue = Obj.get("username").toString().toLowerCase();
            userValue = userValue.substring(1, userValue.length() - 1);
            //System.out.println(Obj.get("filename"));
            String filenameValue = Obj.get("filename").toString().toLowerCase();
            filenameValue = filenameValue.substring(1, filenameValue.length() - 1);
            //System.out.println(Obj.get("image"));
            String imageValue = Obj.get("image").toString();
            imageValue = imageValue.substring(1, imageValue.length() - 1);
            byte[] imageBytes = Base64.getDecoder().decode(imageValue);

            String[] tags = Obj.get("tags").toString().split(",");
            UserPhoto userCheck = null;
            userCheck = userService.findUserByUsername(userValue);
            if (userCheck == null) {
                userService.insertUserPhoto(new UserPhoto(userValue, new Photo[]{new Photo(userValue, filenameValue,
                        new Binary(BsonBinarySubType.BINARY, imageBytes), tags)}));
                return ("User document created for user " + username + " successfully.");
            } else {
                userService.insertPhoto(userCheck.getId(), new Photo(userValue, filenameValue,
                        new Binary(BsonBinarySubType.BINARY, imageBytes), tags));
                return ("User document updated for user " + username + ".");
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


        return "Bingus";
    }

    @GetMapping(value = "/photo-tags-user", produces = {"Application/zip"})
    public byte[] getPhotosByTags(@RequestParam String tag, @RequestParam String username) throws IOException {
        String usernameValue = username.toLowerCase();
        List<Photo> queryResult = null;
        if (tag.split(",").length == 3) {
            String tag1 = tag.split(",")[0].toLowerCase();
            String tag2 = tag.split(",")[1].toLowerCase();
            String tag3 = tag.split(",")[2].toLowerCase();
            queryResult = userService.findPhotosbyTagsAndUsername(tag1, tag2, tag3, usernameValue);
        } else if (tag.split(",").length == 2) {
            String tag1 = tag.split(",")[0].toLowerCase();
            String tag2 = tag.split(",")[1].toLowerCase();
            queryResult = userService.findPhotosbyTagsAndUsername(tag1, tag2, usernameValue);
        } else if (tag.split(",").length == 1) {
            String tag1 = tag.split(",")[0].toLowerCase();
            queryResult = userService.findPhotosbyTagsAndUsername(tag1, usernameValue);
        } else {
            throw new IOException("Invalid tag count.");
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
                entry = new ZipEntry(photo.getFilename() + ".png");
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


    @GetMapping(value = "/photo-user", produces = {"Application/zip"})
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
                entry = new ZipEntry(photo.getFilename() + ".png");
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


    @GetMapping(value = "/photo-tags", produces = {"Application/zip"})
    public byte[] getPhotosByTags(@RequestParam String tag) throws IOException {
        List<Photo> queryResult = null;
        if (tag.split(",").length == 3) {
            String tag1 = tag.split(",")[0].toLowerCase();
            String tag2 = tag.split(",")[1].toLowerCase();
            String tag3 = tag.split(",")[2].toLowerCase();
            queryResult = userService.findPhotosbyTags(tag1, tag2, tag3);
        } else if (tag.split(",").length == 2) {
            String tag1 = tag.split(",")[0].toLowerCase();
            String tag2 = tag.split(",")[1].toLowerCase();
            queryResult = userService.findPhotosbyTags(tag1, tag2);
        } else if (tag.split(",").length == 1) {
            String tag1 = tag.split(",")[0].toLowerCase();
            queryResult = userService.findPhotosbyTag(tag1);
        } else {
            throw new IOException("Invalid tag size, input 3 tags.");
        }

        if (queryResult == null) {
            throw new IOException("No photos found for tag " + tag + ".");
        }
        File zipFile = new File("tags");
        List<String> filenames = new ArrayList<>();
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile));
        int counter = 0;
        for (Photo photo : queryResult) {
            counter++;
            ZipEntry entry;
            if (filenames.contains(photo.getFilename())) {
                entry = new ZipEntry(photo.getFilename() + counter + ".png");
            } else {
                entry = new ZipEntry(photo.getFilename() + ".png");
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