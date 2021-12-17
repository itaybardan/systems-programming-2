package bgu.spl.mics;

/**
 * The message-bus is a shared object used for communication between
 * microservices.
 * It should be implemented as a thread-safe singleton.
 * The message-bus implementation must be thread-safe as
 * it is shared between all the microservices in the system.
 * You must not alter any of the given methods of this interface.
 * You cannot add methods to this interface.
 */
public interface MessageBus {

    //Queries
    boolean isEventSubsEmpty(Class<? extends Event> type);

    boolean isBroadcastSubsEmpty(Class<? extends Broadcast> type);

    boolean isRegistered(MicroService m);

    boolean isEventProcessed(Event event, MicroService m); //is the event in the message queue of a microservice

    boolean isBroadcastProcessed(Broadcast broadcast, MicroService m);

    /**
     * Subscribes {@code m} to receive {@link Event}s of type {@code type}.
     * <p>
     *
     * @param <T>  The type of the result expected by the completed event.
     * @param type The type to subscribe to,
     * @param m    The subscribing microservice.
     * @pre: m </< event_subscribers.get(type).getKey()
     * @post: m < event_subscribers.get(type).getKey()
     */
    <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m);

    /**
     * Subscribes {@code m} to receive {@link Broadcast}s of type {@code type}.
     * <p>
     *
     * @param type The type to subscribe to.
     * @param m    The subscribing microservice.
     * @pre: m </< broadcast_subscribers.get(type)
     * @post:m < broadcast_subscribers.get(type)
     */
    void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m);

    /**
     * Notifies the MessageBus that the event {@code e} is completed and its
     * result was {@code result}.
     * When this method is called, the message-bus will resolve the {@link Future}
     * object associated with {@link Event} {@code e}.
     * <p>
     *
     * @param <T>    The type of the result expected by the completed event.
     * @param e      The completed event.
     * @param result The resolved result of the completed event.
     * @pre e.getFuture().get(1, TimeUnit.MILLISECONDS) == null
     * @post e.getFuture.get() == result
     */
    <T> void complete(Event<T> e, T result);

    /**
     * Adds the {@link Broadcast} {@code b} to the message queues of all the
     * microservices subscribed to {@code b.getClass()}.
     * <p>
     *
     * @param b The message to added to the queues.
     * @post: V MicroService m < messagesQueue.keySet() (m < broadcast_subscribers.get(b.getClass())  -->>  b < messagesQueue.get(m) )
     */
    void sendBroadcast(Broadcast b);

    /**
     * Adds the {@link Event} {@code e} to the message queue of one of the
     * microservices subscribed to {@code e.getClass()} in a round-robin
     * fashion. This method should be non-blocking.
     * <p>
     *
     * @param <T> The type of the result expected by the event and its corresponding future object.
     * @param e   The event to add to the queue.
     * @return {@link Future<T>} object to be resolved once the processing is complete,
     * null in case no microservice has subscribed to {@code e.getClass()}.
     * @post: E MicroService m < messagesQueue.keySet()  (m < event.subscribers.get(e.getClass()) &&  e < messagesQueue.get(m) )
     */
    <T> Future<T> sendEvent(Event<T> e);

    /**
     * Allocates a message-queue for the {@link MicroService} {@code m}.
     * <p>
     *
     * @param m the microservice to create a queue for.
     * @post: m < messagesQueue.keySet()
     */
    void register(MicroService m);

    /**
     * Removes the message queue allocated to {@code m} via the call to
     * {@link #register(bgu.spl.mics.MicroService)} and cleans all references
     * related to {@code m} in this message-bus. If {@code m} was not
     * registered, nothing should happen.
     * <p>
     *
     * @param m the microservice to unregister.
     * @post: V Pair pair < event_subscribers.valueSet() (m </< pair.getKey()) &&
     * V LinkedList list < event_subscribers.valueSet() ( m </< list ) &&
     * m </< messagesQueue.keySet()
     */
    void unregister(MicroService m);

    /**
     * Using this method, a <b>registered</b> microservice can take message
     * from its allocated queue.
     * This method is blocking meaning that if no messages
     * are available in the microservice queue it
     * should wait until a message becomes available.
     * The method should throw the {@link IllegalStateException} in the case
     * where {@code m} was never registered.
     * <p>
     *
     * @param m The microservice requesting to take a message from its message
     *          queue.
     * @return The next message in the {@code m}'s queue (blocking).
     * @throws InterruptedException if interrupted while waiting for a message
     *                              to became available.
     */
    Message awaitMessage(MicroService m) throws InterruptedException;


}
