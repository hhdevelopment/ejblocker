/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.locker.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author François Achache
 */
@Target(value = {ElementType.PARAMETER})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface LockKey {

	String method() default "toString";
}
