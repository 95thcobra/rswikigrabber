import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Grabs item information from RS Wiki and puts them into a json file.
 * 
 * @author Sky
 */
public class GrabEquipmentReqs {

	/**
	 * Read id and name from txt file.
	 * 
	 * @return
	 * @throws IOException
	 */
	public static boolean save() throws IOException {

		Path path = Paths.get("test.json");
		File file = path.toFile();
		if (!file.exists()) {
			file.createNewFile();
		}
		try (FileWriter writer = new FileWriter(file)) {
			Gson builder = new GsonBuilder().setPrettyPrinting().create();
			JsonArray jsonArray = new JsonArray();
			JsonObject jsonObject = new JsonObject();

			jsonObject.addProperty("string1", "text1");
			jsonObject.addProperty("string2", "text2");
			jsonObject.addProperty("number1", 1);
			jsonArray.add(jsonObject);

			writer.write(builder.toJson(jsonArray));

			writer.close();
		} catch (IOException ex) {
			// logger.log(Level.SEVERE, "An error occured while trying to save a
			// player file.");
		}
		return true;
	}

	public static void start() throws IOException {

		Path path = Paths.get("myequipmentreqs.json");
		File file = path.toFile();
		if (!file.exists()) {
			file.createNewFile();
		}

		Gson builder = new GsonBuilder().setPrettyPrinting().create();
		JsonArray jsonArray = new JsonArray();

		BufferedReader in = new BufferedReader(new FileReader("itemlist78.txt"));
		String line;
		while ((line = in.readLine()) != null) {
			if (!line.contains(" : ")) {
				continue;
			}
			int first = line.indexOf(" : ") + 3;
			int second = line.indexOf(" : ", first + 1);

			String idText = line.substring(0, first - 3);
			int id = Integer.parseInt(idText);
			String itemName = line.substring(first, second);

			/////////////////////////////////////////////
			Map<String, Integer> kanker;
			boolean equipable;
			try {
				equipable = getEquipable(itemName);
				kanker = getRequirementsMaps(itemName);
			} catch (Exception e) {
				continue;
			}

			if (!equipable) {
				continue;
			}

			JsonArray requirements = new JsonArray();

			for (Map.Entry<String, Integer> entry : kanker.entrySet()) {
				JsonObject test2 = new JsonObject();
				test2.addProperty("skill", entry.getKey());
				test2.addProperty("level", entry.getValue());
				requirements.add(test2);
			}

			if (kanker.size() <= 0) {
				continue;
			}
			/////////////////////////////////////////////

			try (FileWriter writer = new FileWriter(file)) {
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("id", id);
				jsonObject.add("requirements", requirements);
				jsonArray.add(jsonObject);
				writer.write(builder.toJson(jsonArray));
				writer.close();
				System.out.println("id: " + id + ", itemName: " + itemName);
			} catch (IOException ex) {
			}
		}
		in.close();
	}

	public static Map<String, Integer> getRequirementsMaps(String itemName) throws IOException {

		Map<String, Integer> map = new HashMap<String, Integer>();

		final String[] SKILL_NAMES = { "Attack", "Defence", "Strength", "Hitpoints", "Ranged", "Prayer", "Magic", "Cooking", "Woodcutting", "Fletching", "Fishing", "Firemaking", "Crafting", "Smithing", "Mining", "Herblore", "Agility", "Thieving", "Slayer", "Farming", "Runecraft" };

		URL url = new URL("http://2007.runescape.wikia.com/wiki/" + itemName.replace(' ', '_'));
		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;

		// <a href="/wiki/Attack" title="Attack">Attack</a>

		while ((line = in.readLine()) != null) {
			for (int i = 0; i < SKILL_NAMES.length; i++) {
				String skillName = SKILL_NAMES[i];
				String contain = "<a href=\"/wiki/" + skillName + "\" title=\"" + skillName + "\">" + skillName + "</a>";
				//System.out.println(contain);
				// while ((line = in.readLine()) != null) {
				if (line.contains(contain)) {
					//System.out.println(line);
					map.put(skillName.toUpperCase(), 1);
				}
			}
		}
		in.close();
		return map;
	}

	public static boolean getEquipable(String itemName) throws IOException {
		String line = "<th style=\"white-space: nowrap;\"><a href=\"/wiki/Equipment\" title=\"Equipment\">Equipable</a>?";
		return getBoolean(itemName, line);
	}

	public static boolean getBoolean(String itemName, String text) throws IOException {
		URL url = new URL("http://2007.runescape.wikia.com/wiki/" + itemName.replace(' ', '_'));
		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) {
			if (line.contains(text)) {
				// Go to next line.
				line = in.readLine();
				if (line.contains("Yes")) {
					return true;
				} else {
					return false;
				}
			}
		}
		in.close();
		return false;
	}

}