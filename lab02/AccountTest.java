

/** A suite of print tests for Account.
 @author Zephyr Barkan
 */

public class AccountTest {

    public static void testWithdraw() {
        System.out.println("Making an account to test withdraw");
        Account account = new Account(1000);
        System.out.println(account.withdraw(500));
        System.out.println(500 == account.getBalance());
        System.out.println(!account.withdraw(9999));
        System.out.println(500 == account.getBalance());
        System.out.println("All print statements should be true");
        System.out.println("Note: The above output may contain 'Insufficient Funds'. This is okay.");
    }

    public static void testMerge() {
        System.out.println("Making two accounts to test merge");
        Account one = new Account(100);
        Account two = new Account(100);
        one.merge(two);
        System.out.println(0 == two.getBalance());
        System.out.println(200 == one.getBalance());
        System.out.println("Both print statements should be true");
    }

    public static void testParent() {
        System.out.println("Making two accounts to test parent accounts");
        Account parent = new Account(1000);
        Account child = new Account(100, parent);
        System.out.println(child.withdraw(50));
        System.out.println(50 == child.getBalance());
        System.out.println(1000 == parent.getBalance());
        System.out.println(child.withdraw(55));
        System.out.println(0 == child.getBalance());
        System.out.println(995 == parent.getBalance());
        System.out.println("All print statements should be true");
    }


    public static void main(String[] args) {
        testWithdraw();
        testMerge();
        testParent();
    }
}
