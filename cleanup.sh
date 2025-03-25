#!/bin/bash

# Output directory to clean
OUTPUT_DIR="./output"

# Check if the output directory exists
if [ -d "$OUTPUT_DIR" ]; then
    echo "Cleaning up the output directory: $OUTPUT_DIR"
    rm -rf "$OUTPUT_DIR"
    echo "Output directory removed."
else
    echo "No output directory found. Nothing to clean."
fi

# Remove all scores.txt files
echo "Removing all scores.txt files..."
find . -name "scores.txt" -type f -delete
echo "All scores.txt files removed."

# Remove any temporary directories (e.g., created by `mktemp`)
echo "Looking for leftover temporary directories..."
find /tmp -type d -name "tmp.*" -mtime +1 -exec rm -rf {} \; 2>/dev/null

echo "Cleanup completed."
