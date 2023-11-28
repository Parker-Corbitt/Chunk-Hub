package com.chunk.repository;

import com.chunk.model.Photo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository("photos")
public interface PhotoRepository extends MongoRepository<Photo, String> {
    @Query("{title: '?0'}")
    Photo findPhotoByTitle(String title);
    @Query("{user: '?0'}")
    List<Photo> findAllPhotosForUser(String user);
    @Query("{id: '?0'}")
    Photo findPhotoById(String id);

}
