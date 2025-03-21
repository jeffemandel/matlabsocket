package io.github.jeffemandel.matlabsocket;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.*;
import jakarta.json.Json;

import jakarta.websocket.Session;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.WebSocketContainer;

public class PromiseClient {

    private Session mySession;
    private MatlabSocketClient myClient;

    public static void main(String[] args) {
        // For testing call the program with the URI and one or more messages to send to
        // the server:
        // java -jar target/matlabsocket-1.0.jar ws://localhost:3000/ init run exit
        try {
            URI endpointURI = new URI(args[0]);
            PromiseClient instance = new PromiseClient(endpointURI);
            for (int i = 1; i < args.length; i++) {
                String jsonData = Json.createObjectBuilder().add("name", args[i]).build().toString();
                String result = instance.sendMessageAndWait(jsonData);
                System.out.println("Server returned: " + result);
            }

        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
        }

    }

    public PromiseClient(URI endpointURI) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            myClient = new MatlabSocketClient();
            mySession = container.connectToServer(myClient, endpointURI);
            // System.out.println("Client connected");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String sendMessageAndWait(String message) {

        // Create a new CompletableFuture and a messageHandler with it and add this to the session
        CompletableFuture<String> messageFuture = new CompletableFuture<java.lang.String>();
        PromiseHandler myHandler = new PromiseHandler(messageFuture);
        mySession.addMessageHandler(myHandler);

        String result;
        try {
            mySession.getBasicRemote().sendText(message);
            // Read the response from the CompletableFuture or timeout after 4 seconds
            result = messageFuture.get(4L, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | IOException e) {
            result = e.getLocalizedMessage();
            System.err.println("Exception got: " + result);
        } catch (TimeoutException e) {
            result = "timeout";
            System.err.println("Exception got: " + result);
        }
        // Remove the spent handler (we can't reuse a promise)
        mySession.removeMessageHandler(myHandler);

        return result;

    }

    class MatlabSocketClient extends Endpoint {
        Session mySession;

        @Override
        public void onOpen(Session session, EndpointConfig arg1) {
            this.mySession = session;
        }
    }

    class PromiseHandler implements MessageHandler.Whole<String> {

        private CompletableFuture<String> messageFuture;

        public PromiseHandler(CompletableFuture<String> messageFuture) {
            this.messageFuture = messageFuture;
        }

        @Override
        public void onMessage(String message) {
            // System.out.println("Message Handler got: " + message);
            messageFuture.complete(message);
        }

    }

}
