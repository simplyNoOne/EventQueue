#!/bin/bash

# Function to stop all running processes
cleanup() {
    echo "Stopping all running Java processes..."
    # Kill all background processes (jobs)
    kill $pid1
    exit 0
}

# Trap SIGINT (Ctrl+C) to call the cleanup function
trap cleanup SIGINT

# Run the JAR file three times in the background
java -jar Type1Publisher.jar & pid1=$!

# Wait for any of the background jobs to finish or be cancelled
wait $pid1

