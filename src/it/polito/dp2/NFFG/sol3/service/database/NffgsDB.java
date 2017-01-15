package it.polito.dp2.NFFG.sol3.service.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sun.xml.bind.v2.schemagen.xmlschema.List;

import it.polito.dp2.NFFG.sol3.service.NffgService;

public class NffgsDB {
	// this is a database class containing a static Map of nffg objects
	private static Map<Long,NffgService> map = new HashMap<Long,NffgService>();
	private static long last=0;

	public static Map<Long, NffgService> getMap() {
		return map;
	}

	public static void setMap(Map<Long, NffgService> map) {
		NffgsDB.map = map;
	}

	public static long getNext() {
		return ++last;
	}
	
	public List<NffgType> getAllNffgs() {
		ArrayList<NffgType> list = new ArrayList<NffgType>(); 
		
	}

}

