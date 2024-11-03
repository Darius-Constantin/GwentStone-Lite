package fileio;

import java.lang.reflect.Field;

public class SerializableField {
    final public String label;
    final public Object value;

    public SerializableField(String label, Object value) {
        this.label = label;
        this.value = value;
    }
}
