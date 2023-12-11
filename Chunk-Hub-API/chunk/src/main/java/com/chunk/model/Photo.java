package com.chunk.model;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("photos")
public class Photo {
    @Id
    private String _id;
    private String user;
    private Binary image;
    private String filename;
    private String[] tags;

    public Photo(String user, String filename, Binary image, String[] tags) {
        this.user = user;
        this.image = image;
        this.tags = tags;
        this.filename = filename;
    }

    public Binary getImage() {
        return this.image;
    }
    public String getId() { return this._id; }
    public String getUser() { return this.user; }
    public String getFilename() { return this.filename; }

    public String[] getTags() {
        return tags;
    }
}
