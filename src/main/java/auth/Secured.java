/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package auth;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.ws.rs.NameBinding;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;


/**
 *
 * @author ivanc
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE, METHOD})
public @interface Secured {
    Permission[] value() default{};
    
}
