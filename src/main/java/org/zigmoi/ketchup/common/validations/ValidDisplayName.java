package org.zigmoi.ketchup.common.validations;


import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@NotBlank
@Size(max = 50)
@Target({TYPE, METHOD, FIELD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface ValidDisplayName {

    String message() default "Display Name should be minimum 1 and maximum 50 in length.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}