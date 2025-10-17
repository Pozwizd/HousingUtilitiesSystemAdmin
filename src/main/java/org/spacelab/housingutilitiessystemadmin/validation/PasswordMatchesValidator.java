package org.spacelab.housingutilitiessystemadmin.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.spacelab.housingutilitiessystemadmin.models.user.UserRequest;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, UserRequest> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
    }

    @Override
    public boolean isValid(UserRequest userRequest, ConstraintValidatorContext context) {
        if (userRequest == null) {
            return true;
        }

        String password = userRequest.getPassword();
        String confirmPassword = userRequest.getConfirmPassword();

        // If both are null or empty, consider it valid (other validators will handle required fields)
        if ((password == null || password.isEmpty()) && (confirmPassword == null || confirmPassword.isEmpty())) {
            return true;
        }

        // Check if passwords match
        boolean isValid = password != null && password.equals(confirmPassword);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Пароли не совпадают")
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
        }

        return isValid;
    }
}
