package org.poo.fileio;

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
    ///////////////// FA JAVADOC AICI ------------------------------------------------------
    // Singleton instance directly/publicly accessible to avoid a significant number of
    // getInstance() calls, thus reducing overhead. To avoid instance reassignment, the field
    // is declared "final" with empty parameters that must be manually set to avoid Runtime
    // exceptions.
    public static final IOHandler INSTANCE = new IOHandler();

    @Getter
    private Input inputData;
    private ObjectMapper objectMapper = new ObjectMapper();
    @Getter
    private ArrayNode output = objectMapper.createArrayNode();

    private ArrayList<ObjectNode> objectNodes = new ArrayList<>();

    public JsonNode createNodeFromObject(final Object obj) throws IllegalAccessException {
        return objectMapper.valueToTree(obj);
    }

    public void writeJsonNodeToObject(final String name,
                                      final JsonNode node) {
        objectNodes.getFirst().set(name, node);
    }

    public void resetInstance() {
        objectMapper = new ObjectMapper();
        output = objectMapper.createArrayNode();
        objectNodes = new ArrayList<>();
    }

    public void handleInput(final String inPath) throws IOException {
        inputData = objectMapper.readValue(new File(inPath), Input.class);
    }

    public void handleOutput(final String outPath) throws IOException {
        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(new File(outPath), output);

        resetInstance();
    }

    public void beginObject() {
        objectNodes.addFirst(objectMapper.createObjectNode());
    }

    public void endObject() {
        objectNodes.removeFirst();
    }

    public void writeToObject(final String name,
                              final String value) {
        if (objectNodes.isEmpty()) {
            throw new RuntimeException("IOHandler no objectNodes.");
        }
        objectNodes.getFirst().put(name, value);
    }

    public void writeToObject(final String name,
                              final int value) {
        if (objectNodes.isEmpty()) {
            throw new RuntimeException("IOHandler no objectNodes.");
        }
        objectNodes.getFirst().put(name, value);
    }

    public void writeObjectToOutput() {
        if (objectNodes.isEmpty()) {
            throw new RuntimeException("IOHandler no objectNodes.");
        }
        output.add(objectNodes.getFirst());
    }
}
