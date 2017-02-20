package it.polito.dp2.NFFG.sol3.service;

import java.util.Calendar;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import it.polito.dp2.NFFG.sol3.service.database.NffgInfo;
import it.polito.dp2.NFFG.sol3.service.database.NffgsDB;
import it.polito.dp2.NFFG.sol3.service.database.PoliciesDB;
import it.polito.dp2.NFFG.sol3.service.database.PolicyInfo;
import it.polito.dp2.NFFG.sol3.service.jaxb.PoliciesToBeVerified;
import it.polito.dp2.NFFG.sol3.service.jaxb.PoliciesVerified;
import it.polito.dp2.NFFG.sol3.service.jaxb.ReachabilityPolicyType2;
import it.polito.dp2.NFFG.sol3.service.jaxb.TraversalPolicyType2;
import it.polito.dp2.NFFG.sol3.service.jaxb.VerificationType;
import it.polito.dp2.NFFG.sol3.service.neo4j.Path;
import it.polito.dp2.NFFG.sol3.service.neo4j.Paths;

public class NffgServiceVerification {
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
					ReachabilityPolicyType2 rp = policyInfo.getReachabilityPolicy();
					policies_to_be_returned.getReachabilityPolicy().add(rp);
				}
				// Check if the policy to be returned is a Traversal policy
				else{
					TraversalPolicyType2 tp = policyInfo.getTraversalPolicy();
					policies_to_be_returned.getTraversalPolicy().add(tp);
				}
			}
			else{
				System.err.println("Verification-verifyPolicies - Policy/Policies passed not found: Exception(\"Not found\")");
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
			Response responseTest = target.path("resource")
					.path("node")
					.path(sourceNodeID)
					.path("paths")
					.queryParam("dst", destiantionNodeID)
					.request()
					.get(Response.class);

			if(responseTest.getStatus() >= 400){
				System.err.println("Verification - sendPolicyVerification - GET Paths Error: " + responseTest.getStatus() + " Exception(\"Internal Server Error\")");
				throw new Exception("Internal Server Error");
			}

			Paths response3 = responseTest.readEntity(Paths.class);

			List<Path> pathList = response3.getPath();

			// If there at least one Path the two nodes are reachable 
			//TODO REACHABLE POLICY
			if(pathList.isEmpty() == false){

				synchronized(NffgsDB.getNffgMap()){
					// Adding the Verification if the policy have not one
					if(policyInfo.getVerification() == null){
						setVerificationForReachablePolicy(policyInfo, policy_name);
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
					}else{
						policyInfo.getVerification().setResult(false);
						policyInfo.getVerification().setMessage("Policy Negative and Reachable");
					}

					return policyInfo;
				}
			}
			// The are no Path, the two nodes are unreachable 
			//TODO NON REACHABLE POLICY
			else{
				synchronized(NffgsDB.getNffgMap()){
					// Adding the Verification if the policy have not one
					if(policyInfo.getVerification() == null){
						setVerificationForUnreachablePolicy(policyInfo, policy_name);
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
					}else{
						policyInfo.getVerification().setResult(true);
						policyInfo.getVerification().setMessage("Policy Negative and Unreachable");
					}
					return policyInfo;
				}
			}

		} catch(RuntimeException e){
			System.err.println("Verification - sendPolicyVerification - RuntimeException(\"Internal Server Error\")");
			throw new Exception("Internal Server Error");
		}
	}
	
	public void setVerificationForReachablePolicy(PolicyInfo policyInfo, String policy_name){
		VerificationType verification = new it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory().createVerificationType();
		policyInfo.setVerification(verification);

		// Setting the new verification in the nffgDB
		NffgInfo nffgInfoVerification = NffgsDB.getNffgMap().get(policyInfo.getNffg());

		// The verification to be stored is related to a ReachabilityPolicy
		if(nffgInfoVerification.isTraversalPolicy(policy_name) == false){

			for(int i=0; i<nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().size(); i++){
				if(nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getName().equals(policy_name)){
					nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).setVerification(verification);
					verification = nffgInfoVerification.getNffg().getPolicies().getReachabilityPolicy().get(i).getVerification();
					verification.setTime(updateTime());

					if(policyInfo.getIsPositive() == true){
						verification.setMessage("Policy is Positive and Reachable");
						verification.setResult(true);
					}else{
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
					}else{
						verification.setMessage("Policy is Negative and Reachable");		
						verification.setResult(false);
					}
				}
			}
		}
	}

	
	
	public void setVerificationForUnreachablePolicy(PolicyInfo policyInfo, String policy_name){
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
					}else{
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
					}else{
						verification.setMessage("Policy is Negative but Unreachable");		
						verification.setResult(true);
					}
				}
			}
		}
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
}
