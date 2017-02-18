package it.polito.dp2.NFFG.sol3.service;

import it.polito.dp2.NFFG.sol3.service.database.NffgInfo;
import it.polito.dp2.NFFG.sol3.service.database.NffgsDB;
import it.polito.dp2.NFFG.sol3.service.database.PoliciesDB;
import it.polito.dp2.NFFG.sol3.service.database.PolicyInfo;
import it.polito.dp2.NFFG.sol3.service.jaxb.Policy;
import it.polito.dp2.NFFG.sol3.service.jaxb.ReachabilityPolicyType2;
import it.polito.dp2.NFFG.sol3.service.jaxb.TraversalPolicyType2;

public class NffgServicePoliciesManagment {
	
	/** Add a policy in the PoliciesDB and in the NffgsDB 
	 * @throws Exception **/
	public Policy addNewPolicy(Policy policy_to_add) throws Exception {
		try{
			// Check if the policy to be added is a Reachability Policy
			if(policy_to_add.getTraversalPolicy() == null){

				// Check if there is a ReachabilityPolicy
				if(policy_to_add.getReachabilityPolicy() == null)
					throw new Exception("Not found"); 

				// Create a new ReachabilityPolicy
				ReachabilityPolicyType2 reachability_policy = policy_to_add.getReachabilityPolicy();

				// Check if the nffg inside Reachability Policy exists, if no do not add it
				if(NffgsDB.getNffgMap().containsKey(reachability_policy.getNffg()) == false){
					throw new Exception("Not found"); 
				} 

				if(NffgsDB.getNffgMap().get(reachability_policy.getNffg()).isReachabilityPolicy(reachability_policy.getName())== true){
					updatePolicy(policy_to_add);
					return null;
				}else{

					// Create a new PolicyInfo 
					PolicyInfo policyInfo = new PolicyInfo(reachability_policy.getName(), reachability_policy.getNffg(), reachability_policy.getSource(), reachability_policy.getDestination(), reachability_policy.isIsPositive(),reachability_policy.getVerification());

					//Store Policy in the PoliciesDB
					synchronized(NffgsDB.getNffgMap()){
						PoliciesDB.addNewPolicy(reachability_policy.getName(), policyInfo);
						NffgsDB.addReachabilityPolicy(reachability_policy);
					}
					return policy_to_add;
				}
			}
			else{
				// Create a new TraversalPolicy
				TraversalPolicyType2 traversal_policy = policy_to_add.getTraversalPolicy();			

				// Check if the nffg inside Traversal Policy exists, if no do not add it
				if(NffgsDB.getNffgMap().containsKey(traversal_policy.getNffg()) == false){
					throw new Exception("Not found"); 
				}	

				if(NffgsDB.getNffgMap().get(traversal_policy.getNffg()).isTraversalPolicy(traversal_policy.getName())== true){
					updatePolicy(policy_to_add);
					return null;
				}else{

					// Create a new PolicyInfo and store it in the PoliciesDB
					PolicyInfo policyInfo = new PolicyInfo(traversal_policy.getName(), traversal_policy.getNffg(), traversal_policy.getSource(), traversal_policy.getDestination(), traversal_policy.isIsPositive(),traversal_policy.getDevices(),traversal_policy.getVerification());

					//Store Policy in the PoliciesDB
					synchronized(NffgsDB.getNffgMap()){
						PoliciesDB.addNewPolicy(traversal_policy.getName(), policyInfo);
						NffgsDB.addTraversalPolicy(traversal_policy);
					}
					return policy_to_add;
				}
			}
		}catch(RuntimeException e){
			throw new Exception("Internal Server Error");
		}
	}
	
	/** Update the selected policy in NffgsDb and PoliciesDB 
	 * @throws Exception **/
	public void updatePolicy(Policy policy_to_update) throws Exception {
		ReachabilityPolicyType2 reachability_policy = policy_to_update.getReachabilityPolicy();
		try{
			if(reachability_policy != null){
				// Reachability Policy Update
				// Check if the nffg exist
				if(NffgsDB.getNffgMap().get(reachability_policy.getNffg()) == null){
					throw new Exception("Not found");
				}
				// Check if the policy exists
				if(NffgsDB.getNffgMap().get(reachability_policy.getNffg()).getReachabilityPolicyFromNffg(reachability_policy.getName())==null){
					throw new Exception("Not found");
				}
				synchronized (NffgsDB.getNffgMap()){
					// TODO test with Sleep(10);
					NffgsDB.updateReachabilityPolicy(reachability_policy, reachability_policy.getNffg());
					PoliciesDB.updatePolicy(policy_to_update);
				}
			}
			else{
				// Traversal Policy Update
				TraversalPolicyType2 traversal_policy = policy_to_update.getTraversalPolicy();

				if(NffgsDB.getNffgMap().get(traversal_policy.getNffg()) == null){
					throw new Exception("Not found");
				}
				// Check if the policy exists
				if(NffgsDB.getNffgMap().get(traversal_policy.getNffg()).getTraversalPolicyFromNffg(traversal_policy.getName())==null){
					throw new Exception("Not found");
				}
				synchronized (NffgsDB.getNffgMap()){
					NffgsDB.updateTraversalPolicy(traversal_policy, traversal_policy.getNffg());
					PoliciesDB.updatePolicy(policy_to_update);
				}
			}
		}catch (RuntimeException e) {
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
				synchronized(NffgsDB.getNffgMap()){
					// Delete Reachability policy in NffgsDB
					NffgsDB.getNffgMap().get(policyInfo.getNffg()).deleteReachabilityPolicyFromNffg(policyName);
					//Delete the policy inside the PoliciesDB
					PoliciesDB.deletePolicy(policyName);
				}
			}
			else{
				synchronized(NffgsDB.getNffgMap()){
					// Delete Traversal policy in NffgsDB
					NffgsDB.getNffgMap().get(policyInfo.getNffg()).deleteTraversalPolicyFromNffg(policyName);
					//Delete the policy inside the PoliciesDB
					PoliciesDB.deletePolicy(policyName);
				}
			}	
		}
	}
	
	/** Get the Policy specified in the request**/	
	public Policy getPolicy(String policyID) throws Exception {
		try{
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
		} catch(RuntimeException e){
			throw new Exception("Internal Servier Error");
		}
	}
}
