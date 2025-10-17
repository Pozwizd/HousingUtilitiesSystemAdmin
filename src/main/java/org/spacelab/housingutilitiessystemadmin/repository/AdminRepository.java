package org.spacelab.housingutilitiessystemadmin.repository;

import org.bson.types.ObjectId;
import org.spacelab.housingutilitiessystemadmin.entity.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends MongoRepository<Admin, ObjectId> {

    @Query("{email:'?0'}")
    Admin findByEmail(String email);
}
