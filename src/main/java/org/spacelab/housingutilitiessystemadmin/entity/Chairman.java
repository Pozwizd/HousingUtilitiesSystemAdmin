package org.spacelab.housingutilitiessystemadmin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.spacelab.housingutilitiessystemadmin.entity.location.House;
import org.spacelab.housingutilitiessystemadmin.entity.location.Status;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Data
@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
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