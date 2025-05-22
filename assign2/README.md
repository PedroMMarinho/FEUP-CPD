# Chat Application 

This is a simple client-server chat application written in Java. 

---

## Project Structure

```plaintext
.
├── Makefile # Automation commands for build/run/clean
├── code/ # Java source code and Gradle files
├── user1/, user2/, ... # Created at runtime for each chat client
└── docs/ # Documentation files
```

## ✅ Requirements

- Java JDK 8+
- GNU Make
- Internet access (for Gradle dependencies, on first build)


> No need to install Gradle globally — the project uses the Gradle wrapper (`gradlew`).

To run the AI features: 

- Docker 
- Ollama docker image 

---

## Build the Project

To compile the Java source using Gradle:

```bash
make build
```
## Running the AI

To start the VM:

```bash
sudo docker run -d -v ollama:/root/.ollama -p 11434:11434 --name ollama14 ollama/ollama 
```

## Running the Project

To run the server:

```bash
make server
```

To run the client:

```bash
make user N=X
```
> Where `X` is any number (1, 2, ...). When running multiple clients, use different numbers for each client.


## Cleaning

To clean user directories:
```bash
make clean-users
```

To reset server user data:
```bash
make clean-server
```

To clean only the compiled code:
```bash
make clean-code
```

To clean the project:

```bash
make clean
```

## User Guide

On startup, the user is prompted to LOGIN or REGISTER.
- **LOGIN**: Enter your username and password to access your account.
- **REGISTER**: Create a new account by providing a username and password.

Once logged in, the user can:

- **JOIN `<room>`**: Join a chat room with the specified name. If the room doesn't exist, it will be created.
- **JOIN_AI `<room>`**: Join a chat room with AI assistance with the specified name. If the room doesn't exist, it will be created.
- **REFRESH**: Refresh the list of available chat rooms.
- **LOGOUT**: Log out of the current account.

While in a chat room, the user can:

- Send messages to the room by typing them in the input field.
- **/leave**: Leave the current chat room.
- **/help**: Display a list of available commands.
- **/list**: Display a list of users in the current chat room.

If the user is in an AI-assisted chat room, they can also:

- **/ai `<prompt>`**: Prompt the AI. The AI will respond with an answer visible to all users in the room.

