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

@NotBlank
@Size(max=50)
@Pattern(regexp = "[a-zA-Z0-9.]*", message = "Only alphabets, numbers and period are allowed.")
@Target({ TYPE, METHOD, FIELD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { })
@Documented
public @interface ValidTenantId {

    String message() default "Only alphabets, numbers and period are allowed and maximum length allowed is 50.";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}