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
import org.springframework.data.mongodb.core.query.BasicQuery;
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

    public List<Photo> findPhotosbyTagsAndUsername(String tag1, String username) {
        BasicQuery query = new BasicQuery("{ 'photos.tags': { $all: ['" + tag1 + "'] }, 'username': '" + username + "' }");
        List<UserPhoto> photos = template.find(query, UserPhoto.class);
        List<Photo> photosWithTag = new ArrayList<>();
        for (UserPhoto user : photos) {
            for (Photo photo : user.getPhotos()) {
                if (Arrays.asList(photo.getTags()).containsAll(Arrays.asList(tag1))) {
                    photosWithTag.add(photo);
                }
            }
        }
        return photosWithTag;
    }

    public List<Photo> findPhotosbyTagsAndUsername(String tag, String tag2, String username) {
        BasicQuery query = new BasicQuery("{ 'photos.tags': { $all: ['" + tag + "', '" + tag2 + "'] }, 'username': '" + username + "' }");
        List<UserPhoto> photos = template.find(query, UserPhoto.class);
        List<Photo> photosWithTag = new ArrayList<>();
        for (UserPhoto user : photos) {
            for (Photo photo : user.getPhotos()) {
                if (Arrays.asList(photo.getTags()).containsAll(Arrays.asList(tag, tag2))) {
                    photosWithTag.add(photo);
                }
            }
        }
        return photosWithTag;
    }

    public List<Photo> findPhotosbyTagsAndUsername(String tag1, String tag2, String tag3, String username) {
        BasicQuery query = new BasicQuery("{ 'photos.tags': { $all: ['" + tag1 + "', '" + tag2 + "', '" + tag3 + "'] }, 'username': '" + username + "' }");
        List<UserPhoto> photos = template.find(query, UserPhoto.class);
        List<Photo> photosWithTag = new ArrayList<>();
        for (UserPhoto user : photos) {
            for (Photo photo : user.getPhotos()) {
                if (Arrays.asList(photo.getTags()).containsAll(Arrays.asList(tag1, tag2, tag3))) {
                    photosWithTag.add(photo);
                }
            }
        }
        return photosWithTag;
    }

    public List<Photo> findPhotosbyTag(String tag) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("photos.tags").in(tag));
        ProjectionOperation projectionOperation = Aggregation.project("photos", "username");
        Aggregation aggregation = Aggregation.newAggregation(matchOperation, projectionOperation);
        List<UserPhoto> photos = template.aggregate(aggregation, "UserPhotos", UserPhoto.class).getMappedResults();
        List <Photo> photosWithTag = new ArrayList<>();
        for (UserPhoto user : photos) {
            for (Photo photo : user.getPhotos()) {
                if (Arrays.asList(photo.getTags()).contains(tag)) {
                    photosWithTag.add(photo);
                }
            }
        }
        return photosWithTag;
    }

    public List<Photo> findPhotosbyTags(String tag, String tag2, String tag3) {
    BasicQuery query = new BasicQuery("{ 'photos.tags': { $all: ['" + tag + "', '" + tag2 + "', '" + tag3 + "'] } }");
    List<UserPhoto> photos = template.find(query, UserPhoto.class);
    List<Photo> photosWithTag = new ArrayList<>();
    for (UserPhoto user : photos) {
        for (Photo photo : user.getPhotos()) {
            if (Arrays.asList(photo.getTags()).containsAll(Arrays.asList(tag, tag2, tag3))) {
                photosWithTag.add(photo);
            }
        }
    }
    return photosWithTag;
}

    public List<Photo> findPhotosbyTags(String tag, String tag2) {
        BasicQuery query = new BasicQuery("{ 'photos.tags': { $all: ['" + tag + "', '" + tag2 + "'] } }");
        List<UserPhoto> photos = template.find(query, UserPhoto.class);
        List<Photo> photosWithTag = new ArrayList<>();
        for (UserPhoto user : photos) {
            for (Photo photo : user.getPhotos()) {
                if (Arrays.asList(photo.getTags()).containsAll(Arrays.asList(tag, tag2))) {
                    photosWithTag.add(photo);
                }
            }
        }
        return photosWithTag;
    }

    public List<Photo> findPhotosbyTags(String tag) {
        BasicQuery query = new BasicQuery("{ 'photos.tags': { $all: ['" + tag + "'] } }");
        List<UserPhoto> photos = template.find(query, UserPhoto.class);
        List<Photo> photosWithTag = new ArrayList<>();
        for (UserPhoto user : photos) {
            for (Photo photo : user.getPhotos()) {
                if (Arrays.asList(photo.getTags()).containsAll(Arrays.asList(tag))) {
                    photosWithTag.add(photo);
                }
            }
        }
        return photosWithTag;
    }

}
