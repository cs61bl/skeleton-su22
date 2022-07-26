/* PriorityQueue interface. Unlike Java's priority queue, items are not limited
   to only Comparable objects and can store any type of object (represented by
   type T) and an associated priority value. */
public interface PriorityQueue<T> {

    /* Returns but does not remove the highest priority element. */
    T peek();

    /* Inserts ITEM into the PriorityQueue with priority value PRIORITYVALUE. */
    void insert(T item, double priorityValue);

    /* Removes and returns the highest priority element. */
    T poll();

    /* Changes the priority value of ITEM to PRIORITYVALUE. Assume the items in
       the PriorityQueue are all distinct. */
    void changePriority(T item, double priorityValue);

    /* Returns the number of items in the PriorityQueue. */
    int size();

    /* Returns true if ITEM is in the PriorityQueue. Does not check what the
       priority value is. */
    boolean contains(T item);
}
