package fileio;

import lombok.Getter;

@Getter
public class SerializableField {
    private final String label;
    private final Object value;

    public SerializableField(final String label,
                             final Object value) {
        this.label = label;
        this.value = value;
    }
}
