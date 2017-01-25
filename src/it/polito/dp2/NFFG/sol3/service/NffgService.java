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
import it.polito.dp2.NFFG.sol3.service.jaxb.PoliciesToBeVerified;
import it.polito.dp2.NFFG.sol3.service.jaxb.PoliciesVerified;
import it.polito.dp2.NFFG.sol3.service.jaxb.Policy;
import it.polito.dp2.NFFG.sol3.service.jaxb.ReachabilityPolicyType;
import it.polito.dp2.NFFG.sol3.service.jaxb.ReachabilityPolicyType2;
import it.polito.dp2.NFFG.sol3.service.jaxb.TraversalPolicyType;
import it.polito.dp2.NFFG.sol3.service.jaxb.TraversalPolicyType2;
import it.polito.dp2.NFFG.sol3.service.jaxb.VerificationType;
import it.polito.dp2.NFFG.sol3.service.neo4j.Labels;
import it.polito.dp2.NFFG.sol3.service.neo4j.Node;
import it.polito.dp2.NFFG.sol3.service.neo4j.ObjectFactory;
import it.polito.dp2.NFFG.sol3.service.neo4j.Path;
import it.polito.dp2.NFFG.sol3.service.neo4j.Paths;
import it.polito.dp2.NFFG.sol3.service.neo4j.Property;
import it.polito.dp2.NFFG.sol3.service.neo4j.Relationship;

public class NffgService {


	/** Add a policy in the PoliciesDB and in the NffgsDB 
	 * @throws Exception **/
	public void addNewPolicy(Policy policy_to_add) throws Exception {

		// Check if the policy to be added is a Reachability Policy
		if(policy_to_add.getTraversalPolicy() == null){

			// Check if there is a ReachabilityPolicy
			if(policy_to_add.getReachabilityPolicy() == null)
				throw new Exception("Not found"); 
			
			// Create a new ReachabilityPolicy
			ReachabilityPolicyType2 reachability_policy = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createReachabilityPolicyType2();
			reachability_policy = policy_to_add.getReachabilityPolicy();

			// Check if the Reachability Policy is still in the database, if yes do not add it
			if(PoliciesDB.getPoliciesMap().containsKey(reachability_policy.getName()) == true){
				PoliciesDB.getPoliciesMap().remove(reachability_policy.getName());				
			}

			// Check if the nffg inside Reachability Policy exists, if no do not add it
			if(NffgsDB.getNffgMap().containsKey(reachability_policy.getNffg()) == false){
				throw new Exception("Nffg Not found"); 
			}

			// Create a new PolicyInfo and store it in the PoliciesDB
			PolicyInfo policyInfo = new PolicyInfo(reachability_policy.getName(), reachability_policy.getNffg(), reachability_policy.getSource(), reachability_policy.getDestination(), reachability_policy.isIsPositive());
			PoliciesDB.addNewPolicy(reachability_policy.getName(), policyInfo);

			// Get the NffgInfo element related to the policy in order to Update the NffgsDB
			NffgInfo nffgInfo = NffgsDB.getNffgMap().get(reachability_policy.getNffg());

			// The new policy must be stored in NffgsDB with ReachabilityPolicyType because we don't need nffg info stored inside the policy to be added
			ReachabilityPolicyType reachability_policy_nffg = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createReachabilityPolicyType();
			reachability_policy_nffg.setDestination(reachability_policy.getDestination());
			reachability_policy_nffg.setName(reachability_policy.getName());
			reachability_policy_nffg.setSource(reachability_policy.getSource());
			reachability_policy_nffg.setIsPositive(reachability_policy.isIsPositive());

			// Check if the verification field of the policy is not empty, if not set the verification data
			if(reachability_policy.getVerification() != null){
				reachability_policy_nffg.setVerification(reachability_policy.getVerification());
				policyInfo.setVerification(reachability_policy.getVerification());
			}

			// Update the NffgsDB adding the new policy to the related nffg
			nffgInfo.getNffg().getPolicies().getReachabilityPolicy().add(reachability_policy_nffg);

		}
		// The policy to be added is a Traversal Policy
		else{
			// Create a new TraversalPolicy
			TraversalPolicyType2 traversal_policy = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createTraversalPolicyType2();
			traversal_policy = policy_to_add.getTraversalPolicy();

			// Check if the Traversal Policy is still in the database, if yes do not add it
			if(PoliciesDB.getPoliciesMap().containsKey(traversal_policy.getName()) == true)
				PoliciesDB.getPoliciesMap().remove(traversal_policy.getName());				

			// Check if the nffg inside Traversal Policy exists, if no do not add it
			if(NffgsDB.getNffgMap().containsKey(traversal_policy.getNffg()) == false){
				throw new Exception("Not found"); 
			}

			// Create a new PolicyInfo and store it in the PoliciesDB
			PolicyInfo policyInfo = new PolicyInfo(traversal_policy.getName(), traversal_policy.getNffg(), traversal_policy.getSource(), traversal_policy.getDestination(), traversal_policy.isIsPositive(),traversal_policy.getDevices());
			PoliciesDB.addNewPolicy(traversal_policy.getName(), policyInfo);

			// Get the NffgInfo element related to the policy in order to Update the NffgsDB
			NffgInfo nffgInfo = NffgsDB.getNffgMap().get(traversal_policy.getNffg());

			// The new policy must be stored in NffgsDB with TraversalPolicyType because we don't need nffg info stored inside the policy to be added
			TraversalPolicyType traversal_policy_nffg = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createTraversalPolicyType();
			traversal_policy_nffg.setDestination(traversal_policy.getDestination());
			traversal_policy_nffg.setName(traversal_policy.getName());
			traversal_policy_nffg.setSource(traversal_policy.getSource());
			traversal_policy_nffg.setIsPositive(traversal_policy.isIsPositive());

			// Check if the verification field of the policy is not empty, if not set the verification data
			if(traversal_policy.getVerification() != null){
				traversal_policy_nffg.setVerification(traversal_policy.getVerification());
				policyInfo.setVerification(traversal_policy.getVerification());
			}
			// Update the NffgsDB adding the new policy to the related nffg
			nffgInfo.getNffg().getPolicies().getTraversalPolicy().add(traversal_policy_nffg);
		}
	}

	/** Add the selected nffg on Neo4J and add the selected nffg and its relative policies in the cache 
	 * @throws Exception **/
	public void LoadOneNffgOnNeo4J(NFFG nffg) throws Exception{
		Map<String, String> nodesMap = new HashMap<String, String>(); 
		Map<String, String> linksMap = new HashMap<String, String>(); 
		Map<String, String> belongsMap = new HashMap<String, String>(); 

		List<NodeType> nodes = new ArrayList<NodeType>();
		List<LinkType> links = new ArrayList<LinkType>();

		if(NffgsDB.getNffgMap().containsKey(nffg.getName())){
			throw new Exception("Nffg already stored");
		}

		try{			
			// Create and set the Property element to be attached to the NffgNode
			Property nffg_property = new ObjectFactory().createProperty();
			nffg_property.setName("name");
			nffg_property.setValue(nffg.getName());

			// Create the NffgNode element and attach the Property element
			Node nffg_node = new ObjectFactory().createNode();
			nffg_node.getProperty().add(nffg_property);

			// Send the NffgNode to Neo4J 
			WebTarget target = createTarget();
			Node response1 = target.path("resource")
					.path("node")
					.request(MediaType.APPLICATION_XML)
					.post(Entity.entity(nffg_node, MediaType.APPLICATION_XML),Node.class);

			// Save NffgNode in node map
			nodesMap.put(nffg.getName(),response1.getId());
		}catch (RuntimeException e){
			throw new Exception("Internal Server Error");
		}

		try{
			// Create the Labels element related to the nffg
			Labels nffg_label = new ObjectFactory().createLabels();
			nffg_label.getValue().add("NFFG");

			// Send the nffg Label to Neo4J
			WebTarget target = createTarget();
			target.path("resource")
			.path("node")
			.path(nodesMap.get(nffg.getName()))
			.path("label")
			.request(MediaType.APPLICATION_XML)
			.post(Entity.entity(nffg_label, MediaType.APPLICATION_XML));
		}catch (RuntimeException e){
			throw new Exception("Internal Server Error");
		}

		// Take the list of all nodes and links from the nffg
		nodes = nffg.getNodes().getNode();
		links = nffg.getLinks().getLink();

		/* Send every Node and every belongs to Neo4J */
		for(NodeType n : nodes){
			try{
				// Create and set the Property element to be attached to the Node	
				Property property = new ObjectFactory().createProperty();
				property.setName("name");
				property.setValue(n.getName());

				// Create the Node element and attach the Property element
				Node node = new ObjectFactory().createNode();
				node.getProperty().add(property);

				// Send the Node to Neo4J
				WebTarget target = createTarget();
				Node response2 = target.path("resource")
						.path("node")
						.request(MediaType.APPLICATION_XML)
						.post(Entity.entity(node, MediaType.APPLICATION_XML),Node.class);

				// Save every node in node map
				nodesMap.put(n.getName(),response2.getId());
			}catch(RuntimeException e){
				throw new Exception("Internal Server Error");
			}
			try{
				// Create the Labels element related to the node
				Labels node_label = new ObjectFactory().createLabels();		
				node_label.getValue().add(n.getService().value().toString());

				// Send the node Label in Neo4J
				WebTarget target2 = createTarget();
				target2.path("resource")
				.path("node")
				.path(nodesMap.get(n.getName()))
				.path("label")
				.request(MediaType.APPLICATION_XML)
				.post(Entity.entity(node_label, MediaType.APPLICATION_XML));
			}catch(RuntimeException e){
				throw new Exception("Internal Server Error");
			}

			try{
				// Create and set the Relationship "Belongs" element where the source is the nffg
				Relationship nffg_relationship = new ObjectFactory().createRelationship();
				nffg_relationship.setDstNode(nodesMap.get(n.getName()));
				nffg_relationship.setType("belongs");

				// Send the Relationship "Belongs" to Neo4J
				WebTarget target3 = createTarget();
				Relationship response3 = target3.path("resource")
						.path("node")
						.path(nodesMap.get(nffg.getName()))
						.path("relationship")
						.request(MediaType.APPLICATION_XML)
						.post(Entity.entity(nffg_relationship, MediaType.APPLICATION_XML), Relationship.class);	

				// Save every belongs in belongs map
				belongsMap.put(n.getName(),response3.getId());
			}catch(RuntimeException e){
				throw new Exception("Internal Server Error");
			}
		}

		/* Send every link to Neo4J */
		for(LinkType l: links){

			try{
			// Create and set the Relationship "Link" element
			Relationship relationship = new ObjectFactory().createRelationship();
			relationship.setDstNode(nodesMap.get(l.getDestination()));
			relationship.setType("Link");

			// Send the Relationship "Link" to Neo4J
			WebTarget target = createTarget();
			Relationship response4 = target.path("resource")
					.path("node")
					.path(nodesMap.get(l.getSource()))
					.path("relationship")
					.request(MediaType.APPLICATION_XML)
					.post(Entity.entity(relationship, MediaType.APPLICATION_XML), Relationship.class);	

			// Save every link in links map
			linksMap.put(l.getName(), response4.getId());
			}catch(RuntimeException e){
				throw new Exception("Internal Server Error");
			}
		}				

		// Create the NffgInfo Object and update the attribute last_update_time
		NffgInfo nffgInfo = new NffgInfo(nffg.getName(),nodesMap.get(nffg.getName()),nffg,nodesMap, linksMap, belongsMap);	
		nffgInfo.getNffg().setLastUpdateTime(updateTime());

		// Add the Nffg to the nffg database
		NffgsDB.addNewNffg(nffg.getName(), nffgInfo);	

		if(nffg.getPolicies() != null){
			// Store in PoliciesDB the Reachability Policies
			for(ReachabilityPolicyType rp : nffg.getPolicies().getReachabilityPolicy()){
				PolicyInfo policyInfo = new PolicyInfo(rp.getName(), nffg.getName(), rp.getSource(), rp.getDestination(), rp.isIsPositive());
				PoliciesDB.addNewPolicy(rp.getName(), policyInfo);
				if(rp.getVerification() != null)
					policyInfo.setVerification(rp.getVerification());
			}
			// Store in PoliciesDB the Traversal Policies
			for(TraversalPolicyType tp : nffg.getPolicies().getTraversalPolicy()){
				PolicyInfo policyInfo = new PolicyInfo(tp.getName(), nffg.getName(), tp.getSource(), tp.getDestination(), tp.isIsPositive(), tp.getDevices());
				PoliciesDB.addNewPolicy(tp.getName(), policyInfo);
				if(tp.getVerification() != null)
					policyInfo.setVerification(tp.getVerification());
			}
		}
	}


	/** Update the selected policy in NffgsDb and PoliciesDB 
	 * @throws Exception **/
	public void updatePolicy(Policy policy_to_update) throws Exception {
		ReachabilityPolicyType2 policy = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createTraversalPolicyType2();		
		
		// If it is a Reachability policy, delete it inside NffgDB
		if(policy_to_update.getReachabilityPolicy() != null){
			policy = policy_to_update.getReachabilityPolicy();			
			//deleteOnePolicy(policy.getName());
			NffgsDB.getNffgMap().get(policy.getNffg()).deleteReachabilityPolicyFromNffg(policy.getName());

			// If it is a Traversal policy, delete it inside NffgDB
		} else{
			policy = policy_to_update.getTraversalPolicy();
			if(policy == null){
				throw new Exception("Not Found");
			}
			//deleteOnePolicy(policy.getName());
			NffgsDB.getNffgMap().get(policy.getNffg()).deleteTraversalPolicyFromNffg(policy.getName());
		}
		// Delete the policy inside the PoliciesDB
		PoliciesDB.deletePolicy(policy.getName());
		try {
			addNewPolicy(policy_to_update);
		} catch (RuntimeException e) {
			throw new Exception("Internal Server Error");
		}
	}


	/** Delete the selected policy in NffgsDB and PoliciesDB
	 * @throws Exception **/
	public void deleteOnePolicy(String policyName) throws Exception{

		// Check if the policy is not in the PolicyDB
		if(PoliciesDB.getPolicy(policyName) == null){
			throw new Exception("Not found");
		}

		// Take the containers in which the policy must be deleted
		PolicyInfo policyInfo = PoliciesDB.getPolicy(policyName);
		NffgInfo nffgInfo = NffgsDB.getNffgMap().get(policyInfo.getNffg());

		// Check if the policy is reachable
		for(int i =0; i<nffgInfo.getNffg().getPolicies().getReachabilityPolicy().size(); i++){
			if(nffgInfo.getNffg().getPolicies().getReachabilityPolicy().get(i).getName().equals(policyName)){
				// Delete Reachability policy in NffgsDB
				NffgsDB.getNffgMap().get(policyInfo.getNffg()).deleteReachabilityPolicyFromNffg(policyName);
			}
			else{
				// Delete Traversal policy in NffgsDB
				NffgsDB.getNffgMap().get(policyInfo.getNffg()).deleteTraversalPolicyFromNffg(policyName);
			}	
		}

		//Delete the policy inside the PoliciesDB
		PoliciesDB.deletePolicy(policyName);
	}

	/** Get all the Nffgs stored in the database **/

	/** Get the list of all the nffgs **/
	public Nffgs getAllNffgs() {
		// Create the Nffgs element that will contail all the nffgs
		Nffgs nffgs = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createNffgs();

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
	}

	/** Get the Nffg requested from the database **/
	public NFFG getOneNffg(String nffgName) {
		NffgInfo nffgInfo = NffgsDB.getNffgMap().get(nffgName);
		return nffgInfo.getNffg();
	}


	/**Print some information about every Nffg in the NffgsDB
	 * Created for debugging purposes **/ 
	public void printNffgsMap(){
		Set<String> list = new HashSet<String>();
		list  = NffgsDB.getNffgMap().keySet();
		Iterator<String> iter = list.iterator();

		System.out.println("Map Size: " + NffgsDB.getNffgMap().size());

		while(iter.hasNext()) {
			System.out.println("Inside the while");
			String key = iter.next();
			NffgInfo nffgInfo = NffgsDB.getNffgMap().get(key);
			System.out.println("NffgName: "+NffgsDB.getNffgMap().get(key)+", NffgID: "+nffgInfo.getId());
		}
	}


	/** Verify the list of policies specified **/
	public PoliciesVerified verifyPolicies(PoliciesToBeVerified policies) throws Exception {	
		// Get the list of the names of the policies to be verified 
		List<String> policies_to_verify = policies.getName();

		PolicyInfo policyInfo = null;
		PoliciesVerified policies_to_be_returned = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createPoliciesVerified();

		// Verify every policy in the list
		for(int i=0; i<policies_to_verify.size(); i++){

			if(PoliciesDB.getPolicy(policies_to_verify.get(i)) != null){
				policyInfo = sendPolicyVerification(policies_to_verify.get(i));

				// Check if the policy to be returned is a Reachability policy
				if(policyInfo.getDevices() == null){
					ReachabilityPolicyType2 rp = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createReachabilityPolicyType2();
					rp = policyInfo.getReachabilityPolicy();
					policies_to_be_returned.getReachabilityPolicy().add(rp);
				}
				// Check if the policy to be returned is a Traversal policy
				else{
					TraversalPolicyType2 rp = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createTraversalPolicyType2();
					rp = policyInfo.getTraversalPolicy();
					policies_to_be_returned.getTraversalPolicy().add(rp);
				}
			}
			else{
				throw new Exception("Not found");
			}
		}
		return policies_to_be_returned;
	}


	/** Send the policy verification requedt to Neo4J
	 * @throws Exception **/
	public PolicyInfo sendPolicyVerification(String policy_name) throws Exception{
		try{
			// Take the list of all policiesS
			PolicyInfo policyInfo = PoliciesDB.getPolicy(policy_name);

			// Get the NffgInfo from the database in order to take the names of source and destination node of the policy to be verifed
			NffgInfo nffgInfo = NffgsDB.getNffgMap().get(policyInfo.getNffg());
			String sourceNodeID = nffgInfo.getNodesMap().get(policyInfo.getSource());
			String destiantionNodeID =nffgInfo.getNodesMap().get(policyInfo.getDestination());

			// Send the data to be verified to Neo4J
			WebTarget target = createTarget();
			Paths response3 = (Paths) target.path("resource")
					.path("node")
					.path(sourceNodeID)
					.path("paths")
					.queryParam("dst", destiantionNodeID)
					.request()
					.get(Paths.class);
			
			List<Path> pathList = response3.getPath();
			
			// If there at least one Path the two nodes are reachable 
			if(pathList.isEmpty() == false){

				// Adding the Verification if the policy have not one
				if(policyInfo.getVerification() == null){
					VerificationType verification = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createVerificationType();
					policyInfo.setVerification(verification);

					//Setting the new verification in the nffgDB
					NffgInfo nffgInfoVerification = NffgsDB.getNffgMap().get(policyInfo.getNffg());

					//The verification to be stored is related to a ReachabilityPolicy
					if(nffgInfoVerification.isTraversalPolicy(policy_name) == false){

						for(int i=0; i<nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().size(); i++){

							if(nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getName().equals(policy_name)){
								nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).setVerification(verification);
								verification = nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification();
								verification.setTime(updateTime());

								if(policyInfo.getIsPositive() == true){
									verification.setMessage("Policy is Positive and Reachable");
									verification.setResult(true);
								}
								else{
									verification.setMessage("Policy is Negative and Reachable");					
									verification.setResult(false);
								}
							}
						}
					}
					//The verification to be stored is related to a TraversalPolicy
					else{
						for(int i=0; i<nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().size(); i++){

							if(nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getName().equals(policyInfo.getName())){
								nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).setVerification(verification);
								verification = nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification();
								verification.setTime(updateTime());
								if(policyInfo.getIsPositive() == true){
									verification.setMessage("Policy is Positive and Reachable");
									verification.setResult(true);
								}
								else{
									verification.setMessage("Policy is Negative and Reachable");		
									verification.setResult(false);
								}
							}
						}
					}
				}
				// Verification field is not null 
				else{
					NffgInfo nffgInfoVerification = NffgsDB.getNffgMap().get(policyInfo.getNffg());

					//The verification to be updated is related to a ReachabilityPolicy
					if(nffgInfoVerification.isTraversalPolicy(policy_name) == false){

						for(int i=0; i<nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().size(); i++){

							if(nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getName().equals(policy_name)){
								nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setTime(updateTime());

								if(policyInfo.getIsPositive() == true){
									nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setMessage("Policy is Positive and Reachable");
									nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setResult(true);
								}
								else{
									nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setResult(false);
									nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setMessage("Policy is Negative and Reachable");					
								}
							}
						}
					}
					//The verification to be updated is related to a TraversalPolicy
					else{
						for(int i=0; i<nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().size(); i++){
							if(nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getName().equals(policyInfo.getName())){
								nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setTime(updateTime());
								if(policyInfo.getIsPositive() == true){
									nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setResult(true);
									nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setMessage("Policy is Positive and Reachable");
								}else{
									nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setResult(false);
									nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setMessage("Policy is Negative and Reachable");					
								}
							}
						}
					}		
				}
				// Set the current time for the policies in the PoliciesDB
				policyInfo.getVerification().setTime(updateTime());			    

				if(policyInfo.getIsPositive() == true){
					policyInfo.getVerification().setResult(true);
					policyInfo.getVerification().setMessage("Policy Positive and Reachable");
				}
				else{
					policyInfo.getVerification().setResult(false);
					policyInfo.getVerification().setMessage("Policy Negative and Reachable");
				}

				return policyInfo;

			}
			// The are no Path, the two nodes are unreachable 
			else{

				// Adding the Verification if the policy have not one
				if(policyInfo.getVerification() == null){
					VerificationType verification = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createVerificationType();
					policyInfo.setVerification(verification);

					//Setting the new verification in the nffgDB
					NffgInfo nffgInfoVerification = NffgsDB.getNffgMap().get(policyInfo.getNffg());

					//The verification to be stored is related to a ReachabilityPolicy
					if(nffgInfoVerification.isTraversalPolicy(policy_name) == false){

						for(int i=0; i<nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().size(); i++){

							if(nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getName().equals(policy_name)){
								nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).setVerification(verification);
								verification = nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification();
								verification.setTime(updateTime());

								if(policyInfo.getIsPositive() == true){
									verification.setMessage("Policy is Positive and Unreachable");
									verification.setResult(false);
								}
								else{
									verification.setMessage("Policy is Negative and Unreachable");					
									verification.setResult(true);
								}
							}
						}
					}
					//The verification to be stored is related to a TraversalPolicy
					else{
						for(int i=0; i<nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().size(); i++){

							if(nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getName().equals(policyInfo.getName())){
								nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).setVerification(verification);
								verification = nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification();
								verification.setTime(updateTime());
								if(policyInfo.getIsPositive() == true){
									verification.setMessage("Policy is Positive and Unreachable");
									verification.setResult(false);
								}
								else{
									verification.setMessage("Policy is Negative but Unreachable");		
									verification.setResult(true);
								}
							}
						}
					}
				}
				// Verification to be updated
				else{
					NffgInfo nffgInfoVerification = NffgsDB.getNffgMap().get(policyInfo.getNffg());

					//The verification to be updated is related to a ReachabilityPolicy
					if(nffgInfoVerification.isTraversalPolicy(policy_name) == false){

						for(int i=0; i<nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().size(); i++){

							if(nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getName().equals(policy_name)){
								nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setTime(updateTime());

								if(policyInfo.getIsPositive() == true){
									nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setMessage("Policy is Positive and Unreachable");
									nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setResult(false);
								}
								else{
									nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setResult(true);
									nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification().setMessage("Policy is Negative and Unreachable");					
								}
							}
						}
					}
					//The verification to be updated is related to a TraversalPolicy
					else{
						for(int i=0; i<nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().size(); i++){
							if(nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getName().equals(policyInfo.getName())){
								nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setTime(updateTime());
								if(policyInfo.getIsPositive() == true){
									nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setResult(false);
									nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setMessage("Policy is Positive and Unreachable");
								}else{
									nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setResult(true);
									nffgInfoVerification.getNffg().getPolicies().getTraversalPolicy().get(i).getVerification().setMessage("Policy is Negative and Unreachable");					
								}
							}
						}
					}
				}
				// Set the current time for the policies in the PoliciesDB
				policyInfo.getVerification().setTime(updateTime());			    

				if(policyInfo.getIsPositive() == true){
					policyInfo.getVerification().setResult(false);
					policyInfo.getVerification().setMessage("Policy Positive and Unreachable");
				}
				else{
					policyInfo.getVerification().setResult(true);
					policyInfo.getVerification().setMessage("Policy Negative and Unreachable");
				}

				return policyInfo;
			}

		} catch(RuntimeException e){
			throw new Exception("Internal Server Error");
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

	/** Get the Policy specified in the request**/	
	public Policy getPolicy(String policyID) throws Exception {
		PolicyInfo policyInfo = PoliciesDB.getPolicy(policyID);
		if(policyInfo == null){
			throw new Exception("Not found");
		}
		Policy policy = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createPolicy();
		if(policyInfo.getDevices() == null){
			policy.setReachabilityPolicy(policyInfo.getReachabilityPolicy());
		}
		else{
			policy.setTraversalPolicy(policyInfo.getTraversalPolicy());
		}
		return policy;
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
