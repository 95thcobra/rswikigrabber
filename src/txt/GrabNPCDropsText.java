package txt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
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
public class GrabNPCDropsText {
	public static boolean firstLine = false;
	
	/**
	 * Read id and name from txt file.
	 * 
	 * @return
	 * @throws Exception
	 */
	public static void start() throws Exception {

		Path path = Paths.get("npcdrops.txt");
		File file = path.toFile();
		if (!file.exists()) {
			file.createNewFile();
		}
		System.out.println("dumping started");
		BufferedReader in = new BufferedReader(new FileReader("npcshitten.txt"));

		try (FileWriter writer = new FileWriter(file)) {
		}

		String line;
		while ((line = in.readLine()) != null) {
			if (!firstLine) {
				firstLine = true;
				continue;
			}
			if (!line.contains(" = ")) {
				continue;
			}

			String args[] = line.split("=");
			int id = Integer.parseInt(args[0].trim());
			String name = args[1].trim();

			String drops = "";
			// System.out.println(name);
			try {
				drops = getDrops(name);
				if (drops.length() <= 0)
					continue;
			} catch(FileNotFoundException fnx) {
				continue;
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

			System.out.print("id:" + id);
			System.out.println(", name:" + name);

			try (FileWriter writer = new FileWriter(file, true)) {
				writer.write("#" + id + " " + name + "\n");
				writer.write(drops + "\n");
			}
		}
		in.close();
		System.out.println("DONE!");
	}

	public static String getDrops(String name) throws IOException {
		URL url = new URL("http://runescape.wikia.com/wiki/" + name.replace(' ', '_'));
		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;

		String drops = "";

		String search = "<tr style=\"text-align:center;\"><td><a href=\"/<tr style=\"text-align:center;\"><td><a href=\"/";
		String rarity = "class=\"rarity-";
		String rarityEnd = "\">";

		while ((line = in.readLine()) != null) {
			if (line.contains(rarity)) {
				
				//// works
				int indexFirst = line.indexOf(rarity) + rarity.length();
				String rarityString = line.substring(indexFirst, indexFirst + 30);
				int indexEnd = rarityString.indexOf(rarityEnd);
				String rarityPrint = line.substring(indexFirst, indexFirst+indexEnd).toUpperCase();
				////
				
				//// works
				String title = "title=\"";
				String titleEnd = "\"";
				int titleStart = line.indexOf(title) + title.length();
				String src = line.substring(titleStart, titleStart + 50);
				int titleEndd = src.indexOf(titleEnd) + title.length() - 1;
				String titlePrint = line.substring(titleStart, titleStart + titleEndd).trim().replace("\"", "");
				//System.out.println(titlePrint);
				////
				
				String quantity;
				try {
				//
				String qty = titlePrint + "</a></td><td>";
				String qtyEnd = "/td><td";
				int qtyIndexStart = line.indexOf(qty) + qty.length();
				String lmao = line.substring(qtyIndexStart, qtyIndexStart + 50);
				int qtyEndd= qtyIndexStart + lmao.indexOf(qtyEnd) - 1;
				
				 quantity = line.substring(qtyIndexStart, qtyEndd);
				} catch(Exception e) {
					quantity = ""+1;
				}
				//
				if (quantity.contains("-")) {
					quantity = quantity.substring(0, quantity.indexOf("-") - 1);
				}
				if (quantity.contains("–")) {
					quantity = quantity.substring(0, quantity.indexOf("–"));
				}
				if (quantity.contains(";")) {
					quantity = quantity.substring(0, quantity.indexOf(";"));
				}
				System.out.println(quantity);
				
				drops += titlePrint + "$" + quantity + "$" + rarityPrint + " ";
			}
		}
		in.close();
		return drops;
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