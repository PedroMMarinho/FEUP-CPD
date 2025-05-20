#!/bin/bash

# Create and enter the directory
mkdir -p user1
cd user1

# Run the client from ../code/build
java -cp ../code/build/classes/java/main:../code/build/resources/main client.ChatClient localhost 1234
