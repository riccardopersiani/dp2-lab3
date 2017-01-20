package it.polito.dp2.NFFG.sol3.service.database;

import java.util.List;

import it.polito.dp2.NFFG.sol3.service.jaxb.VerificationType;
import it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory;
import it.polito.dp2.NFFG.sol3.service.jaxb.ReachabilityPolicyType2;
import it.polito.dp2.NFFG.sol3.service.jaxb.ServiceType;
import it.polito.dp2.NFFG.sol3.service.jaxb.TraversalPolicyType2;

public class PolicyInfo {
	private String policy_name;
	private String nffg_name;
	private String policy_source;
	private String policy_destination;
	private Boolean isPositive;
	private VerificationType verification;
	private List<ServiceType> devices;

	/* Constructor for a traversal policy */
	public PolicyInfo(String policy_name, String nffg_name, String policy_source,String policy_destination,Boolean isPositive, List<ServiceType> devices){
		this.policy_name = policy_name;
		this.nffg_name = nffg_name;
		this.policy_source = policy_source;
		this.policy_destination = policy_destination;
		this.isPositive = isPositive;
		this.devices = devices;
	}

	/* Constructor for a reachability policy */
	public PolicyInfo(String policy_name, String nffg_name, String policy_source,String policy_destination,Boolean isPositive){
		this.policy_name = policy_name;
		this.nffg_name = nffg_name;
		this.policy_source = policy_source;
		this.policy_destination = policy_destination;
		this.isPositive = isPositive;
	}

	public void setVerification(VerificationType verification){
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

	public List<ServiceType> getDevices(){
		if(this.devices == null){
			return null;
		}
		return this.devices;
	}

	public void printInfos(){
		System.out.print("Policy Name: " + this.policy_name + ", Nffg: " + this.nffg_name+", Src: " + this.policy_source + ", Dst: " + this.getDestination()+", isPositive: " + this.isPositive);
		if(this.verification != null)
			System.out.print(", *Verification* Msg: " + this.verification.getMessage() + ", Time: " + this.verification.getTime() + ", Result: " + this.verification.isResult());
		if(this.devices != null)
			System.out.print(", *Devices* isEmpty? " + this.getDevices().isEmpty());	
		System.out.println(".");
	}
	
	public ReachabilityPolicyType2 setReachabilityPolicy(){
		ReachabilityPolicyType2 rp = new ObjectFactory().createReachabilityPolicyType2();
		rp.setDestination(this.policy_destination);
		rp.setIsPositive(this.isPositive);
		rp.setName(this.policy_name);
		rp.setNffg(this.nffg_name);
		rp.setSource(this.policy_source);
		rp.setVerification(this.verification);
		return rp;
	}
	
	public TraversalPolicyType2 setTraversalPolicy(){
		TraversalPolicyType2 rp = new ObjectFactory().createTraversalPolicyType2();
		rp.setDestination(this.policy_destination);
		rp.setIsPositive(this.isPositive);
		rp.setName(this.policy_name);
		rp.setNffg(this.nffg_name);
		rp.setSource(this.policy_source);
		rp.setVerification(this.verification);
		//TODO
		return rp;
	}

}
