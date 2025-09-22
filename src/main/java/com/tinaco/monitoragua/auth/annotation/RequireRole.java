package com.tinaco.monitoragua.auth.annotation;

import com.tinaco.monitoragua.usuario.entity.Usuario.Role;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    Role value();
}
