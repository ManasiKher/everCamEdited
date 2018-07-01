package io.evercam.androidapp.PhoenixChannel;

public enum ChannelEvent {
    CLOSE("phx_close"),
    ERROR("phx_error"),
    JOIN("phx_join"),
    REPLY("phx_reply"),
    LEAVE("phx_leave");

    private final String phxEvent;

    public static ChannelEvent getEvent(final String phxEvent) {
        for (final ChannelEvent ev : values()) {
            if (ev.getPhxEvent().equals(phxEvent)) {
                return ev;
            }
        }
        return null;
    }

    ChannelEvent(final String phxEvent) {
        this.phxEvent = phxEvent;
    }

    public String getPhxEvent() {
        return phxEvent;
    }
}
