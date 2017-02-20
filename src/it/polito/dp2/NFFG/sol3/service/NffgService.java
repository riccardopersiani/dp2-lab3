package it.polito.dp2.NFFG.sol3.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import it.polito.dp2.NFFG.sol3.service.database.NffgInfo;
import it.polito.dp2.NFFG.sol3.service.database.NffgsDB;
import it.polito.dp2.NFFG.sol3.service.database.PoliciesDB;
import it.polito.dp2.NFFG.sol3.service.database.PolicyInfo;
import it.polito.dp2.NFFG.sol3.service.jaxb.LinkType;
import it.polito.dp2.NFFG.sol3.service.jaxb.NFFG;
import it.polito.dp2.NFFG.sol3.service.jaxb.Nffgs;
import it.polito.dp2.NFFG.sol3.service.jaxb.NodeType;
import it.polito.dp2.NFFG.sol3.service.jaxb.ReachabilityPolicyType;
import it.polito.dp2.NFFG.sol3.service.jaxb.TraversalPolicyType;
import it.polito.dp2.NFFG.sol3.service.neo4j.Labels;
import it.polito.dp2.NFFG.sol3.service.neo4j.Node;
import it.polito.dp2.NFFG.sol3.service.neo4j.ObjectFactory;
import it.polito.dp2.NFFG.sol3.service.neo4j.Property;
import it.polito.dp2.NFFG.sol3.service.neo4j.Relationship;

public class NffgService {

	/** Add the selected nffg on Neo4J and add the selected nffg and its relative policies in the cache 
	 * @return 
	 * @throws Exception **/
	public NFFG loadOneNffgOnNeo4J(NFFG nffg) throws Exception{
		Map<String, String> nodesMap = new HashMap<String, String>(); 
		Map<String, String> linksMap = new HashMap<String, String>(); 
		Map<String, String> belongsMap = new HashMap<String, String>(); 

		List<NodeType> nodes = new ArrayList<NodeType>();
		List<LinkType> links = new ArrayList<LinkType>();

		if(NffgsDB.getNffgMap().containsKey(nffg.getName())){
			System.err.println("NffgService - LoadOneNffgOnNeo4J - Exception(\"Nffg already stored\")");
			throw new Exception("Nffg already stored");
		}

		// Create and set the Property element to be attached to the NffgNode
		Property nffg_property = new ObjectFactory().createProperty();
		nffg_property.setName("name");
		nffg_property.setValue(nffg.getName());

		// Create the NffgNode element and attach the Property element
		Node nffg_node = new ObjectFactory().createNode();
		nffg_node.getProperty().add(nffg_property);

		// Send the NffgNode to Neo4J 
		WebTarget target = createTarget();
		Response responseNffgNode = target.path("resource")
				.path("node")
				.request(MediaType.APPLICATION_XML)
				.post(Entity.entity(nffg_node, MediaType.APPLICATION_XML),Response.class);

		if(responseNffgNode.getStatus() >= 400){
			System.err.println("NffgService - LoadOneNffgOnNeo4J - POST NffgNode Error: "+responseNffgNode.getStatus()+ "Exception(\"Internal Server Error\")");
			throw new Exception("Internal Server Error");
		}

		Node response1 = responseNffgNode.readEntity(Node.class);

		// Save NffgNode in node map
		nodesMap.put(nffg.getName(),response1.getId());


		// Create the Labels element related to the nffg
		Labels nffg_label = new ObjectFactory().createLabels();
		nffg_label.getValue().add("NFFG");

		// Send the nffg Label to Neo4J
		target = createTarget();
		Response responseNffgLabel = target.path("resource")
				.path("node")
				.path(nodesMap.get(nffg.getName()))
				.path("label")
				.request(MediaType.APPLICATION_XML)
				.post(Entity.entity(nffg_label, MediaType.APPLICATION_XML));

		if(responseNffgLabel.getStatus() >= 400){
			System.err.println("NffgService - LoadOneNffgOnNeo4J - POST NffgLabel Error: "+responseNffgLabel.getStatus()+ "Exception(\"Internal Server Error\")");
			throw new Exception("Internal Server Error");
		}


		// Take the list of all nodes and links from the nffg
		nodes = nffg.getNodes().getNode();
		links = nffg.getLinks().getLink();

		/* Send every Node and every belongs to Neo4J */
		for(NodeType n : nodes){
			// Create and set the Property element to be attached to the Node	
			Property property = new ObjectFactory().createProperty();
			property.setName("name");
			property.setValue(n.getName());

			// Create the Node element and attach the Property element
			Node node = new ObjectFactory().createNode();
			node.getProperty().add(property);

			// Send the Node to Neo4J
			target = createTarget();
			Response responseNode = target.path("resource")
					.path("node")
					.request(MediaType.APPLICATION_XML)
					.post(Entity.entity(node, MediaType.APPLICATION_XML),Response.class);

			if(responseNode.getStatus() >= 400){
				System.err.println("NffgService - LoadOneNffgOnNeo4J - POST Node Error: "+responseNode.getStatus()+ "Exception(\"Internal Server Error\")");
				throw new Exception("Internal Server Error");
			}

			Node response2 = responseNode.readEntity(Node.class);

			// Save every node in node map
			nodesMap.put(n.getName(),response2.getId());


			// Create the Labels element related to the node
			Labels node_label = new ObjectFactory().createLabels();		
			node_label.getValue().add(n.getService().value().toString());

			// Send the node Label in Neo4J
			target = createTarget();
			Response responseLabel = target.path("resource")
					.path("node")
					.path(nodesMap.get(n.getName()))
					.path("label")
					.request(MediaType.APPLICATION_XML)
					.post(Entity.entity(node_label, MediaType.APPLICATION_XML));

			if(responseLabel.getStatus() >= 400){
				System.err.println("NffgService - LoadOneNffgOnNeo4J - POST Label Error: " + responseLabel.getStatus() + " Exception(\"Internal Server Error\")");
				throw new Exception("Internal Server Error");
			}

			// Create and set the Relationship "Belongs" element where the source is the nffg
			Relationship nffg_relationship = new ObjectFactory().createRelationship();
			nffg_relationship.setDstNode(nodesMap.get(n.getName()));
			nffg_relationship.setType("belongs");

			// Send the Relationship "Belongs" to Neo4J
			target = createTarget();
			Response responseBelong = target.path("resource")
					.path("node")
					.path(nodesMap.get(nffg.getName()))
					.path("relationship")
					.request(MediaType.APPLICATION_XML)
					.post(Entity.entity(nffg_relationship, MediaType.APPLICATION_XML), Response.class);	

			if(responseBelong.getStatus() >= 400){
				System.err.println("NffgService - LoadOneNffgOnNeo4J - POST Belong Relationship Error: " + responseBelong.getStatus() + " Exception(\"Internal Server Error\")");
				throw new Exception("Internal Server Error");
			}

			Relationship response3 = responseBelong.readEntity(Relationship.class);

			// Save every belongs in belongs map
			belongsMap.put(n.getName(),response3.getId());
		}

		/* Send every link to Neo4J */
		for(LinkType l: links){

			// Create and set the Relationship "Link" element
			Relationship relationship = new ObjectFactory().createRelationship();
			relationship.setDstNode(nodesMap.get(l.getDestination()));
			relationship.setType("Link");

			// Send the Relationship "Link" to Neo4J
			target = createTarget();
			Response responseLink = target.path("resource")
					.path("node")
					.path(nodesMap.get(l.getSource()))
					.path("relationship")
					.request(MediaType.APPLICATION_XML)
					.post(Entity.entity(relationship, MediaType.APPLICATION_XML), Response.class);	

			if(responseLink.getStatus() >= 400){
				System.err.println("NffgService - LoadOneNffgOnNeo4J - POST Link Relationship Error: " + responseLink.getStatus() + " Exception(\"Internal Server Error\")");
				throw new Exception("Internal Server Error");
			}

			Relationship response4 = responseLink.readEntity(Relationship.class);

			// Save every link in links map
			linksMap.put(l.getName(), response4.getId());
		}				

		// Create the NffgInfo Object and update the attribute last_update_time
		NffgInfo nffgInfo = new NffgInfo(nffg.getName(),nodesMap.get(nffg.getName()),nffg,nodesMap, linksMap, belongsMap);	
		nffgInfo.getNffg().setLastUpdateTime(updateTime());

		// Add the Nffg to the nffg database
		NffgsDB.addNewNffg(nffg.getName(), nffgInfo);	

		if(nffg.getPolicies() != null){
			// Store in PoliciesDB the Reachability Policies
			for(ReachabilityPolicyType rp : nffg.getPolicies().getReachabilityPolicy()){
				PolicyInfo policyInfo = new PolicyInfo(rp.getName(), nffg.getName(), rp.getSource(), rp.getDestination(), rp.isIsPositive(),rp.getVerification());
				PoliciesDB.addNewPolicy(rp.getName(), policyInfo);
			}
			// Store in PoliciesDB the Traversal Policies
			for(TraversalPolicyType tp : nffg.getPolicies().getTraversalPolicy()){
				PolicyInfo policyInfo = new PolicyInfo(tp.getName(), nffg.getName(), tp.getSource(), tp.getDestination(), tp.isIsPositive(), tp.getDevices(),tp.getVerification());
				PoliciesDB.addNewPolicy(tp.getName(), policyInfo);
			}
		}

		return nffg;
	}


	/** Get all the Nffgs stored in the database 
	 * @throws Exception **/
	public Nffgs getAllNffgs() throws Exception {
		// Create the Nffgs element that will contail all the nffgs
		Nffgs nffgs = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createNffgs();
		try{
			Set<String> list = new HashSet<String>();
			list  = NffgsDB.getNffgMap().keySet();
			Iterator<String> iter = list.iterator();

			// Put In the list of all Nffgs
			while(iter.hasNext()) {
				String key = iter.next();
				NffgInfo nffgInfo = NffgsDB.getNffgMap().get(key);
				nffgs.getNFFG().add(nffgInfo.getNffg());
			}
			return nffgs;
		}catch (RuntimeException e) {
			System.err.println("NffgService - getAllNffgs - RuntimeException(\"Internal Server Error\")");
			throw new Exception("Internal Server Error");
		}
	}


	/** Get the Nffg requested from the database 
	 * @throws Exception **/
	public NFFG getOneNffg(String nffgName) throws Exception {
		try{
			NffgInfo nffgInfo = NffgsDB.getNffgMap().get(nffgName);
			if(nffgInfo == null){
				System.err.println("NffgService - getOneNffg - Exception(\"Not found\")");
				throw new Exception("Not found");
			}
			return nffgInfo.getNffg();
		}catch (RuntimeException e) {
			System.err.println("NffgService - getOneNffg - RuntimeException(\"Internal Server Error\")");
			throw new Exception("Internal Server Error");
		}
	}


	/**Print some information about every Nffg in the NffgsDB
	 * Created for debugging purposes **/ 
	public void printNffgsMap(){
		Set<String> list = new HashSet<String>();
		list  = NffgsDB.getNffgMap().keySet();
		Iterator<String> iter = list.iterator();

		System.out.println("Map Size: " + NffgsDB.getNffgMap().size());

		while(iter.hasNext()) {
			String key = iter.next();
			NffgInfo nffgInfo = NffgsDB.getNffgMap().get(key);
			System.out.println("NffgName: "+NffgsDB.getNffgMap().get(key)+", NffgID: "+nffgInfo.getId());
		}
	}


	/** Convert from type Calendar to XMLGregorialCalendar**/
	public XMLGregorianCalendar calendarToXMLGregorianCalendar(Calendar calendar) {
		try {
			DatatypeFactory dtf = DatatypeFactory.newInstance();
			XMLGregorianCalendar xgc = dtf.newXMLGregorianCalendar();
			xgc.setYear(calendar.get(Calendar.YEAR));
			xgc.setMonth(calendar.get(Calendar.MONTH) + 1);
			xgc.setDay(calendar.get(Calendar.DAY_OF_MONTH));
			xgc.setHour(calendar.get(Calendar.HOUR_OF_DAY));
			xgc.setMinute(calendar.get(Calendar.MINUTE));
			xgc.setSecond(calendar.get(Calendar.SECOND));
			xgc.setMillisecond(calendar.get(Calendar.MILLISECOND));
			// Calendar ZONE_OFFSET and DST_OFFSET fields are in milliseconds.
			int offsetInMinutes = (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / (60 * 1000);
			xgc.setTimezone(offsetInMinutes);
			return xgc;
		} catch (DatatypeConfigurationException e) {
			System.out.print(e.getMessage());
			return null;
		}
	}

	/** Return the current time **/
	public XMLGregorianCalendar updateTime(){
		XMLGregorianCalendar xmlGregorianCalendar = calendarToXMLGregorianCalendar(Calendar.getInstance());
		return xmlGregorianCalendar;
	}

	/** Create the target **/
	private WebTarget createTarget(){
		WebTarget target;
		String property_value = System.getProperty("it.polito.dp2.NFFG.lab3.NEO4JURL");
		Client c = ClientBuilder.newClient();

		// If the property is not set, the default value
		if(property_value == null)
			property_value = "http://localhost:8080/Neo4JXML/rest";

		target = c.target(property_value);
		return target;
	}

}
