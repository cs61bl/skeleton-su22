  
import java.util.Iterator;

public interface Map61BL<K, V> extends Iterable<K> {

    /* Removes all of the mappings from this map. */
    void clear();

    /* Returns true if this map contains a mapping for the specified key KEY. */
    boolean containsKey(K key);

    /* Returns the value to which the specified key KEY is mapped, or null if
       this map contains no mapping for KEY. */
    V get(K key);

    /* Puts the specified key-value pair (KEY, VALUE) in this map. */
    void put(K key, V value);

    /* Removes and returns a key KEY and its associated value. */
    V remove(K key);

    /* Removes a particular key-value pair (KEY, VALUE) and returns true if
       successful. */
    boolean remove(K key, V value);

    /* Returns the number of key-value pairs in this map. */
    int size();

    /* Returns an Iterator over the keys in this map. */
    Iterator<K> iterator();
}