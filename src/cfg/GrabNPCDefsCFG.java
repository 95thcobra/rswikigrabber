package cfg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Grabs item information from RS Wiki and puts them into a json file.
 * 
 * @author Sky
 */
public class GrabNPCDefsCFG {
	public static boolean firstLine = false;

	/**
	 * Read id and name from txt file.
	 * 
	 * @return
	 * @throws Exception
	 */
	public static void start() throws Exception {

		Path path = Paths.get("npcdefs.cfg");
		File file = path.toFile();
		if (!file.exists()) {
			file.createNewFile();
		}
		System.out.println("dumping started");
		BufferedReader in = new BufferedReader(new FileReader("npclist78.txt"));

		try (FileWriter writer = new FileWriter(file)) {
		}

		String line;
		while ((line = in.readLine()) != null) {
			if (!firstLine) {
				firstLine = true;
				continue;
			}
			if (!line.contains(" : ")) {
				continue;
			}

			int first = line.indexOf(" : ") + 3;
			int second = line.indexOf(" - ", first + 1);

			if (first <= 0 || second <= 0) {
				continue;
			}

			String idText = line.substring(0, first - 3);
			int id = Integer.parseInt(idText);
			String npcName = line.substring(first, second);
			npcName = npcName.replace(" ", "_");

			int combat = 1;
			int hitPoints = 1;

			try {
				combat = getCombat(npcName);
				hitPoints = getHp(npcName);
				if (combat <= 0) {
					combat = 1;
				}
				if (hitPoints <= 0) {
					hitPoints = 1;
				}
			} catch (Exception e) {
				// 1
			}

			System.out.println("id:" + id + " name:" + npcName);

			try (FileWriter writer = new FileWriter(file, true)) {
				writer.write("npc	=	" + id + "		" + npcName + "				" + combat + "	" + hitPoints + "\n");
			}
		}
		in.close();
		System.out.println("DONE!");
	}

	public static int getCombat(String npcName) {
		try {
			return getNpcInfo(npcName, "<th colspan=\"8\"> <a href=\"/wiki/Combat\" title=\"Combat\">Combat</a>");
		} catch (Exception e) {
			return 0;
		}

	}

	public static int getHp(String npcName) {
		try {
			return getNpcInfo(npcName, "<th colspan=\"8\"> <a href=\"/wiki/Hit_points\" title=\"Hit points\" class=\"mw-redirect\">Hit points</a>");
		} catch (Exception e) {
			return 0;
		}
	}

	public static int getNpcInfo(String npcName, String text) throws Exception {
		if (npcName.contains("Zombie")) {
			npcName = "Zombie_(Common)";
		}
		URL url = new URL("http://2007.runescape.wikia.com/wiki/" + npcName.replace(' ', '_'));
		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		int value = 0;
		while ((line = in.readLine()) != null) {
			if (line.contains(text)) {
				// Go to next line.
				line = in.readLine();
				String first = "</th><td colspan=\"12\"> ";
				if (line.contains(first)) {
					String valueString = line.substring(line.indexOf("> ") + 2);
					value = Integer.parseInt(valueString);
				}
			}
		}
		in.close();
		return value;
	}
}