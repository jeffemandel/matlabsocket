package io.github.jeffemandel.matlabsocket;

import java.io.IOException;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/")
public class MatlabServerEndpoint {
    private Session mySession;
    public static TyrusServer myTyrusServer;

    public MatlabServerEndpoint() {
    }

    @OnOpen
    public void onOpen(Session session) {
        // Set the initial value of states to be empty so that the MATLAB callback can use replace.
        session.getUserProperties().put("states", "{}");
        this.mySession = session;
        System.out.println("Server connected");
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        // We have a message, so create an event with the session and the message and send it to the listener
        JsonEvent event = new JsonEvent(session, message);
        for (JsonListener listener : myTyrusServer.getListeners()) {
            listener.onJsonEvent(event);
        }
        // System.out.println("Server received message: " + message.toString());
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("Server disconnected");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Error occurred: " + throwable.getMessage());
    }

    // Probably not useful, as the MATLAB callback will have the Session but not a reference to the endpoint
    public void sendTextMessage(String message) throws IOException {
        this.mySession.getBasicRemote().sendText(message);
    }


}
