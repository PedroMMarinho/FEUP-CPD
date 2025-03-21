package main

import (
	"fmt"
	"time"
	"os"
)

func OnMult(n int) float64{
	matrixA := make([]float64, n*n)
	matrixB := make([]float64, n*n)
	matrixC := make([]float64, n*n)

	for i := 0; i < n; i++ {
		for j := 0; j < n; j++ {
			matrixA[i*n+j] = 1.0
		}
	}

	for i := 0; i < n; i++ {
		for j := 0; j < n; j++ {
			matrixB[i*n+j] = float64(i) + 1.0
		}
	}



	start := time.Now()

	var temp float64
	for i := 0; i < n; i++ {
		for j := 0; j < n; j++ {
			temp = 0
			for k := 0; k < n; k++ {
				temp += float64(matrixA[i*n+k] * matrixB[k*n+j])
			}
			matrixC[i*n+j] = temp

		}
	}

	end := time.Now()

	elapsed := end.Sub(start).Seconds()

	/*
	fmt.Println("Time: ", elapsed, " seconds")
	fmt.Println("Result matrix: ")
	for i := 0; i < 1; i++ {
		for j := 0; j < min(10, n); j++ {
			fmt.Print(matrixC[j], " ")
		}
	}
	fmt.Println()
	*/
	return elapsed

}

func OnMultLine(n int) float64 {
	matrixA := make([]float64, n*n)
	matrixB := make([]float64, n*n)
	matrixC := make([]float64, n*n)

	for i := 0; i < n; i++ {
		for j := 0; j < n; j++ {
			matrixA[i*n+j] = 1.0
		}
	}

	for i := 0; i < n; i++ {
		for j := 0; j < n; j++ {
			matrixB[i*n+j] = float64(i) + 1.0
		}
	}

	for i := 0; i < n; i++ {
		for j := 0; j < n; j++ {
			matrixC[i*n+j] = 0
		}
	}

	start := time.Now()
	var temp float64
	for i := 0; i < n; i++ {
		for j := 0; j < n; j++ {
			temp = matrixA[i*n+j]
			for k := 0; k < n; k++ {
				matrixC[i*n+k] += temp * matrixB[j*n+k]
			}
		}
	}

	end := time.Now()

	elapsed := end.Sub(start).Seconds()
	/*
	fmt.Println("Time: ", elapsed, " seconds")
	fmt.Println("Result matrix: ")
	for i := 0; i < 1; i++ {
		for j := 0; j < min(10, n); j++ {
			fmt.Print(matrixC[j], " ")
		}
	}
	fmt.Println()
	*/
	return elapsed
}

func OnMultBlockLine(n int, bkSize int) float64 {
	matrixA := make([]float64, n*n)
	matrixB := make([]float64, n*n)
	matrixC := make([]float64, n*n)

	for i := 0; i < n; i++ {
		for j := 0; j < n; j++ {
			matrixA[i*n+j] = 1.0
		}
	}

	for i := 0; i < n; i++ {
		for j := 0; j < n; j++ {
			matrixB[i*n+j] = float64(i) + 1.0
		}
	}

	for i := 0; i < n; i++ {
		for j := 0; j < n; j++ {
			matrixC[i*n+j] = 0
		}
	}

	start := time.Now()

	for bi := 0; bi < n; bi += bkSize {
		for bj := 0; bj < n; bj += bkSize {
			for bk := 0; bk < n; bk += bkSize {
				minValueI := min(bi+bkSize, n)
				minValueJ := min(bj+bkSize, n)
				minValueK := min(bk+bkSize, n)

				for i := bi; i < minValueI; i++ {
					for j := bj; j < minValueJ; j++ {
						sum_value := matrixA[j+i*n]
						for k := bk; k < minValueK; k++ {
							matrixC[k+i*n] += sum_value * matrixB[k+j*n]
						}
					}
				}
			}
		}
	}

	end := time.Now()

	elapsed := end.Sub(start).Seconds()

	fmt.Println("Time: ", elapsed, " seconds")
	fmt.Println("Result matrix: ")
	for i := 0; i < 1; i++ {
		for j := 0; j < min(10, n); j++ {
			fmt.Print(matrixC[j], " ")
		}
	}
	fmt.Println()

	return elapsed
}

