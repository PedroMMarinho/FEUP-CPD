# Compiler & Flags
CXX = g++
CXXFLAGS = -O2 -fopenmp -lpapi

# Directories
CPP_DIR = cpp
GO_DIR = go
JAVA_DIR = java
OUTPUT_DIR = output

# Source files
CPP_SRC = $(CPP_DIR)/matrixproduct.cpp
CPP_BIN = $(OUTPUT_DIR)/matrixproduct_cpp

GO_BIN = $(OUTPUT_DIR)/matrixproduct_go

JAVA_SRC = $(JAVA_DIR)/MatrixProduct.java
JAVA_CLASS = MatrixProduct

# Default target
all: build_cpp build_go build_java run_all

# C++ build target
build_cpp:
	$(CXX) src/$(CPP_SRC) -o $(CPP_BIN) $(CXXFLAGS)

# Go build target using the Go source file directly
build_go:
	go build -o $(OUTPUT_DIR)/matrixproduct_go ./src/$(GO_DIR)/matrixproduct.go

# Java build target
build_java:
	javac -d $(OUTPUT_DIR) src/$(JAVA_SRC)

# Run all test cases
run_all: run_cpp run_go run_java

# Run C++ test
run_cpp:
	@echo "Running C++ test cases..."
	./$(CPP_BIN) test

# Run Go test
run_go:
	@echo "Running Go test cases..."
	./$(GO_BIN) test

# Run Java test
run_java:
	@echo "Running Java test cases..."
	java -cp $(OUTPUT_DIR) $(JAVA_CLASS) test

# Clean generated files (but keep the output/ dir)
clean:
	rm -f $(OUTPUT_DIR)/matrixproduct_cpp
	rm -f $(OUTPUT_DIR)/matrixproduct_go
	rm -f $(OUTPUT_DIR)/*.class

.PHONY: all build_cpp build_go build_java run_all run_cpp run_go run_java clean
