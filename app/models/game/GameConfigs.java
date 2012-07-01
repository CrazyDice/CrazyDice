package models.game;

import play.*;
import com.typesafe.config.*;
import java.util.*;
import java.lang.*;

public final class GameConfigs
{
	public static GameConfigs INSTANCE = new GameConfigs();
	private HashMap<String, String> confPaths;
	private Config assetsConfigs;
	private String mode;

	private GameConfigs() {
		confPaths = new HashMap<String, String>();
		assetsConfigs = ConfigFactory.load("assets.conf");
		mode = Play.isDev() ? "DEV" : "PROD";

	}
	public static String getAssetPath(String assetName) {
		String assetPath = new String(INSTANCE.assetsConfigs.getString(INSTANCE.mode + "." + assetName));
		Logger.of("gameserver").info(assetPath);
//		Set<String> keys = INSTANCE.assetsConfigs.keys();
//		for (Iterator< Stringf > ite = set.iterator(); ite.hasNext(); ) {
//			Logger.of("gameserver").info(ite.next());
//		}
//		if (Play.isDev()) {
//			return INSTANCE.assetsDev.getString(assetName);
//		} else {
//			return INSTANCE.assetsProd.getString(assetName);
//		}
		return assetPath;
	}
}
