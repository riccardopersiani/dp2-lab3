package it.polito.dp2.NFFG.sol3.service.resources;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import it.polito.dp2.NFFG.sol3.service.NffgServicePoliciesManagment;
import it.polito.dp2.NFFG.sol3.service.jaxb.Policy;

@Path("/policies")
public class PoliciesResource {
	// Create an instance of the object that can execute operations
		NffgServicePoliciesManagment nffgServicePoliciesManagment = new NffgServicePoliciesManagment();
		
		@GET
		@Path("{PolicyID}")
		@ApiOperation ( value = "get selected policy", notes = "both...")
		@ApiResponses(value = {
				@ApiResponse (code = 200, message = "0K"),
				@ApiResponse(code = 404, message = "Not Found"),
				@ApiResponse (code = 500, message = "Internal Server Error")
		})
		public Policy getPolicy(@PathParam("PolicyID") String PolicyID) {
			try{
				return nffgServicePoliciesManagment.getPolicy(PolicyID);
			} catch(Exception e) {
				if(e.getMessage().equals("Not found")){
					throw new NotFoundException();
				}else{
					throw new InternalServerErrorException();
				}
			}
		}
	
		@POST 
		@ApiOperation ( value = "create a new policy object", notes = "xml format")
		@ApiResponses(value = {
				@ApiResponse (code = 204, message = "No Content"),
				@ApiResponse (code = 201, message = "Created"),
				@ApiResponse(code = 404, message = "Not Found"),
				@ApiResponse (code = 500, message = "Internal Server Error")
		})
		@Consumes(MediaType.APPLICATION_XML)
		@Produces(MediaType.APPLICATION_XML)
		public Response postPolicyXML(Policy policy, @Context UriInfo uriInfo) {
			try{
				Policy created = nffgServicePoliciesManagment.addNewPolicy(policy);
				URI u = null;
				if(created == null){
					return Response.noContent().build();
				}
				UriBuilder builder = uriInfo.getAbsolutePathBuilder();
				if(created.getTraversalPolicy() != null){
					u = builder.path(created.getTraversalPolicy().getName()).build();		
				}else{				
					u = builder.path(created.getReachabilityPolicy().getName()).build();
				}
	        	return Response.created(u).build();

			} catch(Exception e) {
				if(e.getMessage().equals("Not found")){
					return Response.status(Response.Status.NOT_FOUND).build();
				}else{
					throw new InternalServerErrorException();
				}
			}
		}		
		
		@PUT
		@ApiOperation ( value = "update a reachability policy", notes = "xml format")
		@ApiResponses(value = {
				@ApiResponse (code = 204, message = "No Content"),
				@ApiResponse(code = 404, message = "Not Found"),
				@ApiResponse (code = 500, message = "Internal Server Error")
		})
		@Consumes(MediaType.APPLICATION_XML)
		public Response updatePolicyXML(Policy policy, @Context UriInfo uriInfo) {
			try{
				nffgServicePoliciesManagment.updatePolicy(policy);
			} catch(Exception e) {
				if(e.getMessage().equals("Not found")){
					throw new NotFoundException();
				}else{
					throw new InternalServerErrorException();
				}
			}
			return Response.noContent().build();
		}
		
		@DELETE
		@Path("{PolicyID}")
		@ApiOperation ( value = "delete one policy", notes = "both...")
		@ApiResponses(value = {
				@ApiResponse (code = 200, message = "0K"),
				@ApiResponse(code = 404, message = "Not Found"),
				@ApiResponse (code = 500, message = "Internal Server Error")
		})
		public Response deleteOnePolicy(@PathParam("PolicyID") String PolicyID) {
			try{
				nffgServicePoliciesManagment.deleteOnePolicy(PolicyID);
			} catch(Exception e) {
				if(e.getMessage().equals("Not found")){
					return Response.status(Response.Status.NOT_FOUND).build();
				}else{
					throw new InternalServerErrorException();
				}
			}
			return Response.ok().build();
		}
	

}
