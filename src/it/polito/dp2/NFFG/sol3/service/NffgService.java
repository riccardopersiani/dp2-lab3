package it.polito.dp2.NFFG.sol3.service;

import java.util.Map;

import it.polito.dp2.NFFG.sol3.service.database.NffgsDB;

public class NffgService {
	
	Map <Long , NffgService> map = NffgsDB.getMap();

}
