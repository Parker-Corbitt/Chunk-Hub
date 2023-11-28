package com.chunk.repository;

import com.chunk.model.Photo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PhotoRepository extends MongoRepository<Photo, String> {
    @Query("{title: '?0'}")
    Photo findPhotoByTitle(String title);
    @Query("{user: '?0'}")
    List<Photo> findAllPhotosForUser(String user);


}
