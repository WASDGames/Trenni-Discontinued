package me.darkwiiplayer.Trenni;

/* Documentation:
 * addList() Adds the CoinType to the list and returnes true if another coin was overwritten and false otherwise.
 * removeList() Tries to remove the coin type from the list and returnes true it it was removed and false if it wasn't in the list in the first place.
 * loadMap(HashMap) Removes the coin frmo the list, tries to load the data from the given hashmap and adds it to the list if successfull.
 * toMap() creates a HashMap that represents the coin
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Material;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class CoinType {
	private static HashMap<String, CoinType> coinMap; // = new HashMap<String, CoinType>(); //Where all the other coins are saved
	
	private String name;
	private UUID ownerID;
	private ArrayList<String> licenses;
	private Material material;
	private Double amount;

	public CoinType() {
		resetValues();
	}
	
	public CoinType(HashMap map) {
		loadMap(map);
	}
	
	//========== End of Constructors =======================
	
	private void resetValues() {
		ownerID = null;
		name = "";
		licenses = new ArrayList();
		material = Material.AIR;
		amount = 1.0;
	}
	
	private boolean isCoinMap(HashMap map) {
		if (! (map.get("name") instanceof String)) { return false; }
		if (! (map.get("material") instanceof String)) { return false; }
		if (! ((map.get("amount") instanceof Double) | (map.get("amount") instanceof Float))) { return false; }
		if (! (map.get("ownerID") instanceof String)) { return false; }
		if (UUID.fromString((String)map.get("ownerID")) == null) { return false; }
		ArrayList<Object> _licenses;
		if (map.get("licenses") instanceof ArrayList) {
			_licenses = (ArrayList)map.get("licenses");
			for (Object i : _licenses) {
				if (! ((i instanceof String) & (UUID.fromString((String) i) != null))) { return false; }
			}
		} else { return false; }
		
		return true;
	}
	
	public Boolean removeList() { //Returns True if the coin was removed and false if the coin wasn't in the list
		if (coinMap.remove(name) != null) {
			return false;
		}
		return true;
	}
	
	public Boolean addList() { //Returns true if a coin was overwritten
		if (coinMap.put(name, this) == null) {
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean loadMap(HashMap<Object, Object> map) {
		removeList();
		
		//check all the values for correct types:
		if (isCoinMap(map)) {
			name = (String)map.get("name");
			material = Material.getMaterial((String)map.get("material"));
			amount = (Double)map.get("amount");
			ownerID = UUID.fromString((String)map.get("ownerID"));
			licenses = (ArrayList<String>)map.get("licenses");
		} else {
			resetValues();
			return false;
		}
		
		addList();
		return true;
	}
	
	public HashMap<String, Object> toMap() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		map.put("name", name);
		map.put("material", material.name());
		map.put("amount", amount);
		map.put("ownerID", ownerID.toString());
		map.put("licenses", licenses);
		
		return map;
	}
	
	public static boolean reload() {
		coinMap = new HashMap<String, CoinType>();
		File file = new File("placeholder.yml"); //TODO: Implement dynamic file path
		FileInputStream FStream;
		DumperOptions options = new DumperOptions();
	    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		Yaml yaml = new Yaml(options);
		
		try {
			FStream  = new FileInputStream(file);
			for (Object data : yaml.loadAll(FStream)) {
				new CoinType((HashMap)data);
			}
			FStream.close();
			return true;
		} catch (FileNotFoundException e) {
			TrenniPlugin.getInstance().logger.log(Level.SEVERE, "Could not load coin file - File not found!");
			return false;
		} catch (IOException e) {
			TrenniPlugin.getInstance().logger.log(Level.SEVERE, "Error loading coins: " + e.getMessage());
			return false;
		}
	}

	public static boolean dump() {
		FileWriter FWriter;
		DumperOptions options = new DumperOptions();
	    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		Yaml yaml = new Yaml(options);
		File file = new File("placeholder.yml"); //TODO: Implement dynamic file path
		
		ArrayList<HashMap> mapList = new ArrayList<HashMap>();
		for (CoinType coin : coinMap.values()) {
			mapList.add(coin.toMap());
		}
		
		try {
			FWriter = new FileWriter(file);
			
			yaml.dumpAll(mapList.iterator(), FWriter);
			
			FWriter.close();
		} catch (IOException e) {
			TrenniPlugin.getInstance().logger.log(Level.SEVERE, "Error loading coins: " + e.getMessage());
			return false;
		}
		
		return false;
	}
}