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
import java.util.List;

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

			String id = line.substring(0, first - 3);
			String itemName = line.substring(first, second);
			
			/*try (FileWriter writer = new FileWriter(file)) {
				JsonObject jsonObject = new JsonObject();

				jsonObject.addProperty("id", id);
				jsonObject.addProperty("name", itemName);
				//jsonObject.add("requirements", builder.toJsonTree(getRequirements(itemName)));
				jsonObject.add("gewa", lol);

				jsonArray.add(jsonObject);

				writer.write(builder.toJson(jsonArray));
				System.out.println("id: " + id + ", itemName: " + itemName);
			} catch (IOException ex) {
				System.out.println("?");
			}*/
			
			try (FileWriter writer = new FileWriter(file)) {
				//Gson builder = new GsonBuilder().setPrettyPrinting().create();
				//JsonArray jsonArray = new JsonArray();
				JsonObject jsonObject = new JsonObject();

				jsonObject.addProperty("id", id);
				jsonObject.addProperty("name", itemName);
				
				
				
				JsonArray requirements = new JsonArray();	

				try {
					for (JsonObject jso : getRequirements(itemName)) {
						requirements.add(jso);
					}
				//requirements.add(getRequirements(itemName).get(0));
				} catch(Exception e) {
					continue;
				}
				
				/*
				List<JsonObject> test = getRequirements(itemName);
				if (test.isEmpty()) {
					continue;
				}
				for (JsonObject json : test) {
					requirements.add(json);
				}*/
				
				
				jsonObject.add("requirements", requirements);
				
				
				jsonObject.add("test", requirements);
				
				jsonArray.add(jsonObject);
				
				System.out.println("id: " + id + ", itemName: " + itemName);
				
				writer.write(builder.toJson(jsonArray));
				
				writer.close();
			} catch (IOException ex) {
				// logger.log(Level.SEVERE, "An error occured while trying to save a
				// player file.");
			}
		}
		in.close();
	}

	/**
	 * REQUIREMENTS
	 */
	public static List<JsonObject> getRequirements(String itemName) throws IOException {
		final String[] SKILL_NAMES = { "Attack", "Defence", "Strength", "Hitpoints", "Ranged", "Prayer", "Magic", "Cooking", "Woodcutting", "Fletching", "Fishing", "Firemaking", "Crafting", "Smithing", "Mining", "Herblore", "Agility", "Thieving", "Slayer", "Farming", "Runecraft" };
		List<JsonObject> jsonList = new ArrayList<>();
		//List<Integer> jsonList2 = new ArrayList<>();
		
		URL url = new URL("http://2007.runescape.wikia.com/wiki/" + itemName.replace(' ', '_'));
		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) {
			for (int i = 0; i < SKILL_NAMES.length; i++) {
				String skillName = SKILL_NAMES[i];
				String skill = "<a href=\"/wiki/" + skillName + "\" title=\"" + skillName + "\">" + skillName + "</a>";
				if (line.contains(skill)) {
					JsonObject object = new JsonObject();
					object.addProperty("level", 22);
					object.addProperty("skill", skillName.toUpperCase());
					jsonList.add(object);
					//jsonList2.add(2);
				}
			}
		}
		in.close();
		return jsonList;
		//return jsonList2;
	}
}