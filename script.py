import os 

os.system("g++ -O2 -fopenmp -o output/out matrixproduct.cpp -lpapi")
os.system("./output/out test")
