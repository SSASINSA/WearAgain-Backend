package com.ssasinsa.wearagain.global.docs.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiDoc {

    String summary();

    String description() default "";

    String requestExample() default "";

    String responseExample() default "";

    Class<?> responseSchema() default Void.class;

    /**
     * 성공 응답 코드 (기본 200).
     */
    String successStatus() default "200";

    /**
     * 오류 응답 예시. 형식: "statusCode:{json 예시}"
     */
    String[] errorResponses() default {};
}
