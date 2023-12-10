package com.chunk.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="UserPhotos")
public class UserPhoto {
    @Id
    private String _id;
    private String username;
    private Photo[] photos;

    public UserPhoto(String username, Photo[] photos) {
        this.username = username;
        this.photos = photos;
    }

    public String getId(){
        return this._id;
    }

    public Photo[] getPhotos() {
        return this.photos;
    }
}
