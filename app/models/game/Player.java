package models.game;

import play.*;
import play.mvc.*;
import play.libs.*;
import play.libs.F.*;

import akka.util.*;
import akka.actor.*;
import akka.dispatch.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

import static java.util.concurrent.TimeUnit.*;

public class Player {
	private WebSocket.Out<JsonNode> outWs;
    
    public Player(ActorRef GameServer, int uid, int tableId) {
        
        // Create a Fake socket out for the Player that log events to the console.
        outWs = new WebSocket.Out<JsonNode>() {
            public void write(JsonNode frame) {
                Logger.of("Player").info(Json.stringify(frame));
            }
            
            public void close() {}
            
        };
        
        // Join the room
        GameServer.tell(new GameServer.Join(uid, tableId, outWs));
        
        // Make the Player talk every 30 seconds
//        Akka.system().scheduler().schedule(
//            Duration.create(30, SECONDS),
//            Duration.create(30, SECONDS),
//            Table,
//            new Table.Talk("Player", "I'm still alive")
//        );
        
    }
    
}
