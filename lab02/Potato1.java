/** The Potato1 class has its instance variables as NOT private
 *  @author Antares Chen
 */
public class Potato1 {

    /* An instance variable representing the potato's species. 
    Note that since this variable is not private, */
    public String variety;
    /* An instance variable representing the potato's age. */
    public int age;

    /** A constructor that returns a very young russet burbank potato. */
    public Potato1() {
        this.variety = "Russet Burbank";
        this.age = 0;
    }

    /** A constructor that allows you to specify its variety and age. */
    public Potato1(String variety, int age) {
        this.variety = variety;
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
