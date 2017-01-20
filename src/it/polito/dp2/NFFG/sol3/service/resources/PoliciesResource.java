package it.polito.dp2.NFFG.sol3.service.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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

@Path("/policies")
public class PoliciesResource {
	// Create an instance of the object that can execute operations
		NffgService nffgService = new NffgService();

		@GET
		@ApiOperation(	value = "get all policies", notes = "text plain format")
		@ApiResponses(value = {
				@ApiResponse(code = 200, message = "OK"),
				@ApiResponse(code = 500, message = "Internal Server Error")})
		@Produces(MediaType.TEXT_PLAIN)
		public String test() {
			return "Policies collection is reachable";
		}

		
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
		
		@PUT
		@ApiOperation ( value = "update a reachability policy", notes = "xml format")
		@ApiResponses(value = {
				@ApiResponse (code = 200, message = "0K"),
				@ApiResponse (code = 500, message = "Internal Server Error")
		})
		@Consumes(MediaType.APPLICATION_XML)
		public Response updatePolicyXML(ReachabilityPolicyType2 policy, @Context UriInfo uriInfo) {
			try{
				nffgService.updatePolicy(policy);
			} catch(Exception e) {
				return Response.serverError().build();
			}
			return Response.ok().build();
		}
		
		@DELETE
		@ApiOperation ( value = "delete all policies", notes = "xml format")
		@ApiResponses(value = {
				@ApiResponse (code = 200, message = "0K"),
				@ApiResponse(code = 404, message = "Not Found"),
				@ApiResponse (code = 500, message = "Internal Server Error")
		})
		@Consumes(MediaType.APPLICATION_XML)
		public void deletePolicies() {
			return;
		}
		
		@DELETE
		@Path("{PolicyID}")
		@ApiOperation ( value = "delete one policy", notes = "both...")
		@ApiResponses(value = {
				@ApiResponse (code = 200, message = "0K"),
				@ApiResponse(code = 404, message = "Not Found"),
				@ApiResponse (code = 500, message = "Internal Server Error")
		})
		public void deleteOnePolicy(@PathParam("PolicyID") String PolicyID) {
			try{
				nffgService.deleteOnePolicy(PolicyID);
			} catch(Exception e) {
				throw new InternalServerErrorException();
			}
		}
	

}
