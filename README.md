# matlabsocket
Implementations of the Jakarta Websockets to support a stateful MATLAB micro service

# Overview
I am developing Software as a Medical Device that employs MATLAB to generate pharmacokinetic predictions. This requires getting input from the user, sending it to MATLAB, getting output from MATLAB, and sending this back to the user. For each user session, the state of the system needs to be preserved. I have utilized the MATLAB Compiler SDK to generate a Java servlet that can be called from Spring Boot, but this means the MATLAB code must be redployed every time there is a minor change in the UI. A better approach is to have the the MATLAB code deployed as a microservice using the MATLAB Compiler using Websockets for communication. There is an existing project for this; [https://github.com/jebej/MatlabWebSocket](https://github.com/jebej/MatlabWebSocket), but I found it had a number of limitations:
- It is based on javax, rather than jakarta
- It doesn't expose event listeners to allow for MATLAB callbacks
- It doesn't expose the session object, allowing storing the state information
- It doesn't provide for promise-based communication from the client

To address these limitations, I wrote this simple client/server package
# Installation
1. Download or clone the project
2. Decide which Java SDK you will be using. MATLAB includes Java 8, and explains how to install and use Java 11. I use Java 17. **If you use anything other than 8, you need to change the Java environment prior to starting MATLAB**
3. Edit the pom.xml file to relect your choice of JDK and mvn package
4. Copy the resulting jar (target/matlabsocket-1.0.jar) somewhere and add this to the fullfile(prefdir,'javaclasspath.txt') file. No, Mathworks gives no guidance on the standard location for user-supplied jars.
5. Start the server in MATLAB. There is a simple example - doServer.m. This will launch a standalone Tyrus Grizzly server on the specified port listening on 0.0.0.0 at path /.
6. There is a simple client in the jar that can be run from the command line:
  ````
  java -jar matlabsocket-1.0.jar  ws://localhost:3000/ init run exit
  ````
  This will open a websocket to the specified URI and pass 3 messages. When the server receives "exit", it closes the socket. Note that the client shouldn't be called from the same MATLAB session as the server; it will be blocked and you might have to sudo kill -9 your MATLAB session.
  # Use in a larger project
 ## MATLAB
 The dispatcher function should be moved to a separate file and contain the business logic. It expects to get called with two arguments:
  - myServerObj - the javahandle_withcallbacks.io.github.jeffemandel.matlabsocket.TyrusServer object.
  - event - JsonEvent, which has getters for session and message.
  The session is used to access the UserProperties, which is a hashmap that can be used to store the state information. There isn't an easy way to pass MATLAB variables directly to Java, so I use jsonencode.
  The message is a java.lang.string, which must be converted to a MATLAB string. Again, I use jsondecode/jsonencode for messages.
## Java
I looked at various implementations of websockets in Spring, but they tend to solve the problem of subscribing to unsolicited messages from the server; I needed a way to send a request to the server and wait for the answer. This is handled by PromiseClient. This class creates a new instance of CompletableFuture<String> and instantiates a new instance of MessageHandler.Whole<String> with it. When the MessageHandler gets a message, it completes the promise, and the PromiseClient returns the message (or times out). This allows a Spring @Controller to get the data from a form, serialize it to JSON, send it to MATLAB for processing, and return the result to the user.
## Javascript
This should be much easier, as the onMessage can be put in an event listener. The trick will be figuring out how to share the session between Java and Javascript.
