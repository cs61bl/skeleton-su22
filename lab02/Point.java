/** A point in 2D space.
 *  @author Antares Chen and Jedidiah Tsang
 */
class Point {

    /** The x coordinate of Point. */
    private double x;
    /** The y coordinate of Point. */
    private double y;

    /** A constructor that returns a point at the origin. */
    Point() {
        this.x = 0;
        this.y = 0;
    }

    /** A constructor that takes in the x, y coordinate of a point. */
    Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /** A copy constructor that creates a point with the same x and y values. */
    Point(Point point) {
        this.x = point.getX();
        this.y = point.getY();
    }

    /** Returns the x-coordinate of this point. */
    double getX() {
        return this.x;
    }

    /** Returns the y-coordinate of this point. */
    double getY() {
        return this.y;
    }

    /** Sets the x-coordinate to X in this point. */
    void setX(double x) {
        this.x = x;
    }

    /** Sets the y-coordinate to Y in this point. */
    void setY(double y) {
        this.y = y;
    }

    /** Provides a String representation of our Point object.*/
    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }
    
    /** Checks for equality between two Points. */
    @Override
    public boolean equals(Object o) {
        Point other = (Point) o;
        return (this.x == other.x) && (this.y == other.y);
    }
}
