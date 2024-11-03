package fileio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class IOHandler {
    private static IOHandler instance;

    private final String outPath;
    private final String inPath;

    @Getter
    private Input inputData;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Getter
    private final ArrayNode output = objectMapper.createArrayNode();

    private final ArrayList<ObjectNode> objectNodes = new ArrayList<ObjectNode>();
    private final ArrayList<ArrayNode> arrayNodes = new ArrayList<ArrayNode>();

    private IOHandler(String inPath, String outPath) {
        this.inPath = inPath;
        this.outPath = outPath;
    }

    public static void instantiateIOHandler(String inPath, String outPath) {
        instance = new IOHandler(inPath, outPath);
    }

    public static IOHandler getInstance() {
        if (instance == null)
            throw new RuntimeException("IOHandler not initialized.");
        return instance;
    }

    public ObjectNode createObjectNodeFromObject(Object obj) throws IllegalAccessException {
        System.out.println("CREATE OBJECT");
        ObjectNode node = objectMapper.createObjectNode();
        if (obj instanceof SerializeHandler s) {
            ArrayList<SerializableField> fields = s.getSerializableFields();
            for (SerializableField serializableField : fields) {
                System.out.println(serializableField.label);
                node.put(serializableField.label, objectMapper.valueToTree(serializableField.value));
            }
        }
        return node;
    }

    public void writeObjectNodeToObject(String name, ObjectNode node) {
        objectNodes.get(0).put(name, node);
    }

    public ArrayNode createArrayNodeFromArrayOfObjects(ArrayList<Object> array) throws IllegalAccessException {
        ArrayNode node = objectMapper.createArrayNode();
        for (Object obj : array)
            node.add(createObjectNodeFromObject(obj));
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

    public void beginArray() {
        arrayNodes.add(0, objectMapper.createArrayNode());
    }

    public void endArray() {
        arrayNodes.remove(0);
    }

    public void writeToObject(String name, String value) {
        if (objectNodes.isEmpty())
            throw new RuntimeException("IOHandler no objectNodes.");
        objectNodes.get(0).put(name, value);
    }

    public void writeToObject(String name, int value) {
        if (objectNodes.isEmpty())
            throw new RuntimeException("IOHandler no objectNodes.");
        objectNodes.get(0).put(name, value);
    }

    public void writeToArray(String value) {
        if (arrayNodes.isEmpty())
            throw new RuntimeException("IOHandler no arrayNodes.");
        arrayNodes.get(0).add(value);
    }

    public void writeObjectToObject(String name) {
        if (objectNodes.size() < 2)
            throw new RuntimeException("IOHandler no 2 objectNodes.");
        objectNodes.get(1).put(name, objectNodes.get(0));
    }

    public void writeArrayToObject(String name) {
        if (arrayNodes.isEmpty() || objectNodes.isEmpty())
            throw new RuntimeException("IOHandler no arrayNodes or objectNodes.");
        objectNodes.get(0).put(name, arrayNodes.get(0));
    }

    public void writeObjectToArray() {
        if (arrayNodes.isEmpty() || objectNodes.isEmpty())
            throw new RuntimeException("IOHandler no arrayNodes or objectNodes.");
        arrayNodes.get(0).add(objectNodes.get(0));
    }

    public void writeArrayToArray() {
        if (arrayNodes.size() < 2)
            throw new RuntimeException("IOHandler no 2 arrayNodes.");
        arrayNodes.get(1).add(arrayNodes.get(0));
    }

    public void writeObjectToOutput() {
        if (objectNodes.isEmpty())
            throw new RuntimeException("IOHandler no objectNodes.");
        output.add(objectNodes.get(0));
    }

    public void writeArrayToOutput() {
        if (arrayNodes.isEmpty())
            throw new RuntimeException("IOHandler no arrayNodes.");
        output.add(arrayNodes.get(0));
    }
}