#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <omp.h>
#include <papi.h>

using namespace std;

#define SYSTEMTIME clock_t

// 1.1 - Multiplication of two matrices
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

	Time1 = clock();
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
	Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
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

// 1.2 - Multiplication of two matrices by line
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

	Time1 = clock();
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
	Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
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

// 1.3.1 - Multiplication of two matrices by block with inline multiplication
void OnMultBlockInline(int m_ar, int m_br, int bkSize)
{

	SYSTEMTIME Time1, Time2;

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

	Time1 = clock();
	// block algorithm using inline multiplication

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

	Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for (i = 0; i < 1; i++)
	{
		for (j = 0; j < min(10, m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

	free(pha);
	free(phb);
	free(phc);
}
// 1.3.2 - Multiplication of two matrices by block
void OnMultBlock(int m_ar, int m_br, int bkSize)
{
	
	SYSTEMTIME Time1, Time2;

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

	Time1 = clock();

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

	Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for (i = 0; i < 1; i++)
	{
		for (j = 0; j < min(10, m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

	free(pha);
	free(phb);
	free(phc);
}

// 2.1 Parallel version using normal matrix product

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

	Time1 = clock();
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
	Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for (i = 0; i < 1; i++)
	{
		for (j = 0; j < min(10, m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

	double gflops = (2.0 * m_ar * m_ar * m_br) / ((end - start) * 1e6);
    printf("Performance: %.2f MFLOPS\n", gflops);
	timeTaken = end - start;
	free(pha);
	free(phb);
	free(phc);
}



// 2.2 Parallel version using inline matrix product

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

	Time1 = clock();
	start = omp_get_wtime();
	
		
	#pragma omp parallel
    {
        
        #pragma omp single
        num_threads = omp_get_num_threads();

        #pragma omp for private(j, k)
        for (int i = 0; i < m_ar; i++) {
            for (int j = 0; j < m_br; j++) {
                temp = pha[i * m_ar + j];  
                for (int k = 0; k < m_br; k++) {
                    phc[i * m_ar + k] += temp * phb[j * m_ar + k];
                }
            }
        }
    }
	

	end = omp_get_wtime();
	Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for (i = 0; i < 1; i++)
	{
		for (j = 0; j < min(10, m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

	double gflops = (2.0 * m_ar * m_ar * m_br) / ((end - start) * 1e6);
    printf("Performance: %.2f MFLOPS\n", gflops);
	printf("Number of Threads: %d\n", num_threads);
	timeTaken = end - start;

	free(pha);
	free(phb);
	free(phc);
}


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


void chooseBlockFunction(int lin, int col, int blockSize){
	int op;
	cout << "1. Normal Block Matrix Multiplication" << endl;
	cout << "2. Block Matrix Multiplication with Inline Multiplication" << endl;
	cout << "Selection?: ";
	cin >> op;
	
	switch (op)
	{
	case 1:
		OnMultBlock(lin,col,blockSize);
		break;
	case 2:
		OnMultBlockInline(lin,col,blockSize);
		break;
	default:
		cout << "Invalid Input" << endl;
		break;
	}
	
}

void papiResetAndPrintInfo(int EventSet){
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

void handleSerialVsParallel(int lin,int col,int EventSet){
	int op;
	cout << "1. Normal Matrix Product" << endl;
	cout << "2. Inline Matrix Product" << endl;
	cout << "Selection?: ";
	cin >> op;
	int ret;
	double serialTime, paralelTime;
	switch (op)
	{
	case 1:
		printf("Loading serial function ...\n");
		OnMult(lin,col,serialTime);
		cout << endl;
		
		papiResetAndPrintInfo(EventSet);
		ret = PAPI_start(EventSet);
		
		printf("Loading parallel function ...\n");
		OnMultParallelized(lin,col,paralelTime);
		break;
	case 2:
		printf("Loading serial function ...\n");
		OnMultLine(lin,col,serialTime);
		cout << endl;

		papiResetAndPrintInfo(EventSet);
		ret = PAPI_start(EventSet);

		printf("Loading parallel function ...\n");
		OnMultLineParallelized(lin,col,paralelTime);
		break;
	default:
		cout << "Invalid Input" << endl;
		break;
	}
	
	printf("Serial Time taken: %f sec\n",serialTime);
	printf("Parallel Time taken: %f sec\n",paralelTime);
}


void handleParallelizationOption(int lin, int col){
	int op;
	cout << "1. Normal Matrix Product" << endl;
	cout << "2. Inline Matrix Product" << endl;
	cout << "Selection?: ";
	cin >> op;
	double _;
	switch (op)
	{
	case 1:
		OnMultParallelized(lin,col,_);
		break;
	case 2:
		OnMultLineParallelized(lin,col,_);
		break;
	default:
		cout << "Invalid Input" << endl;
		break;
	}
}

int main(int argc, char *argv[])
{

	double _;
	char c;
	int lin, col, blockSize;
	int op;
	
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
			OnMult(lin, col,_);
			break;
		case 2:
			OnMultLine(lin, col,_);
			break;
		case 3:
			cout << "Block Size? ";
			cin >> blockSize;
			chooseBlockFunction(lin, col, blockSize);
			break;
		case 4:
			handleParallelizationOption(lin,col);
			break;

		case 5:
			handleSerialVsParallel(lin,col,EventSet);
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