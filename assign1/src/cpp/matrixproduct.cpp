#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <omp.h>
#include <fstream>
#include <papi.h>

using namespace std;

#define SYSTEMTIME clock_t

// 1.1 - Conventional Multiplication of two matrices
void OnMult(int m_ar, int m_br, double &timeTaken)
{

	SYSTEMTIME Time1, Time2;

	char st[100];
	double temp;
	int i, j, k;

	double start, end;
	double *pha, *phb, *phc;

	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for (i = 0; i < m_ar; i++)
		for (j = 0; j < m_ar; j++)
			pha[i * m_ar + j] = (double)1.0;

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phb[i * m_br + j] = (double)(i + 1);

	start = omp_get_wtime();
	for (i = 0; i < m_ar; i++)
	{
		for (j = 0; j < m_br; j++)
		{
			temp = 0;
			for (k = 0; k < m_ar; k++)
			{
				temp += pha[i * m_ar + k] * phb[k * m_br + j];
			}
			phc[i * m_ar + j] = temp;
		}
	}
	end = omp_get_wtime();

	sprintf(st, "Time: %3.3f seconds\n", (double)(end - start));
	cout << st;
	// display 10 elements of the result matrix to verify correctness
	cout << "Result matrix: " << endl;
	for (i = 0; i < 1; i++)
	{
		for (j = 0; j < min(10, m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;
	

	timeTaken = end - start;
	free(pha);
	free(phb);
	free(phc);
}


// 1.2 - Conventional Multiplication of two matrices by line
void OnMultLine(int m_ar, int m_br, double &timeTaken)
{

	SYSTEMTIME Time1, Time2;
	double start, end;
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;

	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for (i = 0; i < m_ar; i++)
		for (j = 0; j < m_ar; j++)
			pha[i * m_ar + j] = (double)1.0;

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phb[i * m_br + j] = (double)(i + 1);

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phc[i * m_br + j] = (double)(0);

	start = omp_get_wtime();
	for (i = 0; i < m_ar; i++)
	{
		for (j = 0; j < m_br; j++)
		{
			temp = pha[i * m_ar + j];
			for (k = 0; k < m_br; k++)
			{
				phc[i * m_ar + k] += temp * phb[j * m_ar + k];
			}
		}
	}
	end = omp_get_wtime();

	sprintf(st, "Time: %3.3f seconds\n", (double)(end - start));
	cout << st;
	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for (i = 0; i < 1; i++)
	{
		for (j = 0; j < min(10, m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;
	timeTaken = end - start;
	
	timeTaken = end - start;
	free(pha);
	free(phb);
	free(phc);
}


// 2.1 Parallel version outer loop using normal matrix product
void OnMultParallelized(int m_ar, int m_br, double &timeTaken)
{

	SYSTEMTIME Time1, Time2;
	double start, end;

	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;

	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for (i = 0; i < m_ar; i++)
		for (j = 0; j < m_ar; j++)
			pha[i * m_ar + j] = (double)1.0;

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phb[i * m_br + j] = (double)(i + 1);

	start = omp_get_wtime();

#pragma omp parallel for
	for (i = 0; i < m_ar; i++)
	{
		for (j = 0; j < m_br; j++)
		{
			temp = 0;
			for (k = 0; k < m_ar; k++)
			{
				temp += pha[i * m_ar + k] * phb[k * m_br + j];
			}
			phc[i * m_ar + j] = temp;
		}
	}

	end = omp_get_wtime();
	sprintf(st, "Time: %3.3f seconds\n", (double)(end - start));
	cout << st;


	cout << "Result matrix: " << endl;
	for (i = 0; i < 1; i++)
	{
		for (j = 0; j < min(10, m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;
	

	timeTaken = end - start;
	free(pha);
	free(phb);
	free(phc);
}

// 2.2 Parallel version inner loop using normal matrix product
void OnMultParallelizedInnerMostLoop(int m_ar, int m_br, double &timeTaken)
{

	SYSTEMTIME Time1, Time2;
	double start, end;

	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;

	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for (i = 0; i < m_ar; i++)
		for (j = 0; j < m_ar; j++)
			pha[i * m_ar + j] = (double)1.0;

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phb[i * m_br + j] = (double)(i + 1);

	start = omp_get_wtime();

	#pragma omp parallel private(i, j, temp)
	for (i = 0; i < m_ar; i++)
	{
		for (j = 0; j < m_br; j++)
		{
			temp = 0;
			#pragma omp parallel for reduction(+:temp)
			for (k = 0; k < m_ar; k++)
			{
				temp += pha[i * m_ar + k] * phb[k * m_br + j];
			}
			phc[i * m_ar + j] = temp;
		}
	}
	

	end = omp_get_wtime();
	sprintf(st, "Time: %3.3f seconds\n", (double)(end - start));
	cout << st;

	
	cout << "Result matrix: " << endl;
	for (i = 0; i < 1; i++)
	{
		for (j = 0; j < min(10, m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;
	

	timeTaken = end - start;
	free(pha);
	free(phb);
	free(phc);
}


// 2.3 Parallel version outer loop using inline matrix product
void OnMultLineParallelized(int m_ar, int m_br, double &timeTaken)
{

	SYSTEMTIME Time1, Time2;
	double start, end;
	char st[100];
	double temp;
	int i, j, k;

	int num_threads = 1; // set default as 1
	double *pha, *phb, *phc;

	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for (i = 0; i < m_ar; i++)
		for (j = 0; j < m_ar; j++)
			pha[i * m_ar + j] = (double)1.0;

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phb[i * m_br + j] = (double)(i + 1);

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phc[i * m_br + j] = (double)(0);

	start = omp_get_wtime();

	#pragma omp parallel 
	{
		#pragma omp for private(j, k)
			for (int i = 0; i < m_ar; i++)
			{
				for (int j = 0; j < m_br; j++)
				{
					temp = pha[i * m_ar + j];
					for (int k = 0; k < m_br; k++)
					{
						phc[i * m_ar + k] += temp * phb[j * m_ar + k];
					}
				}
			}
	}

	

	end = omp_get_wtime();
	sprintf(st, "Time: %3.3f seconds\n", (double)(end - start));
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for (i = 0; i < 1; i++)
	{
		for (j = 0; j < min(10, m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;
	


	timeTaken = end - start;

	free(pha);
	free(phb);
	free(phc);
}

// 2.4 Parallel version inner loop using inline matrix product
void OnMultLineParallelizedInnerMost(int m_ar, int m_br, double &timeTaken)
{

	SYSTEMTIME Time1, Time2;
	double start, end;
	char st[100];
	double temp;
	int i, j, k;

	int num_threads = 1; // set default as 1
	double *pha, *phb, *phc;

	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for (i = 0; i < m_ar; i++)
		for (j = 0; j < m_ar; j++)
			pha[i * m_ar + j] = (double)1.0;

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phb[i * m_br + j] = (double)(i + 1);

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phc[i * m_br + j] = (double)(0);

	start = omp_get_wtime();


	#pragma omp parallel
	{
		for (int i = 0; i < m_ar; i++)
		{
			for (int j = 0; j < m_br; j++)
			{
				temp = pha[i * m_ar + j];
				
				#pragma omp for

				for (int k = 0; k < m_br; k++)
				{
					phc[i * m_ar + k] += temp * phb[j * m_ar + k];
				}
			}
		}
	}

	end = omp_get_wtime();
	sprintf(st, "Time: %3.3f seconds\n", (double)(end - start));
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for (i = 0; i < 1; i++)
	{
		for (j = 0; j < min(10, m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;
	

	timeTaken = end - start;

	free(pha);
	free(phb);
	free(phc);
}

// 3.1 - Multiplication of two matrices by block with inline multiplication
void OnMultBlockInline(int m_ar, int m_br, int bkSize, double &timeTaken)
{

	SYSTEMTIME Time1, Time2;

	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;

	double start, end;
	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for (i = 0; i < m_ar; i++)
		for (j = 0; j < m_ar; j++)
			pha[i * m_ar + j] = (double)1.0;

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phb[i * m_br + j] = (double)(i + 1);

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phc[i * m_br + j] = (double)(0);

	// block algorithm using inline multiplication
	start = omp_get_wtime();

	for (int bi = 0; bi < m_br; bi = bi + bkSize)
	{
		for (int bj = 0; bj < m_br; bj = bj + bkSize)
		{
			for (int bk = 0; bk < m_br; bk = bk + bkSize)
			{
				int minValueI = min(bi + bkSize, m_br);
				int minValueJ = min(bj + bkSize, m_br);
				int minValueK = min(bk + bkSize, m_br);
				for (i = bi; i < minValueI; i++)
				{
					for (j = bj; j < minValueJ; j++)
					{
						int sum_value = pha[j + i * m_br];
						for (k = bk; k < minValueK; k++)
						{
							phc[k + i * m_br] += sum_value * phb[k + j * m_br];
						}
					}
				}
			}
		}
	}
	end = omp_get_wtime();
	

	sprintf(st, "Time: %3.3f seconds\n", (double)(end - start));
	cout << st;
	cout << "Result matrix: " << endl;
	for (i = 0; i < 1; i++)
	{
		for (j = 0; j < min(10, m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;
	
	timeTaken = end - start;
	free(pha);
	free(phb);
	free(phc);
}
// 3.2 - Conventional Multiplication of two matrices by block
void OnMultBlock(int m_ar, int m_br, int bkSize, double &timeTaken)
{

	SYSTEMTIME Time1, Time2;

	char st[100];
	double temp;
	int i, j, k;
	double start, end;

	double *pha, *phb, *phc;

	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for (i = 0; i < m_ar; i++)
		for (j = 0; j < m_ar; j++)
			pha[i * m_ar + j] = (double)1.0;

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phb[i * m_br + j] = (double)(i + 1);

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phc[i * m_br + j] = (double)(0);

	start = omp_get_wtime();

	for (int bi = 0; bi < m_br; bi = bi + bkSize)
	{
		for (int bj = 0; bj < m_br; bj = bj + bkSize)
		{
			for (int bk = 0; bk < m_br; bk = bk + bkSize)
			{
				int minValueI = min(bi + bkSize, m_br);
				int minValueJ = min(bj + bkSize, m_br);
				int minValueK = min(bk + bkSize, m_br);

				for (i = bi; i < minValueI; i++)
				{
					for (j = bj; j < minValueJ; j++)
					{
						temp = 0;
						for (k = bk; k < minValueK; k++)
						{
							temp += pha[i * m_ar + k] * phb[k * m_br + j];
						}
						phc[i * m_ar + j] += temp;
					}
				}
			}
		}
	}
	end = omp_get_wtime();

	sprintf(st, "Time: %3.3f seconds\n", (double)(end - start));
	cout << st;

	// display 10 elements of the result matrix to verify correctness
	cout << "Result matrix: " << endl;
	for (i = 0; i < 1; i++)
	{
		for (j = 0; j < min(10, m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;
	
	timeTaken = end - start;
	free(pha);
	free(phb);
	free(phc);
}

// PAPI functions
void handle_error(int retval)
{
	printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
	exit(1);
}

void init_papi()
{
	int retval = PAPI_library_init(PAPI_VER_CURRENT);
	if (retval != PAPI_VER_CURRENT && retval < 0)
	{
		printf("PAPI library version mismatch!\n");
		exit(1);
	}
	if (retval < 0)
		handle_error(retval);

	std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
			  << " MINOR: " << PAPI_VERSION_MINOR(retval)
			  << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
}

// Auxilar function to reset and print PAPI info
void papiResetAndPrintInfo(int EventSet)
{
	long long values[2];
	int ret;
	ret = PAPI_stop(EventSet, values);
	if (ret != PAPI_OK)
		cout << "ERROR: Stop PAPI" << endl;
	printf("L1 DCM: %lld \n", values[0]);
	printf("L2 DCM: %lld \n", values[1]);

	ret = PAPI_reset(EventSet);
	if (ret != PAPI_OK)
		std::cout << "FAIL reset" << endl;
	cout << endl;
}

// Auxiliary function to write data to a CSV file
void writeToCSVFile(const std::string &functionType,
	long long L2_DCM, long long L1_DCM, int matrixSize,
	int blockSize, int numThreads, double realTime)
{
		std::ofstream file;

		// Open file in append mode
		file.open("docs/data_cpp.csv", std::ios::app);

		if (!file.is_open())
		{
		std::cerr << "Error opening file: data_cpp.csv" << std::endl;
		return;
		}

		// Write data as a new line in CSV format
		file << functionType << ","
		<< L2_DCM << ","
		<< L1_DCM << ","
		<< matrixSize << ","
		<< blockSize << ","
		<< numThreads << ","
		<< realTime << "\n";

		file.close();
}


// Menu functions

// Handles block functions
void chooseBlockFunction(int lin, int col, int blockSize)
{
	int op;
	cout << "1. Normal Block Matrix Multiplication" << endl;
	cout << "2. Block Matrix Multiplication with Inline Multiplication" << endl;
	cout << "Selection?: ";
	cin >> op;
	double timeTaken;

	switch (op)
	{
	case 1:
		OnMultBlock(lin, col, blockSize, timeTaken);
		break;
	case 2:
		OnMultBlockInline(lin, col, blockSize, timeTaken);
		break;
	default:
		cout << "Invalid Input" << endl;
		break;
	}
}

// Handles serial functions vs parallel functions
void handleSerialVsParallel(int lin, int col, int EventSet)
{
	int op;
	cout << "1. Outer loop Normal Multiplication" << endl;
	cout << "2. Outer loop Inline Multiplication" << endl;
	cout << "3. Inner loop Normal Multiplication" << endl;
	cout << "4. Inner loop Inline Multiplication" << endl;
	cout << "Selection?: ";
	cin >> op;
	int ret;
	double serialTime, paralelTime;
	switch (op)
	{
	case 1:
		printf("Loading serial function ...\n");
		OnMult(lin, col, serialTime);
		cout << endl;

		papiResetAndPrintInfo(EventSet);
		ret = PAPI_start(EventSet);

		printf("Loading parallel function ...\n");
		OnMultParallelized(lin, col, paralelTime);
		break;
	case 2:
		printf("Loading serial function ...\n");
		OnMultLine(lin, col, serialTime);
		cout << endl;

		papiResetAndPrintInfo(EventSet);
		ret = PAPI_start(EventSet);

		printf("Loading parallel function ...\n");
		OnMultLineParallelized(lin, col, paralelTime);
		break;
	case 3:
		printf("Loading serial function ...\n");
		OnMultParallelized(lin, col, serialTime);
		cout << endl;

		papiResetAndPrintInfo(EventSet);
		ret = PAPI_start(EventSet);

		printf("Loading parallel function ...\n");
		OnMultParallelizedInnerMostLoop(lin, col, paralelTime);
		break;
	case 4:
		printf("Loading serial function ...\n");
		OnMultLineParallelized(lin, col, serialTime);
		cout << endl;

		papiResetAndPrintInfo(EventSet);
		ret = PAPI_start(EventSet);

		printf("Loading parallel function ...\n");
		OnMultLineParallelizedInnerMost(lin, col, paralelTime);
		break;
	default:
		cout << "Invalid Input" << endl;
		break;
	}

	printf("Serial Time taken: %f sec\n", serialTime);
	printf("Parallel Time taken: %f sec\n", paralelTime);
}

// Handles the parallelization options (only)
void handleParallelizationOption(int lin, int col)
{
	int op;
	cout << "1. Outer Loop Parallelization" << endl;
	cout << "2. Outer Loop Parallelization with Inline Matrix Product" << endl;
	cout << "3. InnerMost Loop Parallelization" << endl;
	cout << "4. Inline Matrix Product with InnerMost Loop Parallelization" << endl;
	cout << "Selection?: ";
	cin >> op;
	double _;
	switch (op)
	{
	case 1:
		OnMultParallelized(lin, col, _);
		break;
	case 2:
		OnMultLineParallelized(lin, col, _);
		break;
	case 3:
		OnMultParallelizedInnerMostLoop(lin, col, _);
		break;
	case 4:
		OnMultLineParallelizedInnerMost(lin, col, _);
		break;
	default:
		cout << "Invalid Input" << endl;
		break;
	}
}


// Test functions

// 600x600 -> 3000x3000 matrix size
void execFunctionWithTimeBullet1_2(void (*f)(int, int, double &), int lin, int col, double timeTaken, int EventSet, string funcType)
{
	int ret;
	long long values[2];

	for (int lin = 600; lin <= 3000; lin += 400)
	{
		for (int i = 0; i < 30; i++)
		{
			// Start counting
			ret = PAPI_start(EventSet);
			if (ret != PAPI_OK)
				cout << "ERROR: Start PAPI" << endl;
			col = lin;
			f(lin, col, timeTaken);

			// Reset Counting
			ret = PAPI_stop(EventSet, values);
			if (ret != PAPI_OK)
				cout << "ERROR: Stop PAPI" << endl;
			// printf("L1 DCM: %lld \n", values[0]);
			// printf("L2 DCM: %lld \n", values[1]);

			// write to CSV
			writeToCSVFile(funcType, values[1], values[0], lin, -1, -1, timeTaken);
			ret = PAPI_reset(EventSet);
			if (ret != PAPI_OK)
				std::cout << "FAIL reset" << endl;
		}
	}
}

// 600x600 -> 3000x3000 matrix size with threads
void execParallelFunctionWithTimeBullet1_2(void (*f)(int, int, double &), int lin, int col, double timeTaken, int EventSet, string funcType)
{
	int numThreads[4] = {4, 8, 12, 24};
	int ret;
	long long values[2];

	for (auto threads : numThreads)
	{
		// set num of thredas
		omp_set_num_threads(threads);

		for (int lin = 2200; lin <= 3000; lin += 400)
		{
			for (int i = 0; i < 30; i++)
			{
				// Start counting
				ret = PAPI_start(EventSet);
				if (ret != PAPI_OK)
					cout << "ERROR: Start PAPI" << endl;
				col = lin;
				f(lin, col, timeTaken);

				// Reset Counting
				ret = PAPI_stop(EventSet, values);
				if (ret != PAPI_OK)
					cout << "ERROR: Stop PAPI" << endl;
				// printf("L1 DCM: %lld \n", values[0]);
				// printf("L2 DCM: %lld \n", values[1]);
				writeToCSVFile(funcType, values[1], values[0], lin, -1, threads, timeTaken);
				ret = PAPI_reset(EventSet);
				if (ret != PAPI_OK)
					std::cout << "FAIL reset" << endl;
			}
		}
	}
}

// 4096x4096 -> 10240x10240 matrix size with interval 2048
void execFunctionWithTimeBullet2(void (*f)(int, int, double &), int lin, int col, double timeTaken, int EventSet, string funcType)
{
	int ret;
	long long values[2];

	for (int lin = 4096; lin <= 10240; lin += 2048)
	{
		for (int i = 0; i < 30; i++)
		{
			// Start counting
			ret = PAPI_start(EventSet);
			if (ret != PAPI_OK)
				cout << "ERROR: Start PAPI" << endl;
			col = lin;
			f(lin, col, timeTaken);

			// Reset Counting
			ret = PAPI_stop(EventSet, values);
			if (ret != PAPI_OK)
				cout << "ERROR: Stop PAPI" << endl;
			// printf("L1 DCM: %lld \n", values[0]);
			// printf("L2 DCM: %lld \n", values[1]);
			writeToCSVFile(funcType, values[1], values[0], lin, -1, -1, timeTaken);
			ret = PAPI_reset(EventSet);
			if (ret != PAPI_OK)
				std::cout << "FAIL reset" << endl;
		}
	}
}

// 4096x4096 -> 10240x10240 matrix size with interval 2048 Parallel
void execParallelFunctionWithTimeBullet2(void (*f)(int, int, double &), int lin, int col, double timeTaken, int EventSet, string funcType)
{
	int numThreads[4] = {4, 8, 12, 24};
	int ret;
	long long values[2];

	for (auto threads : numThreads)
	{
		// set num of thredas
		omp_set_num_threads(threads);

		for (int lin = 4096; lin <= 10240; lin += 2048)
		{
			for (int i = 0; i < 30; i++)
			{

				// Start counting
				ret = PAPI_start(EventSet);
				if (ret != PAPI_OK)
					cout << "ERROR: Start PAPI" << endl;
				col = lin;
				f(lin, col, timeTaken);

				// Reset Counting
				ret = PAPI_stop(EventSet, values);
				if (ret != PAPI_OK)
					cout << "ERROR: Stop PAPI" << endl;
				// printf("L1 DCM: %lld \n", values[0]);
				// printf("L2 DCM: %lld \n", values[1]);

				writeToCSVFile(funcType, values[1], values[0], lin, -1, threads, timeTaken);
				ret = PAPI_reset(EventSet);
				if (ret != PAPI_OK)
					std::cout << "FAIL reset" << endl;
			}
		}
	}
}

// 600x600 -> 3000x3000 matrix size with block sizes 128, 256, 512
void execFunctionWithBlockSize(void (*f)(int, int, int, double &), int lin, int col, int EventSet, string funcType)
{
	int ret;
	long long values[2];
	int blockSizes[3] = {128, 256, 512};

	double timeTaken;

	for (int blockSize : blockSizes)
	{

		for (int lin = 600; lin <= 3000; lin += 400)
		{
			for (int i = 0; i < 30; i++)
			{
				// Start counting
				ret = PAPI_start(EventSet);
				if (ret != PAPI_OK)
					cout << "ERROR: Start PAPI" << endl;
				col = lin;
				f(lin, col, blockSize, timeTaken);

				// Reset Counting
				ret = PAPI_stop(EventSet, values);
				if (ret != PAPI_OK)
					cout << "ERROR: Stop PAPI" << endl;
				// printf("L1 DCM: %lld \n", values[0]);
				// printf("L2 DCM: %lld \n", values[1]);

				writeToCSVFile(funcType, values[1], values[0], lin, blockSize, -1, timeTaken);
				ret = PAPI_reset(EventSet);
				if (ret != PAPI_OK)
					std::cout << "FAIL reset" << endl;
			}
		}
	}
}
// 4096x4096 -> 10240x10240 matrix size with block sizes 128, 256, 512
void execFunctionWithBlockSizeBullet2(void (*f)(int, int, int, double &), int lin, int col, int EventSet, string funcType)
{
	int ret;
	long long values[2];
	int blockSizes[3] = {128, 256, 512};

	double timeTaken;

	for (int blockSize : blockSizes)
	{

		for (int lin = 4096; lin <= 10240; lin += 2048)
		{
			for (int i = 0; i < 30; i++)
			{
				// Start counting
				ret = PAPI_start(EventSet);
				if (ret != PAPI_OK)
					cout << "ERROR: Start PAPI" << endl;
				col = lin;
				f(lin, col, blockSize, timeTaken);

				// Reset Counting
				ret = PAPI_stop(EventSet, values);
				if (ret != PAPI_OK)
					cout << "ERROR: Stop PAPI" << endl;
				// printf("L1 DCM: %lld \n", values[0]);
				// printf("L2 DCM: %lld \n", values[1]);

				writeToCSVFile(funcType, values[1], values[0], lin, blockSize, -1, timeTaken);
				ret = PAPI_reset(EventSet);
				if (ret != PAPI_OK)
					std::cout << "FAIL reset" << endl;
			}
		}
	}
}

// Handle test cases
void handleTestCases(int EventSet)
{
	int lin, col, blockSize, ret;
	double timeTaken;
	
	// Start of test cases 

	// Run each 30 times

	// Serial Mult 600x600 -> 3000x3000
	printf("Starting onMult function ... \n");
	execFunctionWithTimeBullet1_2(&OnMult, lin, col, timeTaken, EventSet, "Normal Mult");
	printf("Finished onMult function \n\n");

	printf("Starting onMultLine function ... \n");
	execFunctionWithTimeBullet1_2(&OnMultLine, lin, col, timeTaken, EventSet, "Inline Mult");
	printf("Finished onMultLine function\n\n");

	// Outer loop Parallel Mult 600x600 -> 3000x3000
	printf("Starting OnMultParallelized outer loop function ... \n");
	execParallelFunctionWithTimeBullet1_2(&OnMultParallelized, lin, col, timeTaken, EventSet, "Parallelized Normal Mult");
	printf("Finished OnMultParallelized outer loop function \n \n");

	printf("Starting OnMultLineParallelized outer loop function ... \n");
	execParallelFunctionWithTimeBullet1_2(&OnMultLineParallelized, lin, col, timeTaken, EventSet, "Parallelized Inline Mult");
	printf("Finished OnMultLineParallelized outer loop function \n \n");

	// Inner loop Parallel Mult 600x600 -> 3000x3000
	printf("Starting OnMultParallelized inner loop function ... \n");
	execParallelFunctionWithTimeBullet1_2(&OnMultParallelizedInnerMostLoop, lin, col, timeTaken, EventSet, "Inner Most Loop Parallelization");
	printf("Finished OnMultParallelized inner loop function \n \n");

	printf("Starting OnMultLineParallelized inner loop function ... \n");
	execParallelFunctionWithTimeBullet1_2(&OnMultLineParallelizedInnerMost, lin, col, timeTaken, EventSet, "Inner Most Loop Parallelization Inline");
	printf("Finished OnMultLineParallelized inner loop function \n \n");

	// Serial Mult 4096x4096 -> 10240x10240
	printf("Starting OnMult ... \n");
	execFunctionWithTimeBullet2(&OnMult, lin, col, timeTaken, EventSet, "Normal Mult");
	printf("Finished OnMult \n\n");

	printf("Starting OnMultLine ... \n");
	execFunctionWithTimeBullet2(&OnMultLine, lin, col, timeTaken, EventSet, "Inline Mult");
	printf("Finished OnMultLine \n\n");
	
	// Outer Parallel Mult 4096x4096 -> 10240x10240
	printf("Starting OnMultParallelized ... \n");
	execParallelFunctionWithTimeBullet2(&OnMultParallelized, lin, col, timeTaken, EventSet, "Parallelized Normal Mult");
	printf("Finished OnMultParallelized \n\n");

	printf("Starting OnMultLineParallelized ... \n");
	execParallelFunctionWithTimeBullet2(&OnMultLineParallelized, lin, col, timeTaken, EventSet, "Parallelized Inline Mult");
	printf("Finished OnMultLineParallelized \n\n");

	// Inner Parallel Mult 4096x4096 -> 10240x10240
	printf("Starting OnMultParallelizedInnerMostLoop ... \n");
	execParallelFunctionWithTimeBullet2(&OnMultParallelizedInnerMostLoop, lin, col, timeTaken, EventSet, "Inner Most Loop Parallelization");
	printf("Finished OnMultParallelizedInnerMostLoop \n\n");

	printf("Starting OnMultLineParallelizedInnerMost ... \n");
	execParallelFunctionWithTimeBullet2(&OnMultLineParallelizedInnerMost, lin, col, timeTaken, EventSet, "Inner Most Loop Parallelization Inline");
	printf("Finished OnMultLineParallelizedInnerMost \n\n");

	// Block Mult 600x600 -> 3000x3000
	printf("Starting OnMultBlockInline ... \n");
	execFunctionWithBlockSize(&OnMultBlockInline, lin, col, EventSet, "Inline Block Mult");
	printf("Finished OnMultBlockInline \n\n");

	printf("Starting OnMultBlock ... \n");
	execFunctionWithBlockSize(&OnMultBlock, lin, col, EventSet, "Block Mult");
	printf("Finished OnMultBlock \n\n");

	// Block Mult 4096x4096 -> 10240x10240
	printf("Starting OnMultBlockInline ... \n");
	execFunctionWithBlockSizeBullet2(&OnMultBlockInline, lin, col, EventSet, "Inline Block Mult");
	printf("Finished OnMultBlockInline \n\n");

	printf("Starting OnMultBlock ... \n");
	execFunctionWithBlockSizeBullet2(&OnMultBlock, lin, col, EventSet, "Block Mult");
	printf("Finished OnMultBlock \n\n");

	// End of test cases

	ret = PAPI_remove_event(EventSet, PAPI_L1_DCM);
	if (ret != PAPI_OK)
		std::cout << "FAIL remove event" << endl;

	ret = PAPI_remove_event(EventSet, PAPI_L2_DCM);
	if (ret != PAPI_OK)
		std::cout << "FAIL remove event" << endl;

	ret = PAPI_destroy_eventset(&EventSet);
	if (ret != PAPI_OK)
		std::cout << "FAIL destroy" << endl;
}



// Main function
int main(int argc, char *argv[])
{

	double _;
	char c;
	int lin, col, blockSize;
	int op;

	string testString = "";
	if (argv[1] != NULL)
		testString = argv[1];

	int EventSet = PAPI_NULL;
	long long values[2];
	int ret;

	ret = PAPI_library_init(PAPI_VER_CURRENT);
	if (ret != PAPI_VER_CURRENT)
		std::cout << "FAIL" << endl;

	ret = PAPI_create_eventset(&EventSet);
	if (ret != PAPI_OK)
		cout << "ERROR: create eventset" << endl;

	ret = PAPI_add_event(EventSet, PAPI_L1_DCM);
	if (ret != PAPI_OK)
		cout << "ERROR: PAPI_L1_DCM" << endl;

	ret = PAPI_add_event(EventSet, PAPI_L2_DCM);
	if (ret != PAPI_OK)
		cout << "ERROR: PAPI_L2_DCM" << endl;

	if (testString == "test")
	{
		handleTestCases(EventSet);
		return 0;
	}

	op = 1;
	do
	{
		cout << endl
			 << "1. Multiplication" << endl;
		cout << "2. Line Multiplication" << endl;
		cout << "3. Block Multiplication" << endl;
		cout << "4. Parallelization" << endl;
		cout << "5. Serial Vs Parallel Statistics" << endl;
		cout << "0. Exit Program" << endl;
		cout << "Selection?: ";
		cin >> op;
		if (op == 0)
			break;
		printf("Dimensions: lins=cols ? ");
		cin >> lin;
		col = lin;

		// Start counting
		ret = PAPI_start(EventSet);
		if (ret != PAPI_OK)
			cout << "ERROR: Start PAPI" << endl;

		switch (op)
		{
		case 1:
			OnMult(lin, col, _);
			break;
		case 2:
			OnMultLine(lin, col, _);
			break;
		case 3:
			cout << "Block Size? ";
			cin >> blockSize;
			chooseBlockFunction(lin, col, blockSize);
			break;
		case 4:
			handleParallelizationOption(lin, col);
			break;

		case 5:
			handleSerialVsParallel(lin, col, EventSet);
			break;
		}

		ret = PAPI_stop(EventSet, values);
		if (ret != PAPI_OK)
			cout << "ERROR: Stop PAPI" << endl;
		printf("L1 DCM: %lld \n", values[0]);
		printf("L2 DCM: %lld \n", values[1]);

		ret = PAPI_reset(EventSet);
		if (ret != PAPI_OK)
			std::cout << "FAIL reset" << endl;

	} while (op != 0);

	ret = PAPI_remove_event(EventSet, PAPI_L1_DCM);
	if (ret != PAPI_OK)
		std::cout << "FAIL remove event" << endl;

	ret = PAPI_remove_event(EventSet, PAPI_L2_DCM);
	if (ret != PAPI_OK)
		std::cout << "FAIL remove event" << endl;

	ret = PAPI_destroy_eventset(&EventSet);
	if (ret != PAPI_OK)
		std::cout << "FAIL destroy" << endl;
}