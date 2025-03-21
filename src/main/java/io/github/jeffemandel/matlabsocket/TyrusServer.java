package io.github.jeffemandel.matlabsocket;

import java.util.ArrayList;
import java.util.List;

import org.glassfish.tyrus.server.Server;

import jakarta.websocket.DeploymentException;


public class TyrusServer {
    Server tyrusServer;
    private List<JsonListener> listeners = new ArrayList<>();

    public TyrusServer(int port) {
        try {
            tyrusServer = new Server("localhost", port, "/", null, MatlabServerEndpoint.class);
            tyrusServer.start();
            MatlabServerEndpoint.myTyrusServer = this;
        } catch (DeploymentException e) {
            System.err.println(e.getLocalizedMessage());
        }
    }

    public Server getTyrusServer() {
        return this.tyrusServer;
    }

    public void stopTyrusServer() {
        if (!listeners.isEmpty()) {
            JsonListener listener = listeners.get(0);
            removeJsonListener(listener);
        }
        tyrusServer.stop();
    }

    public List<JsonListener> getListeners() {
        return this.listeners;
    }

    public synchronized void addJsonListener(JsonListener listener) {
        if (listeners.isEmpty()) {
            System.out.println(listener.getClass() + " added in server");
            listeners.add(listener);
        }

    }

    public synchronized void removeJsonListener(JsonListener listener) {
        listeners.remove(listener);

    }

    public interface JsonListener extends java.util.EventListener {
        void onJsonEvent(JsonEvent event);
    }




}