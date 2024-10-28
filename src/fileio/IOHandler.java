package fileio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.io.IOException;

public class IOHandler {
    private static IOHandler instance;

    private final String outPath;
    private final String inPath;

    private Input inputData;
    private ObjectMapper objectMapper = new ObjectMapper();
    private ArrayNode output = objectMapper.createArrayNode();

    private IOHandler(String inPath, String outPath) {
        this.inPath = inPath;
        this.outPath = outPath;
    }

    public static void instantiateIOHandler(String inPath, String outPath) {
        instance = new IOHandler(inPath, outPath);
    }

    public static IOHandler getInstance() {
        if (instance == null)
            throw new RuntimeException("TextDisplay not initialized");
        return instance;
    }

    public void handleInput() throws IOException {
        inputData = objectMapper.readValue(new File(inPath), Input.class);
    }

    public void handleOutput() throws IOException {
        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(new File(outPath), output);
    }

    public Input getInputData() { return inputData; }

    public ArrayNode getOutput() { return output; }
}