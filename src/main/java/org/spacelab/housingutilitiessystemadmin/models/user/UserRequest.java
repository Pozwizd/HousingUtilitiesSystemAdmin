package org.spacelab.housingutilitiessystemadmin.models.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.bson.types.ObjectId;
import org.spacelab.housingutilitiessystemadmin.validation.PasswordMatches;
import org.springframework.web.multipart.MultipartFile;

@Data
@PasswordMatches
public class UserRequest {
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @Size(max = 50, message = "Middle name must be less than 50 characters")
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Login is required")
    @Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
    private String login;

    @NotNull(message = "City ID is required")
    private ObjectId cityId;

    @NotNull(message = "Address ID is required")
    private ObjectId addressId;

    @NotBlank(message = "House number is required")
    private String houseNumber;

    @NotBlank(message = "Apartment number is required")
    private String apartmentNumber;

    @NotNull(message = "Apartment area is required")
    private Double apartmentArea;

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "Status is required")
    private String status;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    private MultipartFile photoFile;
    private ObjectId id;

}