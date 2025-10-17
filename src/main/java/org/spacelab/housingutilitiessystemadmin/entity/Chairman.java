package org.spacelab.housingutilitiessystemadmin.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private String status;
    private String login;
    private String password;
    private String photo;

    public String getFullName() {
        return lastName + " " + firstName + " " + middleName;
    }
}