# Makefile

# Paths
BUILD_DIR=code/build
CLASSPATH=$(BUILD_DIR)/classes/java/main
RESOURCEPATH=$(BUILD_DIR)/resources/main
GRADLE=code/gradlew
SERVER_CLASS=server.ChatServer
CLIENT_CLASS=client.ChatClient
HOST=localhost
PORT=1234

# Targets
.PHONY: all build user server clean-users clean-server clean-code clean

all: build

build:
	@echo "Building project with Gradle..."
	cd code && ./gradlew build
	@echo "Build completed."

user:
	@if [ -z "$(N)" ]; then \
		echo "Please specify user number with 'make user N=1'"; \
		exit 1; \
	fi
	stty -echo -icanon min 1 time 0
	@echo "Creating and entering user$(N) directory..."
	mkdir -p user$(N)
	cd user$(N) && java -cp ../$(CLASSPATH):../$(RESOURCEPATH) $(CLIENT_CLASS) $(HOST) $(PORT)
	stty sane

server:
	@echo "Starting ChatServer..."
	java -cp $(CLASSPATH):$(RESOURCEPATH) $(SERVER_CLASS) $(PORT)

ai-server:
	@echo "Starting ChatServer with AI functionalities..."
	# Check if container exists; if yes, start it, else run it
	@if sudo docker ps -a --format '{{.Names}}' | grep -q '^ollama14$$'; then \
		echo "Starting existing ollama14 container..."; \
		sudo docker start ollama14; \
	else \
		echo "Running new ollama14 container..."; \
		sudo docker run -d -v ollama:/root/.ollama -p 11434:11434 --name ollama14 ollama/ollama; \
	fi
	@echo "Ensuring llama3 model is downloaded..."
	sudo docker exec -u root ollama14 ollama run llama3 || true
	@echo "Starting Java ChatServer..."
	java -cp $(CLASSPATH):$(RESOURCEPATH) $(SERVER_CLASS) $(PORT)
	@echo "Stopping ollama14 container..."
	sudo docker stop ollama14



clean-users:
	@echo "Removing user directories..."
	rm -rf user*
	@echo "User directories removed."

clean-server:
	@echo "Removing users from server..."
	rm -f code/data/serverData/users.txt
	touch code/data/serverData/users.txt
	@echo "Server user data removed."

clean-code:
	@echo "Cleaning Gradle build output..."
	cd code && ./gradlew clean
	@echo "Gradle clean completed."

clean:
	@$(MAKE) clean-users
	@$(MAKE) clean-server
	@$(MAKE) clean-code
