import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;

/** A suite of tests for IntList.
 @author Zephyr Barkan, Maurice Lee, Kevin Lin
 */

public class IntListTest {

    /**
     * Example test that verifies correctness of the IntList.of static method.
     * The main point of this is to convince you that assertEquals knows how to
     * handle IntLists just fine because we implemented IntList.equals.
     */
    @Test
    public void testOf() {
        IntList test = IntList.of(1, 2, 3, 4, 5);
        assertNotNull(test);
        assertEquals(1, test.item);
        assertEquals(2, test.next.item);
        assertEquals(3, test.next.next.item);
        assertEquals(4, test.next.next.next.item);
        assertEquals(5, test.next.next.next.next.item);
        assertNull(test.next.next.next.next.next);

        IntList empty = IntList.of();
        assertNull(empty);

        IntList single = IntList.of(7);
        assertNotNull(single);
        assertEquals(7, single.item);
        assertNull(single.next);
    }

    @Test
    public void testGet() {
        IntList test = IntList.of(1, 2, 3, 4, 5);
        assertEquals(1, test.get(0));
        assertEquals(2, test.get(1));
        assertEquals(3, test.get(2));
        assertEquals(4, test.get(3));
        assertEquals(5, test.get(4));
        try {
            test.get(5);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {

        } catch (Exception e) {
            fail("Should throw IllegalArgumentException");
        }

        try {
            test.get(-1);
            fail("Should throw IllegalArgumentException for negative indices");
        } catch (IllegalArgumentException e) {

        } catch (Exception e) {
            fail("Should throw IllegalArgumentException for negative indices");
        }

        IntList single = IntList.of(5);
        assertEquals(5, single.get(0));
        try {
            single.get(1);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {

        } catch (Exception e) {
            fail("Should throw IllegalArgumentException");
        }
    }

    @Test
    public void testToString() {
        IntList test = IntList.of(1, 2, 3, 4, 5);
        assertEquals("1 2 3 4 5", test.toString());
        assertEquals("2 3 4", IntList.of(2, 3, 4).toString());
        assertEquals("1", IntList.of(1).toString());
    }

    @Test
    public void testEquals() {
        IntList a = IntList.of(1, 2, 3, 4, 5);
        IntList b = IntList.of(1, 2, 3, 4, 5);
        assertTrue("List should equal itself.", a.equals(a));
        assertTrue("List should equal itself.", b.equals(b));
        assertTrue("a should equal b.", a.equals(b));
        assertTrue("b should equal a.", b.equals(a));

        assertFalse(a.equals(new Object()));
        assertFalse(b.equals(234));

        assertFalse(a.equals(IntList.of(1, 2, 3, 4)));
        assertFalse(a.equals(IntList.of(1, 2, 3, 5, 6)));
    }

    @Test
    public void testAdd() {
        IntList a = IntList.of(1, 2, 3);
        assertEquals(a, IntList.of(1, 2, 3));
        a.add(4);
        assertEquals(a, IntList.of(1, 2, 3, 4));
        a.add(5);
        assertEquals(a, IntList.of(1, 2, 3, 4, 5));

        IntList single = IntList.of(1);
        single.add(2);
        assertEquals(single, IntList.of(1, 2));
    }

    @Test
    public void testSmallest() {
        assertEquals(6, IntList.of(63, 6, 6, 74, 7, 8, 52, 33, 43, 6, 6, 32).smallest());
        assertEquals(9, IntList.of(9).smallest());
        assertEquals(9, IntList.of(9, 9, 9, 9, 9, 9, 9, 9, 9, 9).smallest());
        assertEquals(1, IntList.of(10, 9, 8, 7, 6, 5, 4, 3, 2, 1).smallest());
    }

    @Test
    public void testSquaredSum() {
        assertEquals(14, IntList.of(1, 2, 3).squaredSum());
        assertEquals(1, IntList.of(1).squaredSum());
        assertEquals(5, IntList.of(1, 2).squaredSum());
        assertEquals(2, IntList.of(1, 1).squaredSum());
        assertEquals(18, IntList.of(3, 3).squaredSum());
    }

    @Test
    public void testDSquareList() {
        IntList L = IntList.of(1, 2, 3);
        assertEquals(IntList.of(1, 2, 3), L);

        IntList.dSquareList(L);
        assertEquals(IntList.of(1, 4, 9), L);
    }

    /**
     * Do not use the new keyword in your tests. You can create
     * lists using the handy IntList.of method.
     *
     * Make sure to include test cases involving lists of various sizes
     * on both sides of the operation. That includes the empty of, which
     * can be instantiated, for example, with
     * IntList empty = IntList.of().
     *
     * Keep in mind that dcatenate(A, B) is NOT required to leave A untouched.
     * Anything can happen to A.
     */

    @Test
    public void testCatenate() {
        IntList A = IntList.of(1, 2, 3);
        IntList B = IntList.of(4, 5, 6);

        IntList exp = IntList.of(1, 2, 3, 4, 5, 6);
        IntList res = IntList.catenate(A, B);
        // Check that correctly catenates
        assertEquals(exp, res);

        // Cannot modify A
        assertEquals(IntList.of(1, 2, 3), A);
    }

    @Test
    public void testDCatenate() {
        IntList A = IntList.of(1, 2, 3);
        IntList B = IntList.of(4, 5, 6);

        IntList exp = IntList.of(1, 2, 3, 4, 5, 6);
        IntList res = IntList.dcatenate(A, B);
        // Check that correctly catenates
        assertEquals(exp, res);

        // Check that A has been modified
        assertEquals(IntList.of(1, 2, 3, 4, 5, 6), A);
        A.item = 7;
        assertEquals(A.item, res.item);
    }
}