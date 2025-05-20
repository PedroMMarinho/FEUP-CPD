#!/bin/bash

# Create and enter the directory
mkdir -p user2
cd user2

# Run the client from ../code/build
java -cp ../code/build/classes/java/main:../code/build/resources/main client.ChatClient localhost 1234
