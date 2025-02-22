import java.util.Scanner;

public class MatrixProduct {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        while (true)
        {
            System.out.println("1. Multiplication");
            System.out.println("2. Line Multiplication");
            System.out.println("3. Block Multiplication");
            System.out.println("0. Exit Program");
            System.out.println("Selection?: ");

            Integer op = scanner.nextInt();

            if (op == 0) break;

            System.out.println("Dimension: lins=cols ? ");

            Integer dimensions = scanner.nextInt();

            switch (op)
            {
                case 1:
                    OnMult(dimensions);
                    break;
                case 2:
                    OnMultLine(dimensions);
                    break;
                case 3:
                    System.out.println("Block Size? ");
                    Integer bkSize = scanner.nextInt();
                    chooseBlockFunction(dimensions, bkSize);
                    break;
            }
        }
    }

    private static void chooseBlockFunction(Integer dimensions, Integer bkSize)
    {
        Scanner scanner = new Scanner(System.in);

        System.out.println("1. Normal Block Matrix Multiplication");
        System.out.println("2. Block Matrix Multiplication with Inline Multiplication");
        System.out.println("Selection?: ");

        Integer op = scanner.nextInt();

        switch (op)
        {
            case 1:
                OnMultBlock(dimensions, bkSize);
                break;
            case 2:
                OnMultBlockLine(dimensions, bkSize);
                break;
            default:
                System.out.println("Invalid Input");
        }

    }

    private static void OnMult(Integer dimensions)
    {
        double[] matrixA = new double[dimensions * dimensions];
        double[] matrixB = new double[dimensions * dimensions];
        double[] matrixC = new double[dimensions * dimensions];

        for (int i = 0; i < dimensions; i++)
        {
            for (int j = 0; j < dimensions; j++)
            {
                matrixA[i * dimensions + j] = 1.0;
                matrixB[i * dimensions + j] = (double) i + 1.0;
            }
        }

        long start = System.nanoTime();

        double temp;

        for (int i = 0; i < dimensions; i++)
        {
            for (int j = 0; j < dimensions; j++)
            {
                temp = 0;
                for (int k = 0; k < dimensions; k++)
                {
                    temp += matrixA[i * dimensions + k] * matrixB[k * dimensions + j];
                }
                matrixC[i * dimensions + j] = temp;
            }
        }

        long end = System.nanoTime();

        double time = (end - start) / 1e9;

        System.out.println("Time: " + time + " seconds");

        System.out.println("Result Matrix: ");
        for (int i = 0; i < 1; i++)
        {
            for (int j = 0; j < Math.min(10, dimensions); j++)
                System.out.println(matrixC[j]);
        }
    }


    private static void OnMultLine(Integer dimensions)
    {

        double[] matrixA = new double[dimensions * dimensions];
        double[] matrixB = new double[dimensions * dimensions];
        double[] matrixC = new double[dimensions * dimensions];

        for (int i = 0; i < dimensions; i++)
        {
            for (int j = 0; j < dimensions; j++)
            {
                matrixA[i * dimensions + j] = 1.0;
                matrixB[i * dimensions + j] = (double) i + 1.0;
                matrixC[i * dimensions + j] = 0.0;
            }
        }

        long start = System.nanoTime();

        double temp;

        for (int i = 0; i < dimensions; i++)
        {
            for (int j = 0; j < dimensions; j++)
            {
                temp = matrixA[i * dimensions + j];
                for (int k = 0; k < dimensions; k++)
                {
                    matrixC[i * dimensions + k] += temp * matrixB[j * dimensions + k];
                }
            }
        }

        long end = System.nanoTime();

        double time = (end - start) / 1e9;

        System.out.println("Time: " + time + " seconds");

        System.out.println("Result Matrix: ");
        for (int i = 0; i < 1; i++)
        {
            for (int j = 0; j < Math.min(10, dimensions); j++)
                System.out.println(matrixC[j]);
        }
    }


    private static void OnMultBlock(Integer dimensions, Integer bkSize)
    {

        double[] matrixA = new double[dimensions * dimensions];
        double[] matrixB = new double[dimensions * dimensions];
        double[] matrixC = new double[dimensions * dimensions];

        for (int i = 0; i < dimensions; i++)
        {
            for (int j = 0; j < dimensions; j++)
            {
                matrixA[i * dimensions + j] = 1.0;
                matrixB[i * dimensions + j] = (double) i + 1.0;
                matrixC[i * dimensions + j] = 0.0;
            }
        }

        long start = System.nanoTime();

        double temp;

        for (int bi = 0; bi < dimensions; bi = bi + bkSize)
        {
            for (int bj = 0; bj < dimensions; bj = bj + bkSize)
            {
                for (int bk = 0; bk < dimensions; bk = bk + bkSize)
                {
                    int minValueI = Math.min(bi + bkSize, dimensions);
                    int minValueJ = Math.min(bj + bkSize, dimensions);
                    int minValueK = Math.min(bk + bkSize, dimensions);

                    for (int i = bi; i < minValueI; i++)
                    {
                        for (int j = bj; j < minValueJ; j++)
                        {
                            temp = 0;
                            for (int k = bk; k < minValueK; k++)
                            {
                                temp += matrixA[i * dimensions + k] * matrixB[k * dimensions + j];
                            }
                            matrixC[i * dimensions + j] += temp;
                        }
                    }
                }
            }
        }

        long end = System.nanoTime();

        double time = (end - start) / 1e9;

        System.out.println("Time: " + time + " seconds");

        System.out.println("Result Matrix: ");
        for (int i = 0; i < 1; i++)
        {
            for (int j = 0; j < Math.min(10, dimensions); j++)
                System.out.println(matrixC[j]);
        }
    }


    private static void OnMultBlockLine(Integer dimensions, Integer bkSize)
    {

        double[] matrixA = new double[dimensions * dimensions];
        double[] matrixB = new double[dimensions * dimensions];
        double[] matrixC = new double[dimensions * dimensions];

        for (int i = 0; i < dimensions; i++)
        {
            for (int j = 0; j < dimensions; j++)
            {
                matrixA[i * dimensions + j] = 1.0;
                matrixB[i * dimensions + j] = (double) i + 1.0;
                matrixC[i * dimensions + j] = 0.0;
            }
        }

        long start = System.nanoTime();

        for (int bi = 0; bi < dimensions; bi = bi + bkSize)
        {
            for (int bj = 0; bj < dimensions; bj = bj + bkSize)
            {
                for (int bk = 0; bk < dimensions; bk = bk + bkSize)
                {
                    int minValueI = Math.min(bi + bkSize, dimensions);
                    int minValueJ = Math.min(bj + bkSize, dimensions);
                    int minValueK = Math.min(bk + bkSize, dimensions);

                    for (int i = bi; i < minValueI; i++)
                    {
                        for (int j = bj; j < minValueJ; j++)
                        {
                            double sum_value = matrixA[j + i * dimensions];

                            for (int k = bk; k < minValueK; k++)
                            {
                                matrixC[k + i * dimensions] += sum_value * matrixB[k + j * dimensions];
                            }
                        }
                    }
                }
            }
        }

        long end = System.nanoTime();

        double time = (end - start) / 1e9;

        System.out.println("Time: " + time + " seconds");

        System.out.println("Result Matrix: ");
        for (int i = 0; i < 1; i++)
        {
            for (int j = 0; j < Math.min(10, dimensions); j++)
                System.out.println(matrixC[j]);
        }
    }
}
