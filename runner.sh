#!/bin/bash

# Function to stop all running processes
cleanup() {
    echo "Stopping all running Java processes..."
    # Kill all background processes (jobs)
    kill $p1_1 $p1_2 $p1_3 $p2_1 $p3_1 $c1_1 $c1_2 $c2_1 $c3_1 $c4_1
    exit 0
}

# Trap SIGINT (Ctrl+C) to call the cleanup function
trap cleanup SIGINT

# Publisher 1
java -jar artifacts/Type1Publisher.jar & p1_1=$!
java -jar artifacts/Type1Publisher.jar & p1_2=$!
java -jar artifacts/Type1Publisher.jar & p1_3=$!

# Publisher 2
java -jar artifacts/Type2Publisher.jar & p2_1=$!

# Publisher 3
java -jar artifacts/Type3Publisher.jar & p3_1=$!

# Consumer 1
java -jar artifacts/Type1Consumer.jar & c1_1=$!
java -jar artifacts/Type1Consumer.jar & c1_2=$!

# Consumer 2
java -jar artifacts/Type2Consumer.jar & c2_1=$!

# Consumer 3
java -jar artifacts/Type3Consumer.jar & c3_1=$!

# Consumer 4
java -jar artifacts/Type4Consumer.jar & c4_1=$!


# Wait for any of the background jobs to finish or be cancelled
wait $p1_1
wait $c4_1

