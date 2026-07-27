package jp.brainjuice.pokego.web.validation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import jp.brainjuice.pokego.web.validation.validator.InRangeValidator;

@Documented
@Constraint(validatedBy = InRangeValidator.class) // ステップ2で作成するバリデータークラスを指定
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
public @interface InRange {
    String message() default "{jp.brainjuice.pokego.web.validation.InRange.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int min();
    int max();

    // 複数指定を可能にするための定義 (Optional)
    @Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
    @Retention(RUNTIME)
    @Documented
    @interface List {
        InRange[] value();
    }
}
