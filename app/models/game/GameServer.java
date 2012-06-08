package models.game;

import com.typesafe.config.*;
import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

import java.util.*;
import static java.util.concurrent.TimeUnit.*;

import play.*;
import play.mvc.*;
import play.libs.*;
import play.libs.F.*;

import akka.util.*;
import akka.actor.*;
import akka.dispatch.*;
import static akka.pattern.Patterns.ask;

public class GameServer extends UntypedActor {
	// Default actor
	static GameServer thisInstance = new GameServer();
	public static GameServer getInstance() {
		return thisInstance;
	}

    static ActorRef defaultServer = Akka.system().actorOf(new Props(GameServer.class));
//	static ActorRef defaultServer = Akka.system().actorOf(new Props(
//				new UntypedActorFactory() {
//					public UntypdedActor create() {
//						return new MyActor("......");
//					}
//				}), "myactor");
    
	private HashMap<Integer, Table> freeTables, busyTables;
	private int freeTableNum, busyTableNum;
	private Config configs;
	private int tableSize;

	@Override
	public void preStart() {
		// some specific initialization for the actor
	}
	@Override
	public void postStop() {
	}

	private GameServer() {
		configs = ConfigFactory.load("../../../conf/game.conf");
		int initTableNum = configs.getInt("initTableNum");
		freeTables = new HashMap<Integer, Table>(initTableNum);
		int tableSize = configs.getInt("tableSize");
		for (int i = 0; i <initTableNum; ++i) {
			freeTables.put(i, new Table(i, tableSize));
		}
	}

	public Table getATable() {
		Iterator ite = freeTables.entrySet().iterator();
		Table newTable = null;
		int newId = 0;
		while (ite.hasNext()) {
			Map.Entry entry = (Map.Entry)ite.next();
			Integer k = (Integer)entry.getKey();
			Table v = (Table)entry.getValue();
			if (v.isFree() == true) {
				newId = k;
				newTable = v;
				break;
			} else {
				freeTables.remove(k);
				freeTableNum--;
				busyTables.put(k, v);
				busyTableNum++;
			}
		}
		if (newTable == null) {
			newId = 0;
			int newTableNum = configs.getInt("newTableNumPerAllocation");
			Table tmpNewTables[] = new Table[newTableNum];
			for (int i = 0; i <newTableNum; ++i) {
				tmpNewTables[i] = new Table(freeTableNum + i, tableSize);
				if (tmpNewTables[i] == null) {
					Logger.error("can't allocate a new table");
					break;
				} else {
					newId++;
					freeTables.put(freeTableNum + i, tmpNewTables[i]);
					freeTableNum++;
				}
			}
			return tmpNewTables[newId];
		}
		return newTable;
	}
 
	public HashMap<Integer, Table> getFreeTables() {
		return freeTables;
	}

	public HashMap<Integer, Table> getBusyTables() {
		return busyTables;
	}
    /**
     * Join a table.
     */
    public static void join(final int userId, final int tableId,
			WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) throws Exception{
		Table toJoinTable = GameServer.getInstance().getFreeTables().get(tableId);
		if (toJoinTable == null) {
			Logger.error ("get table failed " + tableId);
			return;
		}
        // Send the Join message to the room
        String result = (String)Await.result(
				ask(defaultServer, new Join(userId, tableId, out), 1000), Duration.create(1, SECONDS));
        
        if ("OK".equals(result)) {
            // For each event received on the socket,
            in.onMessage(new Callback<JsonNode>() {
               public void invoke(JsonNode event) {
                   
                   // Send a Talk message to the room.
                   defaultServer.tell(new Talk(userId, event.get("text").asText()));
                   
               } 
            });
            
            // When the socket is closed.
            in.onClose(new Callback0() {
               public void invoke() {
                   
                   // Send a Quit message to the room.
                   defaultServer.tell(new Quit(userId));
                   
               }
            });
        } else {
            
            // Cannot connect, create a Json error.
            ObjectNode error = Json.newObject();
            error.put("error", result);
            
            // Send the error to the socket.
            out.write(error);
            
        }
    }
    
    // Members of this room.
    Map<Integer, WebSocket.Out<JsonNode>> members = new HashMap<Integer, WebSocket.Out<JsonNode>>();
    
    public void onReceive(Object message) throws Exception {
        if (message instanceof Join) {
            // Received a Join message
            Join join = (Join)message;
            
            // Check if this userId is free.
            if (members.containsKey(join.userId)) {
                getSender().tell("This userId is already used");
            } else {
                members.put(join.userId, join.outWs);
                notifyAll("join", join.userId, "has entered the room");
                getSender().tell("OK");
            }
        } else if (message instanceof Talk)  {
            // Received a Talk message
            Talk talk = (Talk)message;
            
            notifyAll("talk", talk.userId, talk.text);
        } else if (message instanceof Quit)  {
            // Received a Quit message
            Quit quit = (Quit)message;
            
            members.remove(quit.userId);
            
            notifyAll("quit", quit.userId, "has leaved the room");
        } else {
            unhandled(message);
        }
    }
    
    // Send a Json event to all members
    public void notifyAll(String kind, int user, String text) {
        for (WebSocket.Out<JsonNode> outWs: members.values()) {
            ObjectNode event = Json.newObject();
            event.put("kind", kind);
            event.put("user", user);
            event.put("message", text);
            
            ArrayNode m = event.putArray("members");
            for (int u: members.keySet()) {
                m.add(u);
            }
            
            outWs.write(event);
        }
    }

	// send json event to other players of the same table
	public void notifyTablePlayers(String kind, int user, String text) {
		for (WebSocket.Out<JsonNode> table: members.values()) {
		}
	}
    
    // -- Messages
    public static class Join {
        final int userId;
		final int tableId;
        final WebSocket.Out<JsonNode> outWs;
        
        public Join(int userId, int tableId, WebSocket.Out<JsonNode> outWs) {
            this.userId = userId;
			this.tableId = tableId;
			Table table = GameServer.getInstance().getFreeTables().get(tableId);
			table.addUser(userId);
            this.outWs = outWs;
        }
    }
    
    public static class Talk {
        final int userId;
        final String text;
        
        public Talk(int userId, String text) {
            this.userId = userId;
            this.text = text;
        }
    }
    
    public static class Quit {
        final int userId;

        public Quit(int userId) {
            this.userId = userId;
        }
    }
}