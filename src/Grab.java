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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Grabs item information from RS Wiki and puts them into a json file.
 * 
 * @author Sky
 */
public class Grab {

	/**
	 * Read id and name from txt file.
	 * 
	 * @return
	 * @throws IOException
	 */
	public static void start() throws IOException {

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

			// Gets the examine.
			String id = line.substring(0, first - 3);
			String itemName = line.substring(first, second);

			String examine = null;
			double weight = 0;
			boolean stackable = false;
			boolean equipable = false;

			double highAlch;
			double lowAlch;
			double storePrice;
			String destroy;

			// If the item is not found, go to next iteration.
			try {
				examine = getExamineByName(itemName);
				weight = getWeightByName(itemName);
				stackable = getStackable(itemName);
				equipable = getEquipable(itemName);

				highAlch = getHighAlchValue(itemName);
				lowAlch = getLowAlchValue(itemName);
				storePrice = getStorePrice(itemName);
				destroy = getDestroy(itemName);

			} catch (IOException e) {
				continue;
			}

			try (FileWriter writer = new FileWriter(file)) {
				JsonObject jsonObject = new JsonObject();

				jsonObject.addProperty("id", id);
				jsonObject.addProperty("name", itemName);
				jsonObject.addProperty("examine", examine);
				jsonObject.addProperty("weight", weight);
				jsonObject.addProperty("stackable", stackable);
				jsonObject.addProperty("equipable", equipable);

				jsonObject.addProperty("high-alch", highAlch);
				jsonObject.addProperty("low-alch", lowAlch);
				jsonObject.addProperty("store-price", storePrice);
				jsonObject.addProperty("destroy", destroy);

				System.out.println("id: " + id);
				System.out.println("name: " + itemName);

				jsonArray.add(jsonObject);

				writer.write(builder.toJson(jsonArray));
			} catch (IOException ex) {

			}
		}
		in.close();
	}

	public static double getHighAlchValue(String itemName) throws IOException {
		String line = "<th style=\"white-space: nowrap;\"><a href=\"/wiki/High_Level_Alchemy\" title=\"High Level Alchemy\">High Alch</a>";
		return getNumber(itemName, line);
	}

	public static double getLowAlchValue(String itemName) throws IOException {
		String line = "<th style=\"white-space: nowrap;\"><a href=\"/wiki/Low_Level_Alchemy\" title=\"Low Level Alchemy\">Low Alch</a>";
		return getNumber(itemName, line);
	}

	public static double getStorePrice(String itemName) throws IOException {
		String line = "<th style=\"white-space: nowrap;\"><a href=\"/wiki/High_Level_Alchemy\" title=\"High Level Alchemy\">High Alch</a>";
		return getNumber(itemName, line);
	}

	public static String getDestroy(String itemName) throws IOException {
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

	public static String getExamineByName(String itemName) throws IOException {
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
		return null;
	}

	public static double getWeightByName(String itemName) throws IOException {
		URL url = new URL("http://2007.runescape.wikia.com/wiki/" + itemName.replace(' ', '_'));
		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) {
			String contains1 = "</th><td> ";
			String contains2 = "&#160;kg";

			if (line.contains(contains1) && line.contains(contains2)) {
				int beginIndex = contains1.length();
				int endIndex = line.indexOf("&#");
				return Double.parseDouble(line.substring(beginIndex, endIndex));
			}
		}
		in.close();
		return 0;
	}

	public static boolean getStackable(String itemName) throws IOException {
		String line = "<th style=\"white-space: nowrap;\"><a href=\"/wiki/Stackable_items\" title=\"Stackable items\">Stackable</a>?";
		return getBoolean(itemName, line);
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

	public static double getNumber(String itemName, String text) throws IOException {
		URL url = new URL("http://2007.runescape.wikia.com/wiki/" + itemName.replace(' ', '_'));
		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) {
			String contains1 = text;
			if (line.contains(contains1)) {
				if (!line.contains("&")) {
					return 0;
				}
				// Go to next line.
				line = in.readLine();
				int beginIndex = line.indexOf(' ') + 1;
				int endIndex = line.indexOf('&');
				String value = line.substring(beginIndex, endIndex);
				value = value.replace(',', '.');
				return Double.parseDouble(value);
			}
		}
		in.close();
		return 0;
	}
}