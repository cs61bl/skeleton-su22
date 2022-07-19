import static org.junit.Assert.*;
import org.junit.Test;
import java.util.List;

/**
 * Created by Jenny Huang on 3/12/19.
 */
public class TestMyTrieSet {
    
    
    //Write your own test for add! Do this before running the other tests.

    // assumes add/contains work
    @Test
    public void basicClearTest() {
        // MyTrieSet t = new MyTrieSet();
        // for (int i = 0; i < 455; i++) {
        //     t.add("hi" + i);
        //     //make sure put is working via contains
        //     assertTrue(t.contains("hi" + i));
        // }
        // t.clear();
        // for (int i = 0; i < 455; i++) {
        //     assertFalse(t.contains("hi" + i));
        // }
    }

    // assumes add works
    @Test
    public void basicContainsTest() {
        // MyTrieSet t = new MyTrieSet();
        // assertFalse(t.contains("waterYouDoingHere"));
        // t.add("waterYouDoingHere");
        // assertTrue(t.contains("waterYouDoingHere"));
    }

    // assumes add works
    @Test
    public void basicPrefixTest() {
        // String[] saStrings = new String[]{"same", "sam", "sad", "sap"};
        // String[] otherStrings = new String[]{"a", "awls", "hello"};

        // MyTrieSet t = new MyTrieSet();
        // for (String s: saStrings) {
        //     t.add(s);
        // }
        // for (String s: otherStrings) {
        //     t.add(s);
        // }

        // List<String> keys = t.keysWithPrefix("sa");
        // for (String s: saStrings) {
        //     assertTrue(keys.contains(s));
        // }
        // for (String s: otherStrings) {
        //     assertFalse(keys.contains(s));
        // }
    }
}
