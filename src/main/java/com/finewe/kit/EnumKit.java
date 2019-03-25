package com.finewe.kit;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 映射枚举
 * 
 * @author xiandafu
 *
 */
public class EnumKit {
	  
	@SuppressWarnings("rawtypes")
	private static Map<Class, EnumConfig> cache = new HashMap<Class, EnumConfig>();
	
	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> boolean contains(T c, T ...values) {
		for(T v:values) {
			if(c == v) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获得枚举，根据EnumMapping 注解，如果没有，则使用枚举名称
	 * @param c   枚举类
	 * @param value  参考值
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T extends Enum<T>> T getEnumByValue(Class<T> c, Object value) {
		if (!c.isEnum()) {
			 throw new IllegalArgumentException(c.getName());
		}
		EnumConfig config = cache.get(c);
		if (config == null) {
			init(c);
			config = cache.get(c);
		}
		//测试 SQLServer 数据库的 tinyint 类型 会被转为 Short 而如果封装时Key的类型为Integer 则无法取出
		if(Short.class == value.getClass()) {
			value = ((Short)value).intValue();
		}
		
		Enum e = config.map.get(value);
		
		if(e == null) {
			return null;
		}
		
		if(!e.getDeclaringClass().equals(c)) {
			throw new RuntimeException("枚举类型不匹配， from " + e.getDeclaringClass().getName() + " to: " + c.getName() );
		} 
		return (T) e;
		
	}
	
	/**
	 * 获得枚举，根据EnumMapping 注解，如果没有，则使用枚举名称
	 * @param c   枚举类
	 * @param value  参考值
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T getEnum(Class<T> c, Object value) {
		if (!c.isEnum()) {
			 throw new IllegalArgumentException(c.getName());
		}
		EnumConfig config = cache.get(c);
		if (config == null) {
			init(c);
			config = cache.get(c);
		}
		//测试 SQLServer 数据库的 tinyint 类型 会被转为 Short 而如果封装时Key的类型为Integer 则无法取出
		if(Short.class == value.getClass()) {
			value = ((Short)value).intValue();
		}
		
		Enum e = config.map.get(value);
		if(!e.getDeclaringClass().equals(c)) {
			throw new RuntimeException("枚举类型不匹配， from " + e.getDeclaringClass().getName() + " to: " + c.getName() );
		} 
		return (T) e;
		
	}
	
	@SuppressWarnings("rawtypes")
	public static Object getValueByEnum(Object en) {
		if(en==null) {
			return null;
		}
		Class c = en.getClass();
		
		EnumConfig config = cache.get(c);
		if (config == null) {
			
			init(c);
			config = cache.get(c);
		}
		
		return config.dbMap.get(en);

	}
	
	@SuppressWarnings("rawtypes")
	private static void initNoAnotation(Class c){
		Map<Object, Enum> map = new HashMap<Object, Enum>();
		Map<Enum, Object> map2 = new HashMap<>(); // db
		Enum[] temporaryConstants = getEnumValues(c);
		for (Enum e : temporaryConstants) {
			//用enum的名称
			Object key = e.name();
			map.put(key, e);
			map2.put(e, key);
		}
		EnumConfig config = new EnumConfig(map, map2);
		cache.put(c, config);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void init(Class c){
		try {
			EnumMapping db = (EnumMapping) c.getAnnotation(EnumMapping.class);
			if(db==null){
				//todo, use default
				initNoAnotation(c);
			}else{
				String name = db.value();
				//参考Hibernate 对简单 Enum 进行支持
				String getter = EnumMapping.EnumType.STRING.equals(name)?"toString":EnumMapping.EnumType.ORDINAL.equals(name)?"ordinal":"get" + StringKit.capitalize(name);
				Method m = c.getMethod(getter, new Class[] {});
				Map<Object, Enum> map = new HashMap<Object, Enum>();
				Map<Enum, Object> map2 = new HashMap(); // db
				Enum[] temporaryConstants = getEnumValues(c);
				for (Enum e : temporaryConstants) {
					
					Object key = m.invoke(e, new Object[] {});
					map.put(key, e);
					map2.put(e, key);
				}
				EnumConfig config = new EnumConfig(map, map2);
				cache.put(c, config);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static class EnumConfig {
		Map<Object, Enum> map = new HashMap<Object, Enum>();
		Map<Enum, Object> dbMap = new HashMap<>(); // db
		public EnumConfig(Map<Object, Enum> map, Map<Enum, Object> dbMap) {
			this.map = map;
			this.dbMap = dbMap;
		}

	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Enum[] getEnumValues(Class c){
		try {
			final Method values = c.getMethod("values");
			java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<Void>() {
				@Override
				public Void run() {
					values.setAccessible(true);
					return null;
				}
			});
			Enum[] temporaryConstants;
			temporaryConstants = (Enum[]) values.invoke(null);
			return temporaryConstants;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
		
	}
	
	
	public static void main(String[] args){
		Color c = Color.RED;
		Object value = EnumKit.getValueByEnum(c);
		System.out.println(value);
		
//		String a = "BLUE" ;
		Color e = (Color)EnumKit.getEnumByValue(Color.class, 1);
		System.out.println(e);
		
		System.out.println(contains( Color.from(3),  Color.RED, Color.BLUE, Color.GREEN));
		System.out.println(Color.from(3).contains(Color.RED, Color.BLUE, Color.GREEN));
	}
	
	@EnumMapping("value")
	public enum Color { 
		RED("RED",1),BLUE ("BLUE",2), GREEN ("GREEN",3);
		private String name;  
		private int value;
		private Color(String name, int value) {  
		    this.name = name;  
		    this.value = value;  
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getValue() {
			return value;
		}
		public void setValue(int value) {
			this.value = value;
		}  
		public static Color from(Integer value) {
			return EnumKit.getEnumByValue(Color.class, value);
		}
		public boolean contains(Color ...values) {
			return EnumKit.contains(this, values);
		}
	} 
	
}
