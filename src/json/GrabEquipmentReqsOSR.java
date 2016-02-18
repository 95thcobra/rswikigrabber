package json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
public class GrabEquipmentReqsOSR {
	public static boolean test = false;
	public static boolean firstIteration = false;

	public static void start() throws IOException {
		final String fileName = "equipmentrequirements.json";
		prepareFile(fileName);

		Gson builder = new GsonBuilder().setPrettyPrinting().create();
		JsonArray jsonArray = new JsonArray();

		BufferedReader in = new BufferedReader(new FileReader("itemlist78.txt"));
		String line;
		while ((line = in.readLine()) != null) {
			if (test && firstIteration) {
				break;
			}
			firstIteration = true;
			if (test) {
				line = "4153 : Granite maul : It's a Granite maul. : 50000";
			}

			if (!line.contains(" : ")) {
				continue;
			}
			int first = line.indexOf(" : ") + 3;
			int second = line.indexOf(" : ", first + 1);

			String idText = line.substring(0, first - 3);
			int id = Integer.parseInt(idText);
			String itemName = line.substring(first, second);

			if (!getEquipable(itemName)) {
				continue;
			}

			Map<String, Integer> requirementsMap = new HashMap<>();
			requirementsMap = getRequirementsItem(itemName);

			if (requirementsMap == null) {
				if (test)
					System.out.println("No reqs");
				continue;
			}

			// System.out.println(id + " - " + itemName);

			JsonArray requirements = new JsonArray();

			for (Map.Entry<String, Integer> entry : requirementsMap.entrySet()) {
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("level", entry.getValue());
				jsonObject.addProperty("skill", entry.getKey());
				requirements.add(jsonObject);
			}

			try (FileWriter writer = new FileWriter(fileName)) {
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("id", id);
				// jsonObject.addProperty("name", itemName);
				jsonObject.add("requirements", requirements);
				jsonArray.add(jsonObject);
				writer.write(builder.toJson(jsonArray));
				writer.close();
				System.out.println("id: " + id + ", itemName: " + itemName);
			} catch (IOException ex) {
			}
		}
		in.close();
		System.out.println("DUMPING DONE!");
	}

	public static int[] levels = { 10, 20, 30, 40, 50, 55, 60, 65, 70, 75, 80, 90, 99, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

	public static Map<String, Integer> getRequirementsItem(String itemName) {
		// System.out.println(itemName);
		try {
			Map<String, Integer> requirements = new HashMap<>();
			final String[] SKILL_NAMES = { "Attack", "Defence", "Strength", "Hitpoints", "Ranged", "Prayer", "Magic" };

			URL url = new URL("http://services.runescape.com/m=rswiki/en/OSR_-_" + itemName.replace(' ', '_'));
			URLConnection con = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line;

			while ((line = in.readLine()) != null) {

				String[] errors = { "This article is a stub. You can help out by", "does not currently exist.  To create it please follow the link", "Editing requires logging in." };

				for (String error : errors) {
					if (line.contains(error)) {
						// System.out.println("Looking for alternative...");
						return getRequirementsItemAlternative(itemName);
					}
				}

				if (!line.contains("<th>Weight</th>")) {
					continue;
				}

				for (int i = 0; i < 4; i++)
					line = in.readLine();

				// System.out.println(line);

				for (int i = 0; i < SKILL_NAMES.length; i++) {
					String skillName = SKILL_NAMES[i];
					String line1 = "<a href=\"/m=rswiki/en/OSR_-_Attack\"".replace("Attack", skillName);
					// System.out.println(line);
					if (line.contains(line1)) {
						String levelString = line.substring(line.indexOf(line1) - 3, line.indexOf(line1)).replace(">", "").replace("\"", "").trim();
						System.out.println(skillName + ": " + levelString + " (OSR)");
						requirements.put(skillName.toUpperCase(), Integer.parseInt(levelString));
					}
				}
			}
			in.close();
			if (requirements.size() <= 0) {
				return null;
			}
			return requirements;
		} catch (FileNotFoundException fnfe) {
			// System.out.println("Looking for alternative...");
			return getRequirementsItemAlternative(itemName);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Map<String, Integer> getRequirementsItemAlternative(String itemName) {
		try {
			Map<String, Integer> requirements = new HashMap<>();
			final String[] SKILL_NAMES = { "Attack", "Defence", "Strength", "Hitpoints", "Ranged", "Prayer", "Magic" };

			URL url = new URL("http://2007.runescape.wikia.com/wiki/" + itemName.replace(' ', '_'));
			URLConnection con = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line;

			while ((line = in.readLine()) != null) {
				// System.out.println(line);
				for (int i = 0; i < SKILL_NAMES.length; i++) {
					String skillName = SKILL_NAMES[i];
					String level = null;

					if (line.contains("require") && line.contains("to wield")) {
						String result = line.substring(line.indexOf("require"), line.indexOf("to wield"));
						// System.out.println(result);
						if (result.contains(skillName)) {
							level = result;
							// System.out.println("LEVEL:" + level);
						}

					} else if (line.contains("require") && line.contains("to wear")) {
						String result = line.substring(line.indexOf("require"), line.indexOf("to wear"));
						// System.out.println(result);
						if (result.contains(skillName)) {
							level = result;
							// System.out.println("LEVEL:" + level);
						}
					} else if (line.contains("require") && line.contains("to equip")) {
						String result = line.substring(line.indexOf("require"), line.indexOf("to equip"));
						// System.out.println(result);
						if (result.contains(skillName)) {
							level = result;
							// System.out.println("LEVEL:" + level);
						}
					}

					if (level != null) {
						int reallevel = 9999999;
						try {
							reallevel = Integer.parseInt(level);
						} catch (Exception e) {
							for (int levell : levels) {
								if (level.contains(Integer.toString(levell))) {
									reallevel = levell;
									break;
								}
							}
						}

						if (reallevel > 99 || reallevel <= 1) {
							continue;
						}

						if (!requirements.containsKey(skillName.toUpperCase())) {
							System.out.println(skillName + ": " + reallevel + " (RSWiki)");
							requirements.put(skillName.toUpperCase(), reallevel);
						}
					}
				}
			}
			in.close();
			if (requirements.size() <= 0) {
				return null;
			}
			return requirements;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static boolean getEquipable(String itemName) {
		try {
			String line = "<th style=\"white-space: nowrap;\"><a href=\"/wiki/Equipment\" title=\"Equipment\">Equipable</a>?";
			return getBoolean(itemName, line);
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean getBoolean(String itemName, String text) throws Exception {
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

	public static void prepareFile(String fileName) {
		Path path = Paths.get(fileName);
		File file = path.toFile();
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}