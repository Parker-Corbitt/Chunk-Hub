package com.chunk.model;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("photos")
public class Photo {
    @Id
    private String id;
    private String title;
    private String user;
    private Binary image;

    public Photo(String id,String title,String user, Binary image) {
        this.id = id;
        this.title = title;
        this.user = user;
        this.image = image;
    }

    public Binary getImage() {
        return this.image;
    }
}
