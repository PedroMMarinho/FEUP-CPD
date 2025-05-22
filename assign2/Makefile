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
.PHONY: all build user1 user2 server clean-users clean-server clean-code clean

all: build

build:
	@echo "Building project with Gradle..."
	cd code && ./gradlew build
	@echo "Build completed."

user1:
	@echo "Creating and entering user1 directory..."
	mkdir -p user1
	cd user1 && java -cp ../$(CLASSPATH):../$(RESOURCEPATH) $(CLIENT_CLASS) $(HOST) $(PORT)

user2:
	@echo "Creating and entering user2 directory..."
	mkdir -p user2
	cd user2 && java -cp ../$(CLASSPATH):../$(RESOURCEPATH) $(CLIENT_CLASS) $(HOST) $(PORT)

server:
	@echo "Starting ChatServer..."
	java -cp $(CLASSPATH):$(RESOURCEPATH) $(SERVER_CLASS) $(PORT)

clean-users:
	@echo "Removing user directories..."
	rm -rf user1 user2
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
