package com.dotcms.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.dotcms.rest.WebResource;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.UserAPI;
import com.dotmarketing.portlets.contentlet.business.ContentletAPI;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.util.Logger;

@Path("/contentletUtil")
public class ContentletUtilResource extends WebResource {

	private ContentletAPI conAPI = APILocator.getContentletAPI();
	private UserAPI userAPI = APILocator.getUserAPI();
	
	@GET
	@Path("/exist/{identifier}/{lang}")
	@Produces(MediaType.TEXT_PLAIN)
	public String existContentlet(@PathParam("identifier") String identifier, @PathParam("lang") long languageId) {
		try{
			String exist = "false";
			StringBuffer luceneQuery = new StringBuffer();
			luceneQuery.append("+identifier:");
			luceneQuery.append(identifier);
			luceneQuery.append(" +live:true");
			luceneQuery.append(" +languageId:");
			luceneQuery.append(languageId);
			List<Contentlet> con = conAPI.search(luceneQuery.toString(), 0, 0, null, userAPI.getSystemUser(), true);
			if(null!=con && con.size()>0)
				exist = "true";
			return exist;
		}catch(Exception e){
			Logger.error(this, e.getMessage(), e);
			return "false";
		}
	}
}
