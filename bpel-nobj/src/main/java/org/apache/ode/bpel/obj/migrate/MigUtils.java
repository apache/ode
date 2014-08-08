package org.apache.ode.bpel.obj.migrate;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MigUtils {
	public static List<Field> getAllFields(Class cls) {
		List<Field> fields = getFieldsRec(cls.getSuperclass(), new ArrayList<Field>());
		fields.addAll(Arrays.asList(cls.getDeclaredFields()));
		return fields;
	}
	/**
	 * get fields that are accessible to its sub-classes.
	 * @param cls
	 * @param fields
	 * @return
	 */
	private static List<Field> getFieldsRec(Class cls, ArrayList<Field> fields) {
		if (cls != null){
			Field[] fs = cls.getDeclaredFields();
			for (Field f : fs){
				if ((f.getModifiers() & Modifier.PRIVATE) == 0){
					fields.add(f);
				}
			}
			getFieldsRec(cls.getSuperclass(), fields);
		}
		return fields;
	}
}
