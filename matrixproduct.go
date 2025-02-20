package main

import (
	"fmt"
	"time"
	
)

func OnMult(n int){
	matrixA := make([]float64 , n*n)
	matrixB := make([]float64, n*n)
	matrixC := make([]float64, n*n)
	
	for i:= 0; i < n; i++{
		for j:=0; j < n; j++{
			matrixA[ i* n + j] = 1.0
		}
	}

	for i:= 0; i < n; i++{
		for j:=0; j < n; j++{
			matrixB[ i* n + j] = float64(i) + 1.0
		}
	}
	
	start := time.Now()


	var temp float64
	for i:= 0; i < n; i++{
		for j:= 0; j < n; j++{
			temp = 0
			for k:= 0; k < n; k++{
				temp += float64(matrixA[ i * n + k ] * matrixB[k * n + j ])
			}
			matrixC[ i * n + j]  = temp

		}
	}

	end := time.Now()

	elapsed := end.Sub(start).Seconds()

	fmt.Println("Time: ", elapsed , " seconds" )
	fmt.Println("Result matrix: ")
	for i := 0; i < 1; i++{
		for j:= 0; j < min(10, n); j++{
			fmt.Print(matrixC[j], " ")
		}
	}
	fmt.Println()

	

}

func OnMultLine( n int ){
	matrixA := make([]float64 , n*n)
	matrixB := make([]float64, n*n)
	matrixC := make([]float64, n*n)
	
	for i:= 0; i < n; i++{
		for j:=0; j < n; j++{
			matrixA[ i* n + j] = 1.0
		}
	}

	for i:= 0; i < n; i++{
		for j:=0; j < n; j++{
			matrixB[ i* n + j] = float64(i) + 1.0
		}
	}
	
	for i:= 0; i < n; i++{
		for j:= 0; j < n; j++{
			matrixC[i*n + j] = 0
		}
	}
	
	start := time.Now()
	var temp float64
	for i:= 0; i < n; i++{
		for j:= 0; j < n; j++{
			temp = matrixA[ i * n + j]
			for k := 0; k < n; k++{
				matrixC[i * n + k] += temp * matrixB[j * n + k]
			}
		}
	}

	end := time.Now()

	elapsed := end.Sub(start).Seconds()

	fmt.Println("Time: ", elapsed , " seconds" )
	fmt.Println("Result matrix: ")
	for i := 0; i < 1; i++{
		for j:= 0; j < min(10, n); j++{
			fmt.Print(matrixC[j], " ")
		}
	}
	fmt.Println()
}

func OnMultBlockLine( n int, bkSize int){
	matrixA := make([]float64 , n*n)
	matrixB := make([]float64, n*n)
	matrixC := make([]float64, n*n)
	
	for i:= 0; i < n; i++{
		for j:=0; j < n; j++{
			matrixA[ i* n + j] = 1.0
		}
	}

	for i:= 0; i < n; i++{
		for j:=0; j < n; j++{
			matrixB[ i* n + j] = float64(i) + 1.0
		}
	}
	
	for i:= 0; i < n; i++{
		for j:= 0; j < n; j++{
			matrixC[i*n + j] = 0
		}
	}
	
	start := time.Now()
	
	for bi:= 0; bi < n; bi += bkSize{
		for bj:= 0; bj < n; bj += bkSize{
			for bk:= 0; bk < n; bk += bkSize{
				minValueI := min( bi + bkSize, n)
				minValueJ := min( bj + bkSize, n)
				minValueK := min(bk + bkSize, n)
				
				for i:=bi; i < minValueI; i++{
					for j:= bj; j < minValueJ; j++{
						sum_value := matrixA[ j + i * n]
						for k := bk; k < minValueK; k++{
							matrixC[ k + i * n ] += sum_value * matrixB[ k + j * n]
						}
					}
				}
			}
		}
	}


	end := time.Now()

	elapsed := end.Sub(start).Seconds()

	fmt.Println("Time: ", elapsed , " seconds" )
	fmt.Println("Result matrix: ")
	for i := 0; i < 1; i++{
		for j:= 0; j < min(10, n); j++{
			fmt.Print(matrixC[j], " ")
		}
	}
	fmt.Println()
}

func OnMultBlock(n, bkSize int) {
	matrixA := make([]float64 , n*n)
	matrixB := make([]float64, n*n)
	matrixC := make([]float64, n*n)
	
	for i:= 0; i < n; i++{
		for j:=0; j < n; j++{
			matrixA[ i* n + j] = 1.0
		}
	}

	for i:= 0; i < n; i++{
		for j:=0; j < n; j++{
			matrixB[ i* n + j] = float64(i) + 1.0
		}
	}
	
	for i:= 0; i < n; i++{
		for j:= 0; j < n; j++{
			matrixC[i*n + j] = 0
		}
	}
	
	start := time.Now()
	var temp float64
	for bi:= 0; bi < n; bi += bkSize{
		for bj:= 0; bj < n; bj += bkSize{
			for bk:= 0; bk < n; bk += bkSize{
				minValueI := min( bi + bkSize, n)
				minValueJ := min( bj + bkSize, n)
				minValueK := min(bk + bkSize, n)
				
				for i:=bi; i < minValueI; i++{
					for j:= bj; j < minValueJ; j++{
						temp = 0
						for k := bk; k < minValueK; k++{
							temp += matrixA[i * n + k] * matrixB[k * n + j]
						}
						matrixC[i * n + j] += temp
					}
				}
			}
		}
	}


	end := time.Now()

	elapsed := end.Sub(start).Seconds()

	fmt.Println("Time: ", elapsed , " seconds" )
	fmt.Println("Result matrix: ")
	for i := 0; i < 1; i++{
		for j:= 0; j < min(10, n); j++{
			fmt.Print(matrixC[j], " ")
		}
	}
	fmt.Println()
}

func chooseBlockFunction( n int , blockSize int){
	var op int 
	fmt.Println("1. Normal Block Matrix Multiplication")
	fmt.Println("2. Block Matrix Multiplication with Inline Multiplication")
	fmt.Print("Selection?: ")
	fmt.Scan(&op)

	switch op{
	case 1: 
		OnMultBlock(n,blockSize)
	case 2: 
		OnMultBlockLine(n,blockSize)
	default:
		fmt.Println("Invalid Input")
	}
}



func main() {
	var op int 
	var dimensions int
	var bkSize int 
    for {
		fmt.Println("1. Multiplication")
		fmt.Println("2. Line Multiplication")
		fmt.Println("3. Block Multiplication")
		fmt.Println("0. Exit Program")
		fmt.Print("Selection?: ")
		fmt.Scan(&op)

		if op == 0{
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
			chooseBlockFunction(dimensions,bkSize)
		}
	

	}
}