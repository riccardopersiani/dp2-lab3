package it.polito.dp2.NFFG.sol3.service.database;

import it.polito.dp2.NFFG.sol3.service.jaxb.DevicesListType;
import it.polito.dp2.NFFG.sol3.service.jaxb.ObjectFactory;
import it.polito.dp2.NFFG.sol3.service.jaxb.ReachabilityPolicyType2;
import it.polito.dp2.NFFG.sol3.service.jaxb.TraversalPolicyType2;
import it.polito.dp2.NFFG.sol3.service.jaxb.VerificationType;

public class PolicyInfo {
	private String policy_name;
	private String nffg_name;
	private String policy_source;
	private String policy_destination;
	private Boolean isPositive;
	private VerificationType verification;
	private DevicesListType devices;

	/* Constructor for a traversal policy */
	public PolicyInfo(String policy_name, String nffg_name, String policy_source,String policy_destination,Boolean isPositive, DevicesListType devices){
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

	public DevicesListType getDevices(){
		if(this.devices == null){
			return null;
		}
		return this.devices;
	}

	public ReachabilityPolicyType2 getReachabilityPolicy(){
		ReachabilityPolicyType2 rp = new ObjectFactory().createReachabilityPolicyType2();

		rp.setDestination(this.policy_destination);
		rp.setIsPositive(this.isPositive);
		rp.setName(this.policy_name);
		rp.setNffg(this.nffg_name);
		rp.setSource(this.policy_source);
		rp.setVerification(this.verification);
		return rp;
	}

	public TraversalPolicyType2 getTraversalPolicy(){
		TraversalPolicyType2 tp = new ObjectFactory().createTraversalPolicyType2();

		tp.setDestination(this.policy_destination);
		tp.setIsPositive(this.isPositive);
		tp.setName(this.policy_name);
		tp.setNffg(this.nffg_name);
		tp.setSource(this.policy_source);
		tp.setVerification(this.verification);			
		tp.setDevices(this.devices);
		return tp;
	}

	public void printInfos(){
		System.out.print("Policy Name: " + this.policy_name + ", Nffg: " + this.nffg_name+", Src: " + this.policy_source + ", Dst: " + this.getDestination()+", isPositive: " + this.isPositive);
		if(this.verification != null)
			System.out.print(", *Verification* Msg: " + this.verification.getMessage() + ", Time: " + this.verification.getTime() + ", Result: " + this.verification.isResult());
		if(this.devices != null)
			System.out.print(", *Devices* isEmpty? " + this.getDevices().getDevice().isEmpty());	
		System.out.println(".");
	}

}
