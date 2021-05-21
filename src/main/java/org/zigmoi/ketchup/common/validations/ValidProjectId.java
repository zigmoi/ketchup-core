package org.zigmoi.ketchup.common.validations;


import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@NotBlank(message = "Project name (projectResourceId) should be minimum 2 and maximum 20 in length.")
@Size(min= 2, max = 20, message = "Project name (projectResourceId) should be minimum 2 and maximum 20 in length.")
@Pattern(regexp = "[a-zA-Z0-9-_]*", message = "Project name (projectResourceId) should only contain alphabets, numbers, dashes and underscores.")
@Target({ TYPE, METHOD, FIELD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { })
@Documented
public @interface ValidProjectId {

    String message() default "Invalid project name (projectResourceId), only alphabets and numbers are allowed and length should be between 2-20. ";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}