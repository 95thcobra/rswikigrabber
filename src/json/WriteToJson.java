package json;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class WriteToJson {
	
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
			jsonObject.add("type", builder.toJsonTree(EquipmentType.AMULET_SLOT));
		//	player.setRights(builder.fromJson(reader.get("rights"), Rights.class));
			jsonArray.add(jsonObject);
			
			writer.write(builder.toJson(jsonArray));
			
			writer.close();
		} catch (IOException ex) {
			// logger.log(Level.SEVERE, "An error occured while trying to save a
			// player file.");
		}
		return true;
	}
}
