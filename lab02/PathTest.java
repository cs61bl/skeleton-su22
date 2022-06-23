/** A printing based test for the iteration of a path.
 @author Zephyr Barkan
 */

public class PathTest {

    public static void testIterate() {
        Path path = new Path(0, 0);
        path.iterate(1, 1);
        System.out.println(Math.abs(path.getCurrX()) < .001);
        System.out.println(Math.abs(path.getCurrY()) < .001);
        System.out.println(Math.abs(1 - path.getNextX()) < .001);
        System.out.println(Math.abs(1 - path.getNextY()) < .001);

        path.iterate(1, 1);
        System.out.println(Math.abs(1 - path.getCurrX()) < .001);
        System.out.println(Math.abs(1 - path.getCurrY()) < .001);
        System.out.println(Math.abs(2 - path.getNextX()) < .001);
        System.out.println(Math.abs(2 - path.getNextY()) < .001);

        System.out.println("All print statements should be true");
    }

    public static void main(String[] args) {
        testIterate();
    }

}
