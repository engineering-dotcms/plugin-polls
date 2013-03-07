package com.eng.dotcms.polls.quartz.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import com.dotcms.publisher.endpoint.bean.PublishingEndPoint;
import com.dotcms.publisher.endpoint.business.PublishingEndPointAPI;
import com.dotcms.publisher.util.TrustFactory;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

public class ContentletUtilHandler {
	
	private TrustFactory tFactory;
	private PublishingEndPointAPI endpointAPI = APILocator.getPublisherEndPointAPI();
	
	public ContentletUtilHandler() {
		tFactory = new TrustFactory();
	}
	
	public boolean existOnReceivers(String identifier, long languageId) {
		try {
			List<PublishingEndPoint> receivers = endpointAPI.getReceivingEndPoints();
			Map<String, List<PublishingEndPoint>> endpointsMap = new HashMap<String, List<PublishingEndPoint>>();
			List<PublishingEndPoint> buffer = null;
			
			for (PublishingEndPoint pEndPoint : receivers) {				
				String gid = UtilMethods.isSet(pEndPoint.getGroupId()) ? pEndPoint.getGroupId() : pEndPoint.getId();
				if(endpointsMap.get(gid) == null)
					buffer = new ArrayList<PublishingEndPoint>();
				else 
					buffer = endpointsMap.get(gid);
				buffer.add(pEndPoint);
				endpointsMap.put(gid, buffer);				
			}
			
			ClientConfig cc = new DefaultClientConfig();			
			if(Config.getStringProperty("TRUSTSTORE_PATH") != null && !Config.getStringProperty("TRUSTSTORE_PATH").trim().equals(""))
				cc.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(tFactory.getHostnameVerifier(), tFactory.getSSLContext()));
			Client client = Client.create(cc);
			boolean exists = false;
			for(String group : endpointsMap.keySet()) {
	        	List<PublishingEndPoint> groupList = endpointsMap.get(group);
	        	for (PublishingEndPoint endpoint : groupList) {
	        		WebResource resource = client.resource(endpoint.toURL()+"/api/contentletUtil/exist/"+identifier+"/"+languageId);
	        		ClientResponse response = resource.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
	        		exists = Boolean.parseBoolean(response.getEntity(String.class));	        		
	        	}
			}
			return exists;
		} catch (DotDataException e) {
			Logger.error(this, e.getMessage(), e);
			return false;
		}
	}
}
