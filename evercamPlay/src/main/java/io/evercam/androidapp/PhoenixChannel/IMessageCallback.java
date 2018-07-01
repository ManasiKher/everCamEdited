package io.evercam.androidapp.PhoenixChannel;

public interface IMessageCallback {

    /**
     * @param envelope The envelope containing the message payload and properties
     */
    void onMessage(final Envelope envelope);
}
