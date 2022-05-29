/*
 * AbstractDataObject.java
 *
 * Created on 2022-05-02
 * Updated on 2022-05-25
 *
 * Description: Abstract data object class that stores the data needed.
 */

package site.overwrite.auditranscribe.io.audt_file.data_encapsulators;

/**
 * Abstract data object class that stores the data needed.
 */
public abstract class AbstractDataObject {
    /**
     * Method that implements a weak equality check by merely using hash codes.
     *
     * @param a First object.
     * @param b Second object.
     * @return Boolean, describing whether both objects are <em>weakly</em> equal to each other.
     * @see <a href="https://stackoverflow.com/a/5443140">This StackOverflow answer</a> on why
     * hashcodes are not a perfect check for equality.
     */
    public boolean weakEqualityCheck(Object a, Object b) {
        return a.hashCode() == b.hashCode();
    }
}
