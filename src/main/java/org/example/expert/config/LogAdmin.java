package org.example.expert.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // 어노테이션 메서드에만 적용 가능
@Retention(RetentionPolicy.RUNTIME) // 런타임까지 어노테이션 정보 유지
// @interface -> 어노테이션 생성 선언, LogAdmin -> 어노테이션 이름
public @interface LogAdmin {
    String comment() default ""; // String 타입 comment 하나만 가질 수 있으며, 기본값은 공백
}