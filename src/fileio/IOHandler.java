package fileio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public final class IOHandler {
    public static IOHandler instance;

    private final String outPath;
    private final String inPath;

    @Getter
    private Input inputData;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Getter
    private final ArrayNode output = objectMapper.createArrayNode();

    private final ArrayList<ObjectNode> objectNodes = new ArrayList<ObjectNode>();
    private final ArrayList<ArrayNode> arrayNodes = new ArrayList<ArrayNode>();

    private IOHandler(final String inPath,
                      final String outPath) {
        this.inPath = inPath;
        this.outPath = outPath;
    }

    public static void instantiateIOHandler(final String inPath,
                                            final String outPath) {
        instance = new IOHandler(inPath, outPath);
    }

    //public static IOHandler getInstance() {
    //    if (instance == null) {
    //        throw new RuntimeException("IOHandler not initialized.");
    //    }
    //    return instance;
    //}

    public ObjectNode createObjectNodeFromObject(final Object obj) throws IllegalAccessException {
        ObjectNode node = objectMapper.createObjectNode();
        if (obj instanceof SerializeHandler s) {
            ArrayList<SerializableField> fields = s.getSerializableFields();
            for (SerializableField serializableField : fields) {
                node.put(serializableField.getLabel(), objectMapper.valueToTree(serializableField.getValue()));
            }
            System.out.println();
        }
        return node;
    }

    public void writeJsonNodeToObject(final String name,
                                      final JsonNode node) {
        objectNodes.get(0).put(name, node);
    }

    public <T> ArrayNode createArrayNodeFromArrayOfObjects(final ArrayList<T> array) throws IllegalAccessException {
        ArrayNode node = objectMapper.createArrayNode();
        for (Object obj : array) {
            if (obj instanceof ArrayList) {
                node.add(createArrayNodeFromArrayOfObjects((ArrayList<?>) obj));
            } else {
                node.add(createObjectNodeFromObject(obj));
            }
        }
        return node;
    }

    public void handleInput() throws IOException {
        inputData = objectMapper.readValue(new File(inPath), Input.class);
    }

    public void handleOutput() throws IOException {
        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(new File(outPath), output);
    }

    public void beginObject() {
        objectNodes.add(0, objectMapper.createObjectNode());
    }

    public void endObject() {
        objectNodes.remove(0);
    }

    public void writeToObject(final String name,
                              final String value) {
        if (objectNodes.isEmpty()) {
            throw new RuntimeException("IOHandler no objectNodes.");
        }
        objectNodes.get(0).put(name, value);
    }

    public void writeToObject(final String name,
                              final int value) {
        if (objectNodes.isEmpty()) {
            throw new RuntimeException("IOHandler no objectNodes.");
        }
        objectNodes.get(0).put(name, value);
    }

    public void writeObjectToOutput() {
        if (objectNodes.isEmpty()) {
            throw new RuntimeException("IOHandler no objectNodes.");
        }
        output.add(objectNodes.get(0));
    }
}
