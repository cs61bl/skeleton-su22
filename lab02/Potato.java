/** The Potato class has its instance variables as private.
 *  @authors Antares Chen, Jedidiah Tsang
 */
public class Potato {

    /* An instance variable representing the potato's species. */
    private String variety;
    /* An instance variable representing the potato's age. */
    private int age;

    /** A constructor that returns a very young russet burbank potato. 
    Note that this means that the Java default constructor is not generated
    since one is specified below. */
    public Potato() {
        this.variety = "Russet Burbank";
        this.age = 0;
    }

    /** A constructor that allows you to specify its variety and age. */
    public Potato(String variety, int age) {
        this.variety = variety;
        this.age = age;
    }

    /** A getter method that returns the potato's type. */
    public String getVariety() {
        return this.variety;
    }

    /** A getter method that returns the potato's age. */
    public int getAge() {
        return this.age;
    }

    /** A setter method that sets the potato's age to AGE. */
    public void setAge(int age) {
        this.age = age;
    }

    /** A method that grows the potato. Note it increases its age by 1. */
    public void grow() {
        System.out.println("Photosynthesis!");
        this.age = this.age + 1;
    }

    /** Did you know potatoes can flower? No? Neither did I... */
    public void flower() {
        System.out.println("I am now a beautiful potato");
    }
}
