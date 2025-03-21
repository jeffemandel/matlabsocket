package io.github.jeffemandel.matlabsocket;

import java.util.EventObject;
import jakarta.websocket.Session;

public class JsonEvent extends EventObject {
    // A class that provides the session and message to the EventListener
    private String message;
    private Session mySession;

    public JsonEvent(Session source, String message) {
        super(source);
        this.mySession = source;
        this.message = message;
    }

    public Session getMySession() {
        return this.mySession;
    }

    public String getMessage() {
        return this.message;
    }
}

