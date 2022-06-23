public class ArrayOperationsTest {

    public static boolean testInsert() {
        int[] values = {1, 2, 3, 4, 5};
        ArrayOperations.insert(values, 0, 0);
        int[] afterInsert1 = {0, 1, 2, 3, 4};
        if (!check(afterInsert1, values)) {
            return false;
        }

        ArrayOperations.insert(values, 4, 7);
        int[] afterInsert2 = {0, 1, 2, 3, 7};
        if (!check(afterInsert2, values)) {
            return false;
        }

        return true;
    }

    public static boolean testDelete() {
        int[] values = {1, 2, 3, 4, 5};
        ArrayOperations.delete(values, 4);
        int[] afterDelete1 = {1, 2, 3, 4, 0};
        if (!check(afterDelete1, values)) {
            return false;
        }

        values[4] = 5;
        ArrayOperations.delete(values, 2);
        int[] afterDelete2 = {1, 2, 4, 5, 0};
        if (!check(afterDelete2, values)) {
            return false;
        }

        values[4] = 7;
        ArrayOperations.delete(values, 0);
        int[] afterDelete3 = {2, 4, 5, 7, 0};
        if (!check(afterDelete3, values)) {
            return false;
        }

        return true;
    }

    public static boolean testCatenate() {
        int[] A = {1, 2, 3};
        int[] B = {4, 5};
        int[] firstValues = ArrayOperations.catenate(A, B);
        int[] afterCat1 = {1, 2, 3, 4, 5};
        if (!check(afterCat1, firstValues)) {
            return false;
        }

        int[] C = {4, 7, 9, 0};
        int[] D = {2, 4, 8, 9, 0, 9, 3};
        int[] secondValues = ArrayOperations.catenate(C, D);
        int[] afterCat2 = {4, 7, 9, 0, 2, 4, 8, 9, 0, 9, 3};
        if (!check(afterCat2, secondValues)) {
            return false;
        }

        return true;
    }

    public static boolean check(int[][] expected, int[][] actual) {
        if (expected.length != actual.length) {
            System.out.format("Array length did not match expected length:\n"
                    + "      Expected: %d\n"
                    + "      Got: %d\n",
                    expected.length, actual.length);
            return false;
        }
        for (int k = 0; k < expected.length; k++) {
            if (!check(expected[k], actual[k])) {
                return false;
            }
        }
        return true;
    }

    public static boolean check(int[] expected, int[] actual) {
        if(actual == null) {
            System.out.format("You \"actual\" array is null. Have you implemented the method you are testing?\n\n");
            return false;
        }
        if (expected.length != actual.length) {
            System.out.format("Array length did not match expected length:\n"
                    + "      Expected: %d\n"
                    + "      Got: %d\n",
                    expected.length, actual.length);
            return false;
        }
        for (int k = 0; k < expected.length; k++) {
            if (expected[k] != actual[k]) {
                System.out.format("Array contents different at index %d:\n"
                        + "    Expected: %d\n"
                        + "    Got: %d\n",
                        k, expected[k], actual[k]);
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        if (testInsert()) {
            System.out.println("testInsert passed!");
        } else {
            System.out.println("testInsert failed!");
        }
        if (testDelete()) {
            System.out.println("testDelete passed!");
        } else {
            System.out.println("testDelete failed!");
        }
        if (testCatenate()) {
            System.out.println("testCatenate passed!");
        } else {
            System.out.println("testCatenate failed!");
        }
    }
}