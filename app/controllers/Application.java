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
     * Display the game room.
     */
    public static Result game(final int userId, final int tableId) {
        if(userId == 0 || tableId == 0) {
            flash("error", "Please choose a valid userId.");
            return redirect(routes.Application.index());
        }
        return ok(game.render(userId, tableId));
    }
    
    /**
     * Handle the play websocket.
     */
    public static WebSocket<JsonNode> entry(final int userId, final int tableId) {
        return new WebSocket<JsonNode>() {
            
            // Called when the Websocket Handshake is done.
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
				Logger.info("userId: " + userId + " tableId: " + tableId);
                
                // Join the game.
                try { 
                    GameServer.join(userId, tableId, in, out);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }
  
}
