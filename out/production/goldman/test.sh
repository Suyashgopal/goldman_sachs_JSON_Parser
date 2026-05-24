#!/bin/bash

# Compile the project
echo "Compiling JsonToTsCompiler..."
javac com/typegen/*.java

if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

echo "Compilation successful!"
echo ""

# Run the tool with sample data
echo "Running: java com.typegen.Main sample.json output.d.ts User"
java com.typegen.Main sample.json output.d.ts User

echo ""
echo "Generated output.d.ts:"
echo "---"
cat output.d.ts
