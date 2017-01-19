package it.polito.dp2.NFFG.sol3.service.database;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NffgsDB {
	// the key is the NffgName
	private static Map<String, NffgInfo> nffgMap = new ConcurrentHashMap<String, NffgInfo>();

	// Returns the Map that contains <NffgName, NffgInfo>
	public static Map<String, NffgInfo> getNffgMap() {
		return nffgMap;
	}


	// Add an element to the map <NffgName, NffgInfo>
	public static void addNewNffg(String nffgName, NffgInfo nffgInfo) {
		nffgMap.put(nffgName, nffgInfo);
	}


	// Delete an nffg element from the map
	public static void deleteNffg(String nffgName){
		nffgMap.remove(nffgName);
	}


	// Delete an nffg element from the map
	public static void deleteAll(){
		PoliciesDB.deleteAll();
		nffgMap.clear();
	}
	
	public static void printDB(){
		System.out.println("Printing NffgsDB...");
		
		Set<String> list = new HashSet<String>();
		list  = nffgMap.keySet();
		Iterator<String> iter = list.iterator();


		/** Send the list of all nffgs **/
		while(iter.hasNext()) {
			String key = iter.next();
			nffgMap.get(key).printInfos();
		}
	}

}

