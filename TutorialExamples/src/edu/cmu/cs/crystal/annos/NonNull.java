package edu.cmu.cs.crystal.annos;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface NonNull {

}
