package it.polito.dp2.NFFG.sol3.service.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
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
import it.polito.dp2.NFFG.sol3.service.jaxb.NFFG;
import it.polito.dp2.NFFG.sol3.service.jaxb.Nffgs;

@Path("/nffgs")
public class NffgsResource {

	NffgService nffgService = new NffgService();

	@GET
	@ApiOperation(	value = "get all the nffgs", notes = "text plain format")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Produces(MediaType.APPLICATION_XML)
	public Nffgs getAllNffgsXML() {
		try{
			return nffgService.getAllNffgs();
		} catch(Exception e) {
			throw new InternalServerErrorException();
		}
	}

	@GET
	@Path("{NffgID}")
	@ApiOperation ( value = "get one nffg", notes = "text plain format")
	@ApiResponses(value = {
			@ApiResponse (code = 200, message = "0K"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse (code = 500, message = "Internal Server Error")
	})
	@Produces(MediaType.APPLICATION_XML)
	public NFFG getOneNffgXML(@PathParam("NffgID") String NffgID) {
		try{
			return nffgService.getOneNffg(NffgID);
		} catch(Exception e) {
			if(e.getMessage().equals("Not found")){
				throw new NotFoundException();
			}else{
				throw new InternalServerErrorException();
			}
		}
	}

	@POST 
	@ApiOperation ( value = "load a new nffg object", notes = "xml format")
	@ApiResponses(value = {
			@ApiResponse (code = 200, message = "0K"),
			@ApiResponse (code = 409, message = "Conflict"),
			@ApiResponse (code = 500, message = "Internal Server Error")
	})
	@Consumes(MediaType.APPLICATION_XML)
	public Response createNffgXML(NFFG nffg, @Context UriInfo uriInfo) {
		try{
			nffgService.LoadOneNffgOnNeo4J(nffg);
		} catch(Exception e) {
			if(e.getMessage().equals("Nffg already stored")){
				return Response.status(Response.Status.CONFLICT).build();
			}else{
				throw new InternalServerErrorException();
			}
		} 
		return Response.ok().build();
	}
}
