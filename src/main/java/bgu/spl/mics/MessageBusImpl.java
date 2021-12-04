package bgu.spl.mics;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
    /**
     * @param type The type to subscribe to,
     * @param m    The subscribing micro-service.
     * @param <T>  the type the event holds
     */
    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        // TODO Auto-generated method stub

    }

    /**
     * @param type The type to subscribe to.
     * @param m    The subscribing micro-service.
     */
    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        // TODO Auto-generated method stub

    }

    /**
     * @param e      The completed event.
     * @param result The resolved result of the completed event.
     * @param <T>    the type the event holds
     */
    @Override
    public <T> void complete(Event<T> e, T result) {
        // TODO Auto-generated method stub

    }

    /**
     * @param b The message to added to the queues.
     */
    @Override
    public void sendBroadcast(Broadcast b) {
        // TODO Auto-generated method stub

    }

    /**
     * @param e   The event to add to the queue.
     * @param <T>
     * @return
     */
    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param m the micro-service to create a queue for.
     */
    @Override
    public void register(MicroService m) {
        // TODO Auto-generated method stub

    }

    /**
     * @param m the micro-service to unregister.
     */
    @Override
    public void unregister(MicroService m) {
        // TODO Auto-generated method stub

    }

    /**
     * @param m The micro-service requesting to take a message from its message
     *          queue.
     * @return the resolved message
     * @throws InterruptedException if await raise some exception
     */
    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        // TODO Auto-generated method stub
        return null;
    }


}
