package it.polito.dp2.NFFG.sol3.service.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import it.polito.dp2.NFFG.sol3.service.NffgService;
import it.polito.dp2.NFFG.sol3.service.jaxb.PoliciesToBeVerified;
import it.polito.dp2.NFFG.sol3.service.jaxb.PoliciesVerified;

@Path("/verification")
public class VerificationServiceResource {
	// Create an instance of the object that can execute operations
			NffgService nffgService = new NffgService();

			@PUT 
			@ApiOperation ( value = "Verify one or more policies already stored", notes = "xml format")
			@ApiResponses(value = {
					@ApiResponse (code = 200, message = "0K"),
					@ApiResponse (code = 404, message =  "Not Found"),
					@ApiResponse (code = 500, message = "Internal Server Error")
			})
			@Consumes(MediaType.APPLICATION_XML)
			public PoliciesVerified verifyPolicies(PoliciesToBeVerified policies, @Context UriInfo uriInfo) throws Exception {
				try{
					return nffgService.verifyPolicies(policies);
				} catch(Exception e) {
					if(e.getMessage().equals("Not found")){
						throw new NotFoundException();
					}else{
						throw new InternalServerErrorException();
					}
				}
			}
}
