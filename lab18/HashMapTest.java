import static org.junit.Assert.*;
import org.junit.Test;

import java.util.HashSet;

public class HashMapTest {

    @Test
    public void testConstructor() {
        // assert constructors are initialized, seem to work correctly, and
        // don't cause errors
        HashMap<String, String> dictionary = new HashMap<String, String>();
        assertEquals(0, dictionary.size());
        assertEquals(16, dictionary.capacity());

        dictionary = new HashMap<String, String>(10);
        assertEquals(0, dictionary.size());
        assertEquals(10, dictionary.capacity());

        // simply test that the constructor exists, resizeTest will do the rest
        dictionary = new HashMap<String, String>(10, 1);
        assertEquals(0, dictionary.size());
        assertEquals(10, dictionary.capacity());
    }

    @Test
    public void testClear() {
        HashMap<String, String> h = new HashMap<String, String>();
        h.put("claire", "ko");
        assertTrue(h.containsKey("claire"));
        assertEquals("ko", h.get("claire"));
        assertEquals(1, h.size());
        h.put("matt", "pancakes");
        assertTrue(h.containsKey("matt"));
        assertEquals("pancakes", h.get("matt"));
        assertEquals(2, h.size());
        h.clear();
        assertFalse(h.containsKey("claire"));
        assertFalse(h.containsKey("matt"));
        assertEquals(0, h.size());
    }

    @Test
    public void testPut() {
        HashMap<String, String> h = new HashMap<String, String>();
        h.put("mike", "parth");
        assertTrue(h.containsKey("mike"));
        assertEquals("parth", h.get("mike"));
        assertFalse(h.containsKey("alex"));
        assertFalse(h.containsKey("henry"));
        assertEquals(1, h.size());
        h.put("eli", "kevin");
        assertTrue(h.containsKey("eli"));
        assertFalse(h.containsKey("kevin"));
        assertEquals(2, h.size());
    }

    @Test
    public void testGet() {
        HashMap<String, String> h = new HashMap<String, String>();
        h.put("claire", "lam");
        assertEquals("lam", h.get("claire"));
    }

    @Test
    public void testRemove() {
        HashMap<String, String> h = new HashMap<String, String>();
        h.put("alex", "claire");
        assertTrue(h.containsKey("alex"));
        assertEquals("claire", h.get("alex"));
        assertEquals(1, h.size());
        h.remove("alex");
        assertFalse(h.containsKey("alex"));
        assertEquals(0, h.size());
    }

    @Test
    public void testResize() {
        HashMap<String, String> h = new HashMap<String, String>(2);
        assertEquals(2, h.capacity());
        h.put("connor", "grace");
        h.put("zoe", "matt");
        assertEquals(4, h.capacity());

        h = new HashMap<String, String>(10, 1);
        for (int i = 1; i <= 10; i += 1) {
            h.put(Integer.toString(i), Integer.toString(i));
        }
        assertEquals(10, h.size());
        assertEquals(10, h.capacity());
        h.put("matt", "matt");
        assertEquals(11, h.size());
        assertEquals(20, h.capacity());
        h.remove("matt");
        assertEquals(10, h.size());
        assertEquals(20, h.capacity());
    }

    @Test
    public void basicFunctionalityTest() {
        HashMap<String, String> dictionary = new HashMap<String, String>();
        assertEquals(0, dictionary.size());
        assertEquals(16, dictionary.capacity());

        // can put objects in dictionary and get them
        dictionary.put("grace", "connor");
        assertTrue(dictionary.containsKey("grace"));
        assertEquals("connor", dictionary.get("grace"));
        assertEquals(1, dictionary.size());

        // putting with existing key replaces key
        dictionary.put("grace", "kevin");
        assertEquals(1, dictionary.size());
        assertEquals("kevin", dictionary.get("grace"));
        assertEquals("kevin", dictionary.remove("grace"));
        assertEquals(null, dictionary.get("grace"));
        assertEquals(0, dictionary.size());

        // placing key in multiple times does not affect behavior
        HashMap<String, Integer> studentIDs = new HashMap<String, Integer>();
        studentIDs.put("zoe", 12345);
        assertEquals(1, studentIDs.size());
        assertEquals(12345, studentIDs.get("zoe").intValue());
        studentIDs.put("shreya", 345);
        assertEquals(2, studentIDs.size());
        assertEquals(12345, studentIDs.get("zoe").intValue());
        assertEquals(345, studentIDs.get("shreya").intValue());
        studentIDs.put("shreya", 345);
        assertEquals(2, studentIDs.size());
        assertEquals(12345, studentIDs.get("zoe").intValue());
        assertEquals(345, studentIDs.get("shreya").intValue());
        studentIDs.put("shreya", 345);
        assertEquals(2, studentIDs.size());
        assertEquals(12345, studentIDs.get("zoe").intValue());
        assertEquals(345, studentIDs.get("shreya").intValue());
        assertTrue(studentIDs.containsKey("zoe"));
        assertTrue(studentIDs.containsKey("shreya"));

        // ensure that containsKey does not always return true
        assertFalse(studentIDs.containsKey("jay"));
        assertFalse(studentIDs.containsKey("travis"));
        studentIDs.put("travis", 612);
        studentIDs.put("jay", 216);
        assertTrue(studentIDs.containsKey("travis"));
        assertTrue(studentIDs.containsKey("jay"));

        // confirm hash map can handle values being the same
        assertEquals(345, studentIDs.get("shreya").intValue());
        studentIDs.put("anya", 345);
        assertEquals(345, studentIDs.get("anya").intValue());
        assertEquals(studentIDs.get("anya"), studentIDs.get("shreya"));
    }

    @Test
    public void iteratorTest() {
        // replicate basic functionality test while building database
        HashMap<String, Integer> studentIDs = new HashMap<String, Integer>();
        studentIDs.put("zoe", 12345);
        studentIDs.put("jay", 345);
        assertTrue(studentIDs.containsKey("zoe"));
        assertTrue(studentIDs.containsKey("jay"));

        // ensure that containsKey does not always return true
        assertFalse(studentIDs.containsKey("jay"));
        assertFalse(studentIDs.containsKey("travis"));
        assertFalse(studentIDs.containsKey("ryan"));
        studentIDs.put("ryan", 162);
        assertTrue(studentIDs.containsKey("ryan"));

        // confirm hashMap can handle values being the same
        studentIDs.put("grace", 12345);
        assertEquals(studentIDs.get("grace"), studentIDs.get("zoe"));

        HashSet<String> expected = new HashSet<String>();
        expected.add("zoe");
        expected.add("jay");
        expected.add("ryan");
        expected.add("grace");

        HashSet<String> output = new HashSet<String>();
        for (String name : studentIDs) {
            output.add(name);
        }
        assertEquals(expected, output);
    }

}