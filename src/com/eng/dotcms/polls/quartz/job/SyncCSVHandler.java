package com.eng.dotcms.polls.quartz.job;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpStatus;

import com.dotcms.publisher.endpoint.bean.PublishingEndPoint;
import com.dotcms.publisher.endpoint.business.PublishingEndPointAPI;
import com.dotcms.publisher.pusher.PushUtils;
import com.dotcms.publisher.util.TrustFactory;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.plugin.business.PluginAPI;
import com.dotmarketing.util.Config;
import com.eng.dotcms.polls.util.PollsConstants;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;

public class SyncCSVHandler {
	private TrustFactory tFactory;
	private PublishingEndPointAPI endpointAPI = APILocator.getPublisherEndPointAPI();
	private PluginAPI pAPI = APILocator.getPluginAPI();
	
	public SyncCSVHandler() {
		tFactory = new TrustFactory();
	}
	
	public void sync() {
		//Get staging endpoints
		List<PublishingEndPoint> endpoints = null;
		try {
			endpoints = endpointAPI.getEnabledReceivingEndPoints();
			
			//Compress files
			File csvRoot = new File(
					pAPI.loadProperty(
					PollsConstants.PLUGIN_ID, 
					PollsConstants.PROP_GET_CSV_JOB_SRC_PATH));

			ArrayList<File> list = new ArrayList<File>(1);
			list.add(csvRoot);
			File csvZip = new File(csvRoot+File.separator+".."+File.separator+"csvZip.tar.gz");
			PushUtils.compressFiles(list, csvZip, csvRoot.getAbsolutePath());
			
			//Send files
			ClientConfig cc = new DefaultClientConfig();
			
			if(Config.getStringProperty("TRUSTSTORE_PATH") != null && !Config.getStringProperty("TRUSTSTORE_PATH").trim().equals(""))
				cc.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, 
						new HTTPSProperties(tFactory.getHostnameVerifier(), tFactory.getSSLContext()));
			
			Client client = Client.create(cc);
			
			for(PublishingEndPoint endpoint: endpoints) {
				FormDataMultiPart form = new FormDataMultiPart();

		        form.bodyPart(new FileDataBodyPart("csvZip", csvZip, MediaType.MULTIPART_FORM_DATA_TYPE));
				
				//Sending csvZip to endpoint
		        WebResource resource = client.resource(endpoint.toURL()+"/api/syncPolls/sync");
		        
		        ClientResponse response = 
		        		resource.type(MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
		        
		        
		        if(response.getClientResponseStatus().getStatusCode() != HttpStatus.SC_OK)  {
		        	throw new DotDataException("Failed to send csv to endpoint "+endpoint.toURL());
		        }
			}
		
			//Finish job
		} catch (DotDataException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