func OnMultBlock(n, bkSize int) float64 {
	matrixA := make([]float64, n*n)
	matrixB := make([]float64, n*n)
	matrixC := make([]float64, n*n)

	for i := 0; i < n; i++ {
		for j := 0; j < n; j++ {
			matrixA[i*n+j] = 1.0
		}
	}

	for i := 0; i < n; i++ {
		for j := 0; j < n; j++ {
			matrixB[i*n+j] = float64(i) + 1.0
		}
	}

	for i := 0; i < n; i++ {
		for j := 0; j < n; j++ {
			matrixC[i*n+j] = 0
		}
	}

	start := time.Now()
	var temp float64
	for bi := 0; bi < n; bi += bkSize {
		for bj := 0; bj < n; bj += bkSize {
			for bk := 0; bk < n; bk += bkSize {
				minValueI := min(bi+bkSize, n)
				minValueJ := min(bj+bkSize, n)
				minValueK := min(bk+bkSize, n)

				for i := bi; i < minValueI; i++ {
					for j := bj; j < minValueJ; j++ {
						temp = 0
						for k := bk; k < minValueK; k++ {
							temp += matrixA[i*n+k] * matrixB[k*n+j]
						}
						matrixC[i*n+j] += temp
					}
				}
			}
		}
	}

	end := time.Now()

	elapsed := end.Sub(start).Seconds()

	fmt.Println("Time: ", elapsed, " seconds")
	fmt.Println("Result matrix: ")
	for i := 0; i < 1; i++ {
		for j := 0; j < min(10, n); j++ {
			fmt.Print(matrixC[j], " ")
		}
	}
	fmt.Println()

	return elapsed
}

func chooseBlockFunction(n int, blockSize int) {
	var op int
	fmt.Println("1. Normal Block Matrix Multiplication")
	fmt.Println("2. Block Matrix Multiplication with Inline Multiplication")
	fmt.Print("Selection?: ")
	fmt.Scan(&op)

	switch op {
	case 1:
		OnMultBlock(n, blockSize)
	case 2:
		OnMultBlockLine(n, blockSize)
	default:
		fmt.Println("Invalid Input")
	}
}

func main() {

	args := os.Args[1:]
	var op int
	var dimensions int
	var bkSize int

	if( len(args) > 0 && args[0] == "test"){
		handleTestCases();
	}

	for {
		fmt.Println("1. Multiplication")
		fmt.Println("2. Line Multiplication")
		fmt.Println("3. Block Multiplication")
		fmt.Println("0. Exit Program")
		fmt.Print("Selection?: ")
		fmt.Scan(&op)

		if op == 0 {
			break
		}

		fmt.Print("Dimension: lins=cols ? ")
		fmt.Scan(&dimensions)

		switch op {
		case 1:
			OnMult(dimensions)
		case 2:
			OnMultLine(dimensions)
		case 3:
			fmt.Print("Block Size? ")
			fmt.Scan(&bkSize)
			chooseBlockFunction(dimensions, bkSize)
		}

	}
}

func handleTestCases() {

	file, err := os.OpenFile("../docs/data_go.csv", os.O_APPEND|os.O_WRONLY, 0644)
	if err != nil {
		fmt.Println("Error:", err)
		return
	}
	defer file.Close()

	
	fmt.Print("== Normal multiplication tests ==")
	for n:= 600; n <= 3000; n += 400{
		testFunc(file, OnMult,"Normal Mult",n)
	}
	fmt.Println(" Complete!")
	
	
	fmt.Print("== Line multiplication tests ==")
	for n := 600; n <= 3000; n+= 400{
		testFunc(file, OnMultLine,"Inline Mult",n)
	}
	fmt.Println(" Complete!")
	

	/*
	fmt.Print("== Normal Block multiplication tests ==")
	for n := 4096; n <= 10240; n+= 2048{
		for bkSize := 128; bkSize <= 512; bkSize*=2{
			testBlockFunc(OnMultBlock,n,bkSize)
		}
	}
	fmt.Println(" Complete!")

	fmt.Print("== Block multiplication with Inline Multiplication tests ==")
	for n := 4096; n <= 10240; n+= 2048{
		for bkSize := 128; bkSize <= 512; bkSize*=2{
			testBlockFunc(OnMultBlockLine,n,bkSize)
		}
	}
	fmt.Println(" Complete!")
	*/

}

func testFunc(file *os.File, f func(int) float64, funcName string, n int) {
	avg := 0.0

	for i:= 0; i < 30 ; i++{
		t:= f(n)
		avg += t

		line := fmt.Sprintf("%s,%d,%.6f\n", funcName, n, t)

		_, err := file.WriteString(line)
		if err != nil {
			fmt.Println("Error:", err)
		}
	
	}

	avg /= 30

	avgLine := fmt.Sprintf("%s,%d,Average,%.6f\n\n", funcName, n, avg)
	_, err := file.WriteString(avgLine)
	if err != nil {
		fmt.Println("Error writing average:", err)
	}

}

func testBlockFunc(f func(int,int) float64, n int, bkSize int) {

	avg := 0.0

	for i:= 0; i < 30; i++ {
		avg += f(n,bkSize)
	}

	avg /= 3.0

	// put here code for csv

}
