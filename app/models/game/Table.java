package models.game;

import akka.util.*;
import akka.actor.*;
import akka.dispatch.*;
import static akka.pattern.Patterns.ask;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

import java.util.*;
import static java.util.concurrent.TimeUnit.*;

/**
 * A table is an Actor.
 */
public class Table {
	// table id
	private int id;
	private int players[];
	private int playerNum;
    
	public Table(int id, int num) {
		this.id = id;
		playerNum = 0;
		players = new int[num];
		Arrays.fill(players, 0);
	}

	// -1 failure exception 0 failure existing user, int value new user index
	public int addUser(int uid) {
		if(playerNum == players.length) {
			return -1;
		}
		
		int i;
		for (i = 0; i < players.length; ++i) {
			if (players[i] == uid) {
				return 0;
			}
			if (players[i] == 0) {
				break;
			}
		}
		players[i] = uid;
		playerNum++;

		return i;
	}

	// -1 failure exception 0 find none user, int value removed user index
	public int removeUser(int uid) {
		if(playerNum == 0) {
			return -1;
		}
		int i;
		for (i = 0; i < players.length; ++i) {
			if (players[i] == uid) {
				players[i] = 0;
				playerNum--;
				return i;
			}
		}
		return 0;
	}

	boolean isFree() {
		return playerNum == players.length;
	}
}
