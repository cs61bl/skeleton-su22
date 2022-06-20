/**
 * Class that determines whether or not a year is a leap year.
 * @author YOUR NAME HERE
 */
public class LeapYear {

    /**
     * Update this comment to describe what this method does.
     * @source CS 61BL Lab 1
     */
    public static boolean isLeapYear(int year) {
        // TODO: Fill in this method.
    }

    /** Calls isLeapYear to print correct statement. */
    private static void checkLeapYear(int year) {
        if (isLeapYear(year)) {
            System.out.printf("%d is a leap year.\n", year);
        } else {
            System.out.printf("%d is not a leap year.\n", year);
        }
    }

    /** Must be provided an integer as a command line argument ARGS. */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter command line arguments.");
            System.out.println("e.g. java LeapYear 2000");
        }
        for (int i = 0; i < args.length; i++) {
            try {
                int year = Integer.parseInt(args[i]);
                checkLeapYear(year);
            } catch (NumberFormatException e) {
                System.out.printf("%s is not a valid number.\n", args[i]);
            }
        }
    }
}
