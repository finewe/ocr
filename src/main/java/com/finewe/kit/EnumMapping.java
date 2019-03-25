package com.finewe.kit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnumMapping {
	public String value() default EnumType.STRING;
	
	static public class EnumType{
		public static final String ORDINAL = "ORDINAL";
		public static final String STRING = "STRING";
	}
}
