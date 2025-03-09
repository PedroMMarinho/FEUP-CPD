import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class MatrixProduct {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        if (args.length > 0) {
            if (args[0].equals("test"))
            {
                handleTestCases();
                return;
            }
        } else {
            System.out.println("No arguments provided.");
        }

        while (true)
        {
            System.out.println("1. Multiplication");
            System.out.println("2. Line Multiplication");
            System.out.println("3. Block Multiplication");
            System.out.println("0. Exit Program");
            System.out.println("Selection?: ");

            int op = scanner.nextInt();

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

        int op = scanner.nextInt();

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

    private static double OnMult(Integer dimensions)
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



        return time;
    }


    private static double OnMultLine(Integer dimensions)
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

        /*

        System.out.println("Time: " + time + " seconds");

        System.out.println("Result Matrix: ");
        for (int i = 0; i < 1; i++)
        {
            for (int j = 0; j < Math.min(10, dimensions); j++)
                System.out.println(matrixC[j]);
        }
         */

        return time;
    }


    private static double OnMultBlock(Integer dimensions, Integer bkSize)
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

        return time;
    }


    private static double OnMultBlockLine(Integer dimensions, Integer bkSize)
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

        return time;
    }

    private static void handleTestCases(){

        try{
            FileWriter file = new FileWriter("docs/data_java.csv", true);

            /*
            System.out.print("== Normal multiplication tests ==");

            for (int n = 600; n<=3000; n+=400)
            {
                double totalTime = 0;
                for(int i=0; i<=30; i++){
                    totalTime += OnMult(n);
                }
                file.write("Normal Mult " + n + " " + totalTime/30 + "\n");
            }

            System.out.println("Complete");
            */

            System.out.print("== Inline multiplication tests ==");

            for (int n = 600; n<=3000; n+=400)
            {
                double totalTime = 0;
                for(int i=0; i<=30; i++){
                    totalTime += OnMultLine(n);
                }
                file.write("Inline Mult " + n + " " + totalTime/30 + "\n");

            }

            System.out.println("Complete");
            /*
            System.out.print("== Block multiplication tests ==");

            for (int n = 4096; n<=10240; n+=2048)
            {
                for (int bksize = 128; bksize<=512; bksize+= bksize)
                {
                    double totalTime = 0;
                    for(int i=0; i<=30; i++) {
                        totalTime += OnMultBlock(n, bksize);

                        file.write("Block Mult " + n + " " + bksize + " " + time);
                    }
                    file.write("Block Mult " + n + " " + bkSize + " " + totalTime/30 + "\n");
                }
            }

            System.out.println("Complete");

            System.out.print("== Block Line multiplication tests ==");

            for (int n = 4096; n<=10240; n+=2048)
            {
                for (int bksize = 128; bksize<=512; bksize+= bksize)
                {
                    for(int i=0; i<=30; i++) {
                        double time = OnMultBlockLine(n, bksize);

                        file.write("Inline Block Mult " + n + " " + bksize + " " + time);
                    }
                }
            }

            System.out.println("Complete");
            */
            file.close();
        }
        catch (IOException e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


}
