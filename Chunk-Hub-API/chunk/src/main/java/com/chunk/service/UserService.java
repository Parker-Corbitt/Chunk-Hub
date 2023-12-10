package com.chunk.service;

import com.chunk.model.Photo;
import com.chunk.model.UserPhoto;
import com.mongodb.client.MongoClients;
import org.apache.catalina.User;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private MongoTemplate template = new MongoTemplate(new SimpleMongoClientDatabaseFactory(MongoClients.create(), "ChunkHub"));

    public void insertPhoto(String id, Photo photo) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().push("photos", photo);
        template.findAndModify(query, update, UserPhoto.class);
    }

    public List<UserPhoto> findAllUserPhotos() {
        return template.findAll(UserPhoto.class, "UserPhotos");
    }

    public void insertUserPhoto(UserPhoto userPhoto) {
        template.insert(userPhoto, "UserPhotos");
    }

    public void insertImageIntoUserPhoto(String username, String filename, Binary image, String[] tags) {
        Query query = new Query(Criteria.where("username").is(username));
        Update update = new Update().push("photos", new Photo(username, filename, image, tags));
        template.findAndModify(query, update, UserPhoto.class);
    }

    public UserPhoto findUserByUsername(String username) {
        Query query = new Query(Criteria.where("username").is(username));
        return template.findOne(query, UserPhoto.class);
    }

    public List<Photo> findPhotosByUsername(String username) {
        Query query = new Query(Criteria.where("username").is(username));
        UserPhoto user = template.findOne(query, UserPhoto.class);

        if (user != null) {
            Photo[] photos = user.getPhotos();
            return new ArrayList<>(Arrays.asList(photos));
        } else {
            return new ArrayList<>();
        }
    }

    public List<Photo> findPhotosbyTag(String tag) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("tags").in(tag));
        ProjectionOperation projectionOperation = Aggregation.project("photos", "username");
        Aggregation aggregation = Aggregation.newAggregation(matchOperation, projectionOperation);
        return template.aggregate(aggregation, "UserPhotos", Photo.class).getMappedResults();
    }

}
