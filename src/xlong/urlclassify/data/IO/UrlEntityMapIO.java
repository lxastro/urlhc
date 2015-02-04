package xlong.urlclassify.data.IO;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeSet;

import xlong.urlclassify.data.Entity;
import xlong.util.MyWriter;

public class UrlEntityMapIO {

	public static void writeOverlapUrl (HashMap<String, TreeSet<Entity>> urlEntityMap, String fileName){
		MyWriter.setFile(fileName, false);
		for (Entry<String, TreeSet<Entity>> en:urlEntityMap.entrySet()){
			if (en.getValue().size() >= 2) {
				MyWriter.writeln(en.getKey());
				for(Entity entity:en.getValue()) {
					MyWriter.write(entity.getName() + ":");
					for (String type:entity.getTypes()) {
						MyWriter.write(" " + type);
					}
					MyWriter.writeln("");
				}
				MyWriter.writeln("");
			}
		}
		MyWriter.close();
	}
}
