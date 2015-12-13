import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

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
			
			// If the item is not found, go to next iteration.
			try {
				examine = getExamineByName(itemName);
				weight = getWeightByName(itemName);
				stackable = getStackable(itemName);
				equipable = getEquipable(itemName);
			} catch (IOException e) {
				continue;
			}

			System.out.println("id: " + id);
			System.out.println("name: " + itemName);
			System.out.println("examine: " + examine);
			System.out.println("weight: " + weight);
			System.out.println("stackable: " + stackable);
			System.out.println("equipable: " + equipable);
		}
		in.close();
	}

	/**
	 * Grabs the examine by name.
	 * 
	 * @param itemName
	 * @return
	 * @throws IOException 
	 */
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

	/**
	 * Grabs the weight by name.
	 * 
	 * @param itemName
	 * @return
	 * @throws IOException 
	 */
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
		URL url = new URL("http://2007.runescape.wikia.com/wiki/" + itemName.replace(' ', '_'));
		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) {
			String contains1 = "<th style=\"white-space: nowrap;\"><a href=\"/wiki/Stackable_items\" title=\"Stackable items\">Stackable</a>?";

			if (line.contains(contains1)) {
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
	
	public static boolean getEquipable(String itemName) throws IOException {
		URL url = new URL("http://2007.runescape.wikia.com/wiki/" + itemName.replace(' ', '_'));
		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) {
			String contains1 = "<th style=\"white-space: nowrap;\"><a href=\"/wiki/Equipment\" title=\"Equipment\">Equipable</a>?";

			if (line.contains(contains1)) {
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