import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
public class GrabItemDefs {

	/**
	 * Read id and name from txt file.
	 * 
	 * @return
	 * @throws Exception
	 */
	public static void start() throws Exception {

		Path path = Paths.get("myitemdef.json");
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
			String description = "";
			double weight = 0;
			boolean tradable = false;
			boolean stackable = false;
			boolean notable = false;
			boolean equipable = false;
			boolean isNote = false;
			boolean twoHanded = false;
			double highAlch = 0;
			double lowAlch = 0;
			double storePrice = 0;
			String equipmentType = "NONE";
			boolean plateBody = false;
			boolean fullHelm = false;
			int[] bonus = new int[14];

			try {
				plateBody = itemName.contains("platebody");
				fullHelm = itemName.contains("fullhelm");
				isNote = isNote(idText);
				final String NOTE_DESCRIPTION = "Swap this note at any bank for the equivalent item.";
				description = (isNote ? NOTE_DESCRIPTION : getDescByName(itemName));
				weight = getWeightByName(itemName);
				tradable = isTradable(itemName);
				stackable = getStackable(itemName);
				notable = isNotable(stackable, tradable);
				equipable = getEquipable(itemName);
				equipmentType = getEquipmentType(itemName);
				twoHanded = getTwoHanded(itemName);
				highAlch = getHighAlchValue(itemName);
				lowAlch = getLowAlchValue(itemName);
				storePrice = getStorePrice(itemName);
				bonus = getBonus(itemName);

			} catch (Exception e) {
				// Failed to grab info for item.
			}

			try (FileWriter writer = new FileWriter(file)) {
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("id", id);
				jsonObject.addProperty("name", itemName);
				jsonObject.addProperty("description", description);
				jsonObject.addProperty("weight", weight);
				jsonObject.addProperty("tradable", tradable);
				jsonObject.addProperty("stackable", stackable);
				jsonObject.addProperty("notable", notable);
				jsonObject.addProperty("noted", isNote);
				jsonObject.addProperty("equipable", equipable);
				jsonObject.addProperty("equipment-type", equipmentType);
				jsonObject.addProperty("two-handed", twoHanded);
				jsonObject.addProperty("platebody", plateBody);
				jsonObject.addProperty("fullhelm", fullHelm);
				jsonObject.addProperty("high-alch", highAlch);
				jsonObject.addProperty("low-alch", lowAlch);
				jsonObject.addProperty("store-price", storePrice);
				jsonObject.add("bonus", builder.toJsonTree(bonus));
				System.out.println("id: " + id + ", itemName: " + itemName);
				jsonArray.add(jsonObject);
				writer.write(builder.toJson(jsonArray));
			} catch (Exception ex) {
				// Failed to write to JSON.
			}
		}
		in.close();
	}

	public static boolean getTwoHanded(String itemName) throws Exception {
		boolean twoh = false;
		URL url = new URL("http://2007.runescape.wikia.com/wiki/" + itemName.replace(' ', '_'));
		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) {
			if (line.contains("two-handed")) {
				twoh = true;
				break;
			}
		}
		in.close();
		return twoh;
	}

	public static boolean isNote(String id) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader("isnotedump.txt"));
		Boolean bool = false;
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] array = line.split("\\$");
				String id2 = array[0].substring(line.indexOf("id") + 2);
				if (id.equals(id2)) {
					bool = Boolean.parseBoolean(array[2]);
					break;
				}
			}
		} finally {
			reader.close();
		}

		return bool;
	}

	public static boolean isNotable(boolean stackable, boolean tradable) {
		return !stackable && tradable;
	}

	/**
	 * Do requirements in another file.
	 */
	/*public static JsonObject[] getRequirements(String itemName) throws Exception {
		final String[] SKILL_NAMES = { "Attack", "Defence", "Strength", "Hitpoints", "Ranged", "Prayer", "Magic", "Cooking", "Woodcutting", "Fletching", "Fishing", "Firemaking", "Crafting", "Smithing", "Mining", "Herblore", "Agility", "Thieving", "Slayer", "Farming", "Runecraft" };

		URL url = new URL("http://2007.runescape.wikia.com/wiki/" + itemName.replace(' ', '_'));
		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) {
			for (int i = 0; i < SKILL_NAMES.length; i++) {
				String skillName = SKILL_NAMES[i];
				String skill = "<a href=\"/wiki/" + skillName + "\" title=\"" + skillName + "\">" + skillName + "</a>";

				// do checking
			}

			String text = "<td style=\"text-align: center; width:";
			if (line.contains(text)) {
				int beginIndex = line.indexOf("\">") + 2;
				if (line.contains("+")) {
					line = line.replace("+", "");
				}
				if (line.contains("%")) {
					line = line.replace("%", "");
				}
			}
		}
		in.close();
		return null;
	}*/

	/**
	 * Rewrite to this, so you dont have to connect over and over.
	 * 
	 * @param itemName
	 * @return
	 * @throws Exception
	 */
	/*public static String readPage(String itemName) throws Exception {
		URL url = new URL("http://2007.runescape.wikia.com/wiki/" + itemName.replace(' ', '_'));
		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) {
			if (line.contains("weight")) {
				// handleweight;
			}
			if (line.contains("price")) {
				// handleprice;
			}
		}
		in.close();
		return null;
	}*/

	public static int[] getBonus(String itemName) throws Exception {
		int[] array = new int[14];
		int count = 0;

		URL url = new URL("http://2007.runescape.wikia.com/wiki/" + itemName.replace(' ', '_'));
		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) {
			String text = "<td style=\"text-align: center; width:";
			if (line.contains(text)) {
				int beginIndex = line.indexOf("\">") + 2;
				if (line.contains("+")) {
					line = line.replace("+", "");
				}
				if (line.contains("%")) {
					line = line.replace("%", "");
				}
				array[count] = Integer.parseInt(line.substring(beginIndex));
				count++;
			}
		}
		in.close();
		return array;
	}

	public static double getHighAlchValue(String itemName) throws Exception {
		String line = "<th style=\"white-space: nowrap;\"><a href=\"/wiki/High_Level_Alchemy\" title=\"High Level Alchemy\">High Alch</a>";
		return getNumber(itemName, line);
	}

	public static double getLowAlchValue(String itemName) throws Exception {
		String line = "<th style=\"white-space: nowrap;\"><a href=\"/wiki/Low_Level_Alchemy\" title=\"Low Level Alchemy\">Low Alch</a>";
		return getNumber(itemName, line);
	}

	public static double getStorePrice(String itemName) throws Exception {
		String line = "<th style=\"white-space: nowrap;\"><a href=\"/wiki/Prices#Store_Price\" title=\"Prices\">Store price</a>";
		return getNumber(itemName, line);
	}

	public static String getDestroy(String itemName) throws Exception {
		URL url = new URL("http://2007.runescape.wikia.com/wiki/" + itemName.replace(' ', '_'));
		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) {
			String contains1 = "<th style=\"white-space: nowrap;\"><a href=\"/wiki/Destroy\" title=\"Destroy\">Destroy</a>";

			if (line.contains(contains1)) {
				// Go to next line.
				line = in.readLine();
				int index = line.indexOf(' ') + 1;
				return line.substring(index).toUpperCase();
			}
		}
		in.close();
		return "DROP";
	}

	public static String getEquipmentType(String itemName) throws Exception {

		final String[][] SLOTS = { { "Weapon", "WEAPON" }, { "Head", "HAT" }, { "Neck", "AMULET" }, { "Feet", "BOOTS" }, { "Hands", "HANDS" }, { "Shield", "SHIELD" }, { "Ring", "RING" }, { "Ammunition", "ARROW" }, { "Legwear", "LEGS" }, { "Body", "BODY" } };

		URL url = new URL("http://2007.runescape.wikia.com/wiki/" + itemName.replace(' ', '_'));
		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) {
			for (int i = 0; i < SLOTS.length; i++) {
				String slot = SLOTS[i][0];
				String displaySlot = SLOTS[i][1];
				String text = "Category:" + slot + " slot items";
				if (line.contains(text)) {
					return displaySlot;
				}
			}
		}
		in.close();
		return "NONE";
	}

	public static String getDescByName(String itemName) throws Exception {
		URL url = new URL("http://2007.runescape.wikia.com/wiki/" + itemName.replace(' ', '_'));
		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) {
			String pre = "<td colspan=\"2\" style=\"padding:3px 7px 3px 7px; line-height:140%; text-align:center;\"> ";
			if (line.contains(pre)) {
				return line.substring(pre.length());
			}
		}
		in.close();
		return "";
	}

	public static double getWeightByName(String itemName) throws Exception {
		URL url = new URL("http://2007.runescape.wikia.com/wiki/" + itemName.replace(' ', '_'));
		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) {
			String text = "<th style=\"white-space: nowrap;\"><a href=\"/wiki/Weight\" title=\"Weight\">Weight</a>";
			// String text = "<th style=\"white-space: nowrap;\"><a
			// href=\"/wiki/Weight\" title=\"Weight\">Weight</a>";
			String contains1 = "</th><td> ";
			String contains2 = "&#160;kg";

			if (line.contains(text)) {

				line = in.readLine();
				if (line.contains("<b>Inventory:</b> ")) {
					String inventory = "<b>Inventory:</b> ";
					int beginIndex = line.indexOf(inventory) + inventory.length() + 1;
					int endIndex = line.indexOf("&#");
					return Double.parseDouble(line.substring(beginIndex, endIndex));
				}
				if (line.contains("</th><td> (empty) ")) {
					String inventory = "</th><td> (empty) ";
					int beginIndex = line.indexOf(inventory) + inventory.length() + 1;
					int endIndex = line.indexOf("&#");
					return Double.parseDouble(line.substring(beginIndex, endIndex));
				}
				// if (line.contains(contains1) && line.contains(contains2)) {
				int beginIndex = contains1.length();
				int endIndex = line.indexOf("&#");
				try {
					return Double.parseDouble(line.substring(beginIndex, endIndex));
				} catch (Exception e) {
					return 0;
				}
				// }

			}
		}
		in.close();
		return 0;
	}

	public static boolean getStackable(String itemName) throws Exception {
		String line = "<th style=\"white-space: nowrap;\"><a href=\"/wiki/Stackable_items\" title=\"Stackable items\">Stackable</a>?";
		return getBoolean(itemName, line);
	}

	public static boolean getEquipable(String itemName) throws Exception {
		String line = "<th style=\"white-space: nowrap;\"><a href=\"/wiki/Equipment\" title=\"Equipment\">Equipable</a>?";
		return getBoolean(itemName, line);
	}

	public static boolean isTradable(String itemName) throws Exception {
		String line = "<th style=\"white-space: nowrap;\"><a href=\"/wiki/Tradeable\" title=\"Tradeable\" class=\"mw-redirect\">Tradeable</a>?";
		return getBoolean(itemName, line);
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

	public static double getNumber(String itemName, String text) throws Exception {
		URL url = new URL("http://2007.runescape.wikia.com/wiki/" + itemName.replace(' ', '_'));
		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) {
			try {
				String contains1 = text;
				if (line.contains(contains1)) {
					// Go to next line.
					line = in.readLine();
					if (!line.contains("&")) {
						return 0;
					}
					if (line.contains("-")) {
						String value = line.substring(line.indexOf('-') - 1, line.indexOf('-'));
						return Double.parseDouble(value);
					}
					int beginIndex = line.indexOf(' ') + 1;
					int endIndex;
					if (line.contains("-")) {
						endIndex = line.indexOf('-');
					} else {
						endIndex = line.indexOf('&');
					}
					String value = line.substring(beginIndex, endIndex);
					try {
						value = value.replace(',', '.');
					} catch (Exception e) {
						return 0;
					}
					try {
						return Double.parseDouble(value);
					} catch (Exception e) {
						return 0;
					}
				}
			} catch (Exception e) {
				return 0;
			}
		}
		in.close();
		return 0;
	}
}