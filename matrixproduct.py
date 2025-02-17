from time import perf_counter
'''
from pypapi import papi_low as papi
from pypapi import events
'''




def main(): 
    '''
    papi.library_init()
    evs = papi.create_eventset()
    papi.add_event(evs,events.PAPI_L1_DCM) 
    papi.add_event(evs,events.PAPI_L2_DCM)
    '''
    
    while True:
        print("1. Matrix Product")
        print("2. Line Multiplication")
        print("3. Block Multiplicaton")
        print("4. Close Program")
        option = int(input("Select an option: "))

        if(option == 4):
            break

        # Number of lines and columns
        n = int(input("Choose Matrix Dimension:\nLines x columns: "))
        
        '''
        papi.start(evs)
        '''

        match option:
            case 1:
                matrix_product(n)
            case 2:
                line_multiplication(n)
            case 3:
                block_size = int(input("Choose Block Size: "))
                block_multiplication(n,block_size)
                continue
            case 4:
                break
            case _:
                print("Invalid option, try again")
                continue
        '''
        result = papi.stop(evs)
        print(result)   
        '''
    '''
    papi.cleanup_eventset(evs)
    papi.destroy_eventset(evs)   
    '''


def matrix_product(n):
    matrix1 = []
    matrix2 = []
    result = []

    for _ in range(n):
        for _ in range(n):
            matrix1.append(1.0) 

    for i in range(n): 
        for _ in range(n):
            matrix2.append(i+1)  

    for _ in range(n): 
        for _ in range(n): 
            result.append(0)

    # Put clock start here 
    time_start = perf_counter() 

    for i in range(n): 
        for j in range(n): 
            temp = 0
            for k in range(n): 
                temp += matrix1[i*n + k] * matrix2[k*n + j]
            result[i*n + j] = temp 

    # Put clock end here 
    time_finish = perf_counter()

    print(f"Time: {time_finish - time_start} seconds")

    print("Result Matrix:")
    for j in range(min(10, n)):
        print(f"{result[j]} ", end='')
    print("\n")
    

def line_multiplication(n):
    matrix1 = []
    matrix2 = []
    result = []

    for _ in range(n):
        for _ in range(n):
            matrix1.append(1.0) 

    for i in range(n): 
        for _ in range(n):
            matrix2.append(i+1)  

    for _ in range(n): 
        for _ in range(n): 
            result.append(0)

    # Put clock start here 
    time_start = perf_counter() 

    for i in range(n): 
        for j in range(n):
            temp =  matrix1[j + i*n]
            for k in range(n):
                result[k + i*n]+= temp*matrix2[k + j*n] # matrix1[j + i*n] caso substituir pelo temp mais rápido, devido ao valor cached
                

    # Put clock end here 
    time_finish = perf_counter()

    print(f"Time: {time_finish - time_start} seconds")

    print("Result Matrix:")
    for j in range(min(10, n)):
        print(f"{result[j]} ", end='')
    print("\n")

# Version with line block multiplication
def block_line_multiplication(n,block_size):
    matrix1 = []
    matrix2 = []
    result = []

    for _ in range(n):
        for _ in range(n):
            matrix1.append(1.0) 

    for i in range(n): 
        for _ in range(n):
            matrix2.append(i+1)  

    for _ in range(n): 
        for _ in range(n): 
            result.append(0)

    # Put clock start here 
    time_start = perf_counter()

    for bi in range(0, n, block_size):  
        for bj in range(0, n, block_size):  
            for bk in range(0, n, block_size):  
                for i in range(bi, min(bi + block_size, n)): 
                    for j in range(bj, min(bj + block_size, n)):
                        temp =  matrix1[j + i*n]
                        for k in range(bk, min(bk + block_size, n)):
                            result[k + i*n]+= temp*matrix2[k + j*n] # matrix1[j + i*n] caso substituir pelo temp mais rápido, devido ao valor cached
    # Put clock end here 
    time_finish = perf_counter()

    print(f"Time: {time_finish - time_start} seconds")

    print("Result Matrix:")
    for j in range(min(10, n)):
        print(f"{result[j]} ", end='')
    print("\n")


def block_multiplication(n,block_size):
    matrix1 = []
    matrix2 = []
    result = []

    for _ in range(n):
        for _ in range(n):
            matrix1.append(1.0) 

    for i in range(n): 
        for _ in range(n):
            matrix2.append(i+1)  

    for _ in range(n): 
        for _ in range(n): 
            result.append(0)

    # Put clock start here 
    time_start = perf_counter() 

    for bi in range(0, n, block_size):  
        for bj in range(0, n, block_size): 
            for bk in range(0, n, block_size):  
                
                minValueI = min(bi + block_size, n)
                minValueJ = min(bj + block_size, n)
                minValueK = min(bk + block_size, n)
                for i in range(bi, minValueI):
                    for j in range(bj, minValueJ):
                        sum_value = 0  
                        for k in range(bk, minValueK):
                            sum_value += matrix1[i * n + k] * matrix2[k * n + j]
                        result[i * n + j] += sum_value

    # Put clock end here 
    time_finish = perf_counter()

    print(f"Time: {time_finish - time_start} seconds")

    print("Result Matrix:")
    for j in range(min(10, n)):
        print(f"{result[j]} ", end='')
    print("\n")


if __name__ == "__main__":
    main()
