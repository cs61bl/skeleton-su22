public interface SimpleCollection {

    /** Adds k into the collection. */
    void add(int k);

    /** Removes k from the collection. */
    void remove(int k);

    /** Returns if the collection contains k. */
    boolean contains(int k);

    /** Returns if the collection is empty. */
    boolean isEmpty();

    /** Returns the number of items in the collection. */
    int size();

    /** Returns an array containing all of the elements in this collection. */
    int[] toIntArray();
}
