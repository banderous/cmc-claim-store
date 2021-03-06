package uk.gov.hmcts.cmc.domain.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ValidFullAdmissionConstraintValidator.class, ValidPartAdmissionConstraintValidator.class})
@Documented
public @interface ValidAdmission {
    String message() default "Invalid admission response";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
