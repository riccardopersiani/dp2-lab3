package it.polito.dp2.NFFG.sol3.service.database;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NffgsDB {
	
	private static Map<String, NffgInfo> nffgMap = new ConcurrentHashMap<String, NffgInfo>();

	
	public static Map<String, NffgInfo> getNffgMap() {
		return nffgMap;
	}

	public static void addNewNffg(String nffgName, NffgInfo nffgInfo) {
		nffgMap.put(nffgName, nffgInfo);
	}

	public static void deleteNffg(String nffgName){
		nffgMap.remove(nffgName);
	}

	public static void deleteAll(){
		PoliciesDB.deleteAll();
		nffgMap.clear();
	}
	
	public static void printDB(){		
		Set<String> list = new HashSet<String>();
		list  = nffgMap.keySet();
		Iterator<String> iter = list.iterator();

		// Send the list of all nffgs
		while(iter.hasNext()) {
			String key = iter.next();
			nffgMap.get(key).printInfos();
		}
	}

}

