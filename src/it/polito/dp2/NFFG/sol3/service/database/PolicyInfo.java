package it.polito.dp2.NFFG.sol3.service.database;

import it.polito.dp2.NFFG.sol3.service.jaxb.ReachabilityPolicyType2;
import it.polito.dp2.NFFG.sol3.service.jaxb.VerificationType;

public class PolicyInfo {
	private String policy_name;
	private String nffg_name;
	private String policy_source;
	private String policy_destination;
	private Boolean isPositive;
	private VerificationType verification;
	private ReachabilityPolicyType2 policy;

	public PolicyInfo(String policy_name, String nffg_name, String policy_source,String policy_destination,Boolean isPositive,VerificationType verification){
		this.policy_name = policy_name;
		this.nffg_name = nffg_name;
		this.policy_source = policy_source;
		this.policy_destination = policy_destination;
		this.isPositive = isPositive;
		this.verification = verification;

	}

	public String getName(){
		return this.policy_name;
	}

	public String getNffg(){
		return this.nffg_name;
	}
	
	public String getSource(){
		return this.policy_source;
	}
	
	public String getDestination(){
		return this.policy_destination;
	}
	
	public VerificationType getVerification(){
		return this.verification;
	}
	
	public Boolean getIsPositive(){
		return this.isPositive;
	}
	
	public ReachabilityPolicyType2 getPolicy() {
		return this.policy;
	}
}
