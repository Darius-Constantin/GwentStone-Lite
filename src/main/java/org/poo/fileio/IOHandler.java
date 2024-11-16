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
    private static final IOHandler INSTANCE = new IOHandler();

    @Getter
    private Input inputData;
    private ObjectMapper objectMapper = new ObjectMapper();
    @Getter
    private ArrayNode output = objectMapper.createArrayNode();
    /**
     * Stack-like ArrayList to keep track of object nesting.
     */
    private ArrayList<ObjectNode> objectNodes = new ArrayList<>();

    public static IOHandler getInstance() {
        return INSTANCE;
    }

    /**
     * Function used to describe an {@link Object} as a {@link JsonNode}.
     *
     * @param obj The object to be converted to a JsonNode.
     * @return The equivalent JsonNode.
     */
    public JsonNode createNodeFromObject(final Object obj) {
        return objectMapper.valueToTree(obj);
    }

    /**
     * Function used to add a {@link JsonNode} to the first {@link ObjectNode} in
     * {@link #objectNodes}.
     *
     * @param name The name of the field/JsonNode inside the ObjectNode.
     * @param node The JsonNode that will be added.
     * @throws RuntimeException if there is no active ObjectNode in {@link #objectNodes}.
     */
    public void writeJsonNodeToObject(final String name,
                                      final JsonNode node) {
        if (objectNodes.isEmpty()) {
            throw new RuntimeException("IOHandler no objectNodes.");
        }
        objectNodes.getFirst().set(name, node);
    }

    /**
     * Function used to reset the {@link IOHandler} instance once the initial input and eventual
     * output are no longer required. After calling this function, {@link #handleInput} should be
     * called again, but is not required.
     */
    public void resetInstance() {
        objectMapper = new ObjectMapper();
        output = objectMapper.createArrayNode();
        objectNodes = new ArrayList<>();
    }

    /**
     * Function used to retrieve input from a JSON file as the structure of class {@link Input}.
     *
     * @param inPath The path to the input JSON file.
     */
    public void handleInput(final String inPath) throws IOException {
        inputData = objectMapper.readValue(new File(inPath), Input.class);
    }

    /**
     * A function used to write to a JSON file everything present in {@link #output}. Acts as a
     * sort of equivalent of {@code flush} in C.
     * @param outPath The path to the output JSON file.
     */
    public void handleOutput(final String outPath) throws IOException {
        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        System.out.println(outPath);
        objectWriter.writeValue(new File(outPath), output);
        resetInstance();
    }

    /**
     * Function used to nest {@link ObjectNode}s or to create a flat object.
     */
    public void beginObject() {
        objectNodes.addFirst(objectMapper.createObjectNode());
    }

    /**
     * Function used to mark the end of the current {@link ObjectNode}. It can either end the
     * nesting of multiple objects or end a simple, flat object.
     *
     * @throws RuntimeException if there is no active ObjectNode in {@link #objectNodes}.
     */
    public void endObject() {
        if (objectNodes.isEmpty()) {
            throw new RuntimeException("IOHandler no objectNodes.");
        }
        objectNodes.removeFirst();
    }

    /**
     * Function used to write a String to an {@link ObjectNode}.
     *
     * @param name  The name of the new field.
     * @param value The String to be assigned to the new field.
     * @throws RuntimeException if there is no active ObjectNode in {@link #objectNodes}.
     */
    public void writeToObject(final String name,
                              final String value) {
        if (objectNodes.isEmpty()) {
            throw new RuntimeException("IOHandler no objectNodes.");
        }
        objectNodes.getFirst().put(name, value);
    }

    /**
     * Function used to write an integer to an {@link ObjectNode}.
     *
     * @param name  The name of the new field.
     * @param value The integer to be assigned to the new field.
     * @throws RuntimeException if there is no active ObjectNode in {@link #objectNodes}.
     */
    public void writeToObject(final String name,
                              final int value) {
        if (objectNodes.isEmpty()) {
            throw new RuntimeException("IOHandler no objectNodes.");
        }
        objectNodes.getFirst().put(name, value);
    }

    /**
     * Function used to send the current {@link ObjectNode} to the enclosing {@link ArrayNode}
     * {@link #output}. Does not automatically end the current ObjectNode.
     *
     * @throws RuntimeException if there is no active ObjectNode in {@link #objectNodes}.
     */
    public void writeObjectToOutput() {
        if (objectNodes.isEmpty()) {
            throw new RuntimeException("IOHandler no objectNodes.");
        }
        output.add(objectNodes.getFirst());
    }
}
