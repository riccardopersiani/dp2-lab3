package it.polito.dp2.NFFG.sol3.service.resources;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import it.polito.dp2.NFFG.sol3.service.NffgService;
import it.polito.dp2.NFFG.sol3.service.jaxb.ReachabilityPolicyType2;

@Path("/verification")
public class VerificationServiceResource {
	// Create an instance of the object that can execute operations
			NffgService nffgService = new NffgService();

			//TODO Verificata o no? Come faccio a rispondere al client e dove?
			@GET
			@ApiOperation(	value = "perform the policy/policies verification", notes = "text plain format")
			@ApiResponses(value = {
					@ApiResponse(code = 200, message = "OK"),
					@ApiResponse(code = 500, message = "Internal Server Error")})
			@Produces(MediaType.TEXT_PLAIN)
			public Response verifyPolicies(Map<String, List<String>> policyVerificationMap, @Context UriInfo uriInfo) {
				try{
					Set<String> list = new HashSet<String>();
					list  = policyVerificationMap.keySet();
					Iterator<String> iter = list.iterator();
					
					/** Send the list of policies to be verified **/
					while(iter.hasNext()) {
					     String key = iter.next();
					     for(String policy_name : policyVerificationMap.get(key)){
								nffgService.sendPolicyVerification(policy_name, key);
					     }					     
					}
				} catch (Exception e){
					return Response.serverError().build();
				}
				return Response.ok().build();
			}

			//TODO
			@POST 
			@ApiOperation ( value = "create a new reachability policy object", notes = "xml format")
			@ApiResponses(value = {
					@ApiResponse (code = 200, message = "0K"),
					@ApiResponse (code = 500, message = "Internal Server Error")
			})
			@Consumes(MediaType.APPLICATION_XML)
			public Response postPolicyXML(ReachabilityPolicyType2 policy, @Context UriInfo uriInfo) {
				try{
					nffgService.addNewPolicy(policy);
				} catch(Exception e) {
					return Response.serverError().build();
				}
				return Response.ok().build();
			}
			
			
			@DELETE
			@ApiOperation ( value = "delete a policy", notes = "xml format")
			@ApiResponses(value = {
					@ApiResponse (code = 200, message = "0K"),
					@ApiResponse(code = 404, message = "Not Found"),
					@ApiResponse (code = 500, message = "Internal Server Error")
			})
			@Consumes(MediaType.APPLICATION_XML)
			public void deleteNffgs() {
				return;
			}
}
