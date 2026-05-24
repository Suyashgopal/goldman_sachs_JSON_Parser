package com.typegen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


// Entry point for JsonToTsCompiler CLI tool.
 //Usage: java com.typegen.Main <input.json> <output.d.ts> <RootTypeName>

public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java com.typegen.Main <input.json> <output.d.ts> <RootTypeName>");
            System.exit(1);
        }

        String inputPath = args[0];
        String outputPath = args[1];
        String rootTypeName = args[2];

        try {
            // Read input JSON file
            String jsonContent = new String(Files.readAllBytes(Paths.get(inputPath)));
            
            // Parse JSON into Java collections
            JsonParser parser = new JsonParser(jsonContent);
            Object parsed = parser.parse();
            
            // Generate TypeScript declarations
            TypeEngine engine = new TypeEngine();
            String tsOutput = engine.generate(parsed, rootTypeName);
            
            // Write output to file
            Files.write(Paths.get(outputPath), tsOutput.getBytes());
            
            System.out.println("Successfully generated TypeScript declarations: " + outputPath);
            
        } catch (IOException e) {
            System.err.println("File error: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
