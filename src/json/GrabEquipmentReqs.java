package json;

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
	public static boolean test = true;
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
				line = "3755 : Farseer helm : It's a Farseer helm. : 60000";
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

					String text1 = "requires ";

					String text2 = "<a href=\"/wiki/" + skillName + "\" title=\"" + skillName + "\">" + skillName + "</a> to wear";
					String text3 = "<a href=\"/wiki/Attack\" title=\"Attack\">Attack</a> to wield".replace("Attack", skillName);

					String text4 = "level ";
					String text5 = " <a href=\"/wiki/Ranged\" title=\"Ranged\">Ranged</a>".replace("Ranged", skillName);

					String text6 = "at least level ";

					String text7 = skillName;

					String text8 = "Requiring ";

					String text9 = "<a href=\"/wiki/Attack\" title=\"Attack\">Attack</a> level of".replace("Attack", skillName);

					String text10 = "requires ";
					String text11 = "<a href=\"/wiki/Attack\" title=\"Attack\">Attack</a> and".replace("Attack", skillName);
					String text12 = skillName + " and";
					String text13 = "and 30 " + skillName + " to wield";

					String text14 = "require ";

					String text15 = "<a href=\"/wiki/Defence\" title=\"Defence\">Defence</a> to wear".replace("Defence", skillName);

					String text16 = "at least ";
					String text17 = " to wear it";

					String text18 = "and ";
					String text19 = "<a href=\"/wiki/Prayer\" title=\"Prayer\">Prayer</a> to wield".replace("Prayer", skillName);

					String text20 = "require";
					String text21 = "<a href=\"/wiki/Defence\" title=\"Defence\">Defence</a>".replace("Defence", skillName);
					int choice = -1;

					String level = null;
					// System.out.println(line);

					if (line.contains("75 Defence, 70 Prayer and 65 Magic to wield")) {
						System.out.println("LMEFGAWGEEW");
						if (skillName.equalsIgnoreCase("Defence")) {
							level = 75 + "";
						}
						if (skillName.equalsIgnoreCase("Prayer")) {
							level = 70 + "";
						}
						if (skillName.equalsIgnoreCase("Magic")) {
							level = 65 + "";
						}
						if (skillName.equalsIgnoreCase("Defence") || skillName.equalsIgnoreCase("Prayer") || skillName.equalsIgnoreCase("Magic")) {
							requirements.put(skillName.toUpperCase(), Integer.parseInt(level));
							continue;
						}

					} else if (line.contains(text10) && line.contains(text11)) {
						String result = line.substring(line.indexOf(text10), line.indexOf(text10) + 20);
						// System.out.println(result);
						level = result.substring(result.indexOf("requires ") + "requires ".length(), result.indexOf("<"));
						System.out.println("LEVEL:" + level);
						choice = 100;

					} else if (line.contains(text16) && line.contains(text17)) {
						String result = line.substring(line.indexOf(text16), line.indexOf(text17));
						System.out.println(result);
						if (result.contains(skillName)) {
							// System.out.println(result);
							level = result.substring(result.indexOf("at least"), result.indexOf("required"));
							System.out.println("LEVEL:" + level);
							choice = 100;
						} else if (line.contains(skillName)) {
							// System.out.println(result);
							level = result.substring(result.indexOf("at least"), result.indexOf("required"));
							System.out.println("LEVEL:" + level);
							choice = 100;
						}

					} else if (line.contains(text10) && line.contains(text12)) {
						String result = line.substring(line.indexOf(text10), line.indexOf(text10) + 20);
						System.out.println("result.." + result);
						level = result.substring(result.indexOf("requires "));
						System.out.println("LEVEL:" + level);
						choice = 100;
					}

					else if (line.contains(text10) && line.contains(text13)) {
						String result = line.substring(line.indexOf(text13), line.indexOf(text13) + 20);
						// System.out.println("result: " + result);
						level = result.substring(result.indexOf("and ") + "and ".length(), result.indexOf("to wi"));
						System.out.println("LEVEL:" + level);
						choice = 100;
					}

					else if (line.contains(text1) && line.contains(text2)) {
						String result = line.substring(line.indexOf("requires"), line.indexOf("to wear"));
						// System.out.println(result);
						level = result.substring(result.indexOf("requires ") + "requires ".length(), result.indexOf("<"));
						System.out.println("LEVEL:" + level);
						choice = 1;
					}

					else if (line.contains("requires") && line.contains("to wear")) {
						String result = line.substring(line.indexOf("requires"), line.indexOf("to wear"));
						// System.out.println(result +
						// result.indexOf("requires") + "-" + result.indexOf("to
						// wear"));
						// level = result.substring(result.indexOf("requires"),
						// result.indexOf("to wear"));
						System.out.println(result);
						if (result.contains(skillName)) {
							level = result;
							System.out.println("LEVEL:" + level);
							choice = 1555;
						}
					}

					else if (line.contains(text1) && line.contains(text15)) {
						String result = line.substring(line.indexOf("requires"), line.indexOf("to wear"));
						// System.out.println(result);
						level = result.substring(result.indexOf("requires ") + "requires ".length(), result.indexOf("<"));
						System.out.println("LEVEL:" + level);
						choice = 1;

					} else if (line.contains(text1) && line.contains(text3)) {
						String result = line.substring(line.indexOf("requires"), line.indexOf("to wield"));
						level = result.substring(result.indexOf("requires ") + "requires ".length(), result.indexOf("<"));
						System.out.println("LEVEL:" + level);
						choice = 2;
					}

					///////////////
					else if (line.contains("require") && line.contains("to wield")) {
						String result = line.substring(line.indexOf("require"), line.indexOf("to wield"));
						System.out.println(result);
						if (result.contains(skillName)) {
							level = result;
							System.out.println("LEVEL:" + level);
							choice = 2;
						}

					} else if (line.contains("require") && line.contains("to wear")) {
						String result = line.substring(line.indexOf("require"), line.indexOf("to wear"));
						System.out.println(result);
						if (result.contains(skillName)) {
							level = result;
							System.out.println("LEVEL:" + level);
							choice = 2;
						}
					} else if (line.contains("require") && line.contains("to equip")) {
						String result = line.substring(line.indexOf("require"), line.indexOf("to equip"));
						System.out.println(result);
						if (result.contains(skillName)) {
							level = result;
							System.out.println("LEVEL:" + level);
							choice = 2;
						}
					} //////////////////

					else if (line.contains(text20) && line.contains(text21)) {
						String result = line.substring(line.indexOf(text20), line.indexOf(text21));
						System.out.println(result);
						if (result.contains(skillName)) {
							level = result;
							System.out.println("LEVEL:" + level);
							choice = 2;
						}
					}

					else if (line.contains(text18) && line.contains(text19)) {
						String result = line.substring(line.indexOf(text18), line.indexOf(text19));
						System.out.println(result);
						if (result.contains(skillName)) {
							level = result;
							System.out.println("LEVEL:" + level);
							choice = 2;
						}
					}

					else if (line.contains(text14) && line.contains(text3)) {
						String result = line.substring(line.indexOf("require"), line.indexOf("to wield"));
						level = result.substring(result.indexOf("require ") + "require ".length(), result.indexOf("<"));
						System.out.println("LEVEL:" + level);
						choice = 72;
					}

					else if (line.contains(text1) && line.contains(text9)) {
						String result = line.substring(line.indexOf("requires"), line.indexOf("to wield"));
						System.out.println(result);
						level = result.substring(result.indexOf("requires ") + "requires ".length());
						System.out.println("LEVEL:" + level);
						choice = 6;
					}

					else if (line.contains(text8) && line.contains(text3)) {
						String result = line.substring(line.indexOf("equiring"), line.indexOf("to wield"));
						level = result.substring(result.indexOf("equiring ") + "equiring ".length(), result.indexOf("<"));
						System.out.println("LEVEL:" + level);
						choice = 3;
					}

					else if (line.contains(text1) && line.contains(text4) && line.contains(text5)) {
						String result = line.substring(line.indexOf("requires"), line.indexOf("requires") + 10);
						level = result.substring(result.indexOf("requires ") + "requires ".length(), result.indexOf("<"));
						System.out.println("LEVEL:" + level);
						choice = 4;
					}

					else if (line.contains(text6) && line.contains(text7)) {
						String result = line.substring(line.indexOf(text6), line.indexOf(text6) + 30);
						System.out.println(result);
						level = result.substring(result.indexOf(text6) + text6.length(), result.indexOf(text6) + text6.length() + 2);
						System.out.println("LEVEL:" + level);
						choice = 5;
						if (!result.contains(skillName)) {
							level = null;
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

						if (reallevel > 99) {
							System.out.println("KANKERRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
						}

						System.out.println(skillName + " --- " + itemName + " --- choice:" + choice + " --- REALLLLLLEVEL:" + reallevel);

						requirements.put(skillName.toUpperCase(), reallevel);
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