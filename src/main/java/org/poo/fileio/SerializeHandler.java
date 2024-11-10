package org.poo.fileio;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

public interface SerializeHandler {
    private static ArrayList<Field> getAllFields(final ArrayList<Field> fields,
                                                 final Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }

    default ArrayList<SerializableField> getSerializableFields() throws IllegalAccessException {
        ArrayList<SerializableField> fields = new ArrayList<>();
        ArrayList<Field> classFields = SerializeHandler.getAllFields(new ArrayList<>(),
                this.getClass());
        for (Field field : classFields) {
            if (field.isAnnotationPresent(SerializeField.class)) {
                field.setAccessible(true);
                fields.add(new SerializableField(field.getAnnotation(SerializeField.class).label(),
                        field.get(this)));
            }
        }
        return fields;
    }
}
