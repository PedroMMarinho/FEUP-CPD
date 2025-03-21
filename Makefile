# Compiler & Flags
CXX = g++
CXXFLAGS = -O2 -fopenmp -lpapi

# Files
CPP_SRC = matrixproduct.cpp
CPP_BIN = output/matrixproduct_cpp

GO_SRC = go/matrixproduct.go
GO_BIN = output/main_go

JAVA_SRC = MatrixProduct.java
JAVA_CLASS = MatrixProduct
JAVA_OUTPUT_DIR = output

# Default target
all: build_cpp build_go build_java run_all

# Build C++ binary
build_cpp:
	$(CXX) $(CPP_SRC) -o $(CPP_BIN) $(CXXFLAGS)

# Build Go binary
build_go:
	go build -o $(GO_BIN) $(GO_SRC)

# Compile Java
build_java:
	javac -d $(JAVA_OUTPUT_DIR) $(JAVA_SRC)

# Run all test cases
run_all: run_cpp run_go run_java

run_cpp:
	@echo "Running C++ test cases..."
	./$(CPP_BIN) test

run_go:
	@echo "Running Go test cases..."
	./$(GO_BIN) test

run_java:
	@echo "Running Java test cases..."
	java -cp $(JAVA_OUTPUT_DIR) $(JAVA_CLASS) test

# Clean generated files (but not the directory itself)
clean:
	rm -f output/matrixproduct_cpp output/main_go
	rm -f output/*.class

.PHONY: all build_cpp build_go build_java run_all run_cpp run_go run_java clean
