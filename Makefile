# Compiler & Flags
CXX = g++
CXXFLAGS = -O2 -fopenmp -lpapi

# Directories
CPP_DIR = cpp
GO_DIR = go
JAVA_DIR = java
OUTPUT_DIR = output
PYTHON_DIR = python

# Source files
CPP_SRC = $(CPP_DIR)/matrixproduct.cpp
CPP_BIN = $(OUTPUT_DIR)/matrixproduct_cpp

GO_BIN = $(OUTPUT_DIR)/matrixproduct_go

JAVA_SRC = $(JAVA_DIR)/MatrixProduct.java
JAVA_CLASS = MatrixProduct

PYTHON_SRC = src/$(PYTHON_DIR)/matrixproduct.py

# Default target
all_tests: build_cpp build_go build_java run_all_tests

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
run_all_tests: run_cpp_test run_go_test run_java_test run_python_test

# Run C++ test
run_cpp_test:
	@echo "Running C++ test cases..."
	./$(CPP_BIN) test

# Run Go test
run_go_test:
	@echo "Running Go test cases..."
	./$(GO_BIN) test

# Run Java test
run_java_test:
	@echo "Running Java test cases..."
	java -cp $(OUTPUT_DIR) $(JAVA_CLASS) test

run_python_test: 
	@echo "Running Python test cases..."
	python $(PYTHON_SRC) test

# Run C++ without the test argument
run_cpp:
	@echo "Running C++ without test argument..."
	./$(CPP_BIN)

# Run Go without the test argument
run_go:
	@echo "Running Go without test argument..."
	./$(GO_BIN)

# Run Java without the test argument
run_java:
	@echo "Running Java without test argument..."
	java -cp $(OUTPUT_DIR) $(JAVA_CLASS)

run_python: 
	@echo "Running Python without test argument..."
	python $(PYTHON_SRC)

# Clean generated files (but keep the output/ dir)
clean:
	rm -f $(OUTPUT_DIR)/matrixproduct_cpp
	rm -f $(OUTPUT_DIR)/matrixproduct_go
	rm -f $(OUTPUT_DIR)/*.class

.PHONY: all build_cpp build_go build_java run_all run_cpp run_go run_java clean
