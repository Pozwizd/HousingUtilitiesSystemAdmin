package org.spacelab.housingutilitiessystemadmin.entity;

import lombok.Data;
import org.spacelab.housingutilitiessystemadmin.entity.location.House;
import org.spacelab.housingutilitiessystemadmin.entity.location.Status;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Data
@Document
public class Chairman {
    @Id
    private String id;
    private String lastName;
    private String firstName;
    private String middleName;
    private String phone;
    private String email;
    private Status status;
    private String login;
    private String password;
    private String photo;

    @DocumentReference(
            lazy = true
    )
    private House house;

    public String getFullName() {
        return lastName + " " + firstName + " " + middleName;
    }
}