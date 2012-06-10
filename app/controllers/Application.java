package controllers;

import play.*;
import play.mvc.*;
import play.libs.F.*;

import org.codehaus.jackson.*;

import views.html.*;

import models.game.*;

public class Application extends Controller {
  
    /**
     * Display the home page.
     */
    public static Result index() {
        return ok(index.render());
    }
  
    /**
     * Display the chat room.
     */
    public static Result chatRoom(final int userId, final int tableId) {
        if(userId == 0 || tableId == 0) {
            flash("error", "Please choose a valid userId.");
            return redirect(routes.Application.index());
        }
        return ok(chatRoom.render(userId, tableId));
    }
    
    /**
     * Handle the chat websocket.
     */
    public static WebSocket<JsonNode> chat(final int userId, final int tableId) {
        return new WebSocket<JsonNode>() {
            
            // Called when the Websocket Handshake is done.
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out){
                
                // Join the chat room.
                try { 
                    GameServer.join(userId, tableId, in, out);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }
  
}
