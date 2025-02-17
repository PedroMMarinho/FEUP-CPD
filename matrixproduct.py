
def main():
    
    while True:
        print("1. Matrix Product")
        print("2. Line Multiplication")
        print("3. Block Multiplicaton")
        print("4. Close Program")
        option = int(input("Select an option: "))
        
        match option:
            case 1:
                matrix_product()
            case 2:
                #line_multiplication()
                continue
            case 3:
                #block_multiplication()
                continue
            case 4:
                break
            case _:
                print("Invalid option, try again")
                continue
        
def matrix_product():
    print("Hello world")


if __name__ == "__main__":
    main()
