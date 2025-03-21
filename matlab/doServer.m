function myServerObj = doServer
    import org.jeffmandel.matlabsocket.*;
    import java.net.URI;
    
    function dispatcher(myServerObj, event)
        % Get the session and message from the JsonEvent
        session = event.getMySession();
        message = event.getMessage();

        % The message is a JSON java.lang.string, so convert it to a MATLAB
        % string then parse the JSON
        messageStruct = jsondecode(string(message));
        name = messageStruct.name;
        fprintf("Client sent %s\n", name);

        % Recover our states from the session's userProperties, again
        % parsing the JSON
        states = jsondecode(string(session.getUserProperties().get("states")));
        if isfield(states, 'name')
            fprintf("States: %s\n", states.name);
        else
            fprintf("States: []\n");
        end

        % Update the value of states and store it back in the
        % userProperties. Note that replace only works because states was
        % initialized with {} in the @onOpen method of the
        % MatlabServerEndpoint
        states.name = name;
        session.getUserProperties().replace("states", jsonencode(states));

        % Send something back to the client
        response = jsonencode(struct("name", name));
        session.getBasicRemote().sendText(response);
        if name == "exit"
            pause(1);
            myServerObj.stopTyrusServer();
        end
    end
    
    % Start the TyrusServer on port 3000 and set the callback to our
    % dispatcher function
    myServerObj = handle(TyrusServer(3000),'CallbackProperties');
    set(myServerObj, 'OnJsonEventCallback', @dispatcher);
    
end