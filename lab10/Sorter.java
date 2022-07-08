/**
 *  A class used to analyze the runtime of sorting algorithms*/


public class Sorter {

    /* Uses insertion sort to sort the elements of VALUES by inserting item k
       into its correct position. Maintains the following invariant:
       values[0] <= values[1] <= ... <= values[k-1] */
    public static void sort(double[] values) {
        for (int k = 1; k < values.length; k += 1) {
            double temp = values[k];
            int j;
            for (j = k - 1; j >= 0 && values[j] > temp; j -= 1) {
                values[j + 1] = values[j];
            }
            values[j + 1] = temp;
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Sorter N");
            System.out.println("  N - the number of elements to be sorted");
            System.exit(1);
        }
        int N = Integer.parseInt(args[0]);
        double[] values = new double[N];
        for (int k = 0; k < N; k += 1) {
            values[k] = Math.random();
        }
        Timer t = new Timer();
        t.start();
        sort(values);
        long elapsedMs = t.stop();
        System.out.println(elapsedMs + " milliseconds elapsed");
        if (N < 20) {
            for (int k = 0; k < N; k += 1) {
                System.out.println(values[k]);
            }
        }
    }
}