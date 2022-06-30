import org.junit.Test;
import static org.junit.Assert.*;

public class ListSetTest {

    @Test
    public void testBasics() {
        ListSet aSet = new ListSet();
        assertEquals(0, aSet.size());
        for (int i = -50; i < 50; i += 2) {
            aSet.add(i);
            assertTrue(aSet.contains(i));
        }
        assertEquals(50, aSet.size());
        for (int i = -50; i < 50; i += 2) {
            aSet.remove(i);
            assertFalse(aSet.contains(i));
        }
        assertTrue(aSet.isEmpty());
        assertEquals(0, aSet.size());
    }

}
