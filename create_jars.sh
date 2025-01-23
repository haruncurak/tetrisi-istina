#!/bin/bash

# Base directory (convert to absolute path)
BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUTPUT_DIR="$BASE_DIR/output"

# Create the output directory if it doesn't exist
mkdir -p "$OUTPUT_DIR"

# Function to compile Java files and create JARs
process_folder() {
    local folder_path="$1"
    # Get the path after 01-22/ to maintain structure but remove the top level
    local relative_path="${folder_path#"$BASE_DIR/01-22/"}"

    # Create a temporary directory for compiling
    temp_dir=$(mktemp -d)

    echo "Processing folder: $folder_path"
    echo "Output will be in: $OUTPUT_DIR/$relative_path"

    # Copy Java and manifest files to the temp directory
    find "$folder_path" -type f \( -name "*.java" -o -name "manifest.txt" \) -exec cp {} "$temp_dir/" \;

    # Compile Java files
    pushd "$temp_dir" > /dev/null
    echo "Contents of temp directory before compile:"
    ls -la
    javac *.java
    if [ $? -eq 0 ]; then
        echo "Compiled Java files in $folder_path successfully."
        echo "Contents of temp directory after compile:"
        ls -la
    else
        echo "Failed to compile Java files in $folder_path."
        popd > /dev/null
        rm -rf "$temp_dir"
        return
    fi

    # Create output directory
    target_dir="$OUTPUT_DIR/$relative_path"
    mkdir -p "$target_dir"
    
    # Create the JAR file
    echo "Creating JAR in: $target_dir"
    if [ -f manifest.txt ]; then
        # Create JAR with manifest
        jar cfm "Tetris.jar" manifest.txt *.class
    else
        # Create JAR without manifest
        echo "Warning: manifest.txt not found"
        jar cf "Tetris.jar" *.class
    fi
    
    # Move the JAR to its final location
    if [ $? -eq 0 ]; then
        cp -v "Tetris.jar" "$target_dir/Tetris.jar"
        echo "Created JAR: $target_dir/Tetris.jar"
        # List contents of the created JAR
        jar tvf "Tetris.jar"
    else
        echo "Failed to create JAR for $folder_path"
    fi

    # Clean up
    popd > /dev/null
    rm -rf "$temp_dir"
}

# Recursively process all folders containing Tetris.java
find "$BASE_DIR" -type f -name "Tetris.java" | while read -r tetris_path; do
    folder_path="$(dirname "$tetris_path")"
    echo "Processing: $folder_path"
    process_folder "$folder_path"
done

echo "All tasks completed. Check the '$OUTPUT_DIR' folder for results."
