package ie.ul.fitbook.interfaces;

/**
 * This class is similar to {@link ActionHandler} but it allows a user to pass in an
 * instance of type T to it
 * @param <T> the type of the object to pass into the interface
 */
@FunctionalInterface
public interface ActionHandlerConsumer<T> {
    /**
     * Does the object defined by this interface
     * @param object the object to operate on
     */
    void doAction(T object);
}
