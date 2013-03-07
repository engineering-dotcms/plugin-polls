package com.eng.dotcms.polls.quartz.job;

import static com.eng.dotcms.polls.util.PollsConstants.POLL_STRUCTURE_NAME;
import static com.eng.dotcms.polls.util.PollsConstants.PLUGIN_ID;
import static com.eng.dotcms.polls.util.PollsConstants.PROP_ENABLE_EXPIRED_JOB;
import static com.eng.dotcms.polls.util.PollsConstants.PROP_REMOTE_ENABLED;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import com.dotcms.publisher.business.DotPublisherException;
import com.dotcms.publisher.business.PublisherAPI;
import com.dotmarketing.beans.Permission;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.UserAPI;
import com.dotmarketing.cache.StructureCache;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.plugin.business.PluginAPI;
import com.dotmarketing.portlets.categories.model.Category;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.util.Logger;

public class ExpiredPollsJob implements StatefulJob {
	
	private PluginAPI pluginAPI = APILocator.getPluginAPI();
	private UserAPI userAPI = APILocator.getUserAPI();
	private ContentletUtilHandler handler = new ContentletUtilHandler();
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {			
			boolean enabled = Boolean.parseBoolean(pluginAPI.loadProperty(PLUGIN_ID, PROP_ENABLE_EXPIRED_JOB));
			boolean remoteEnabled = Boolean.parseBoolean(pluginAPI.loadProperty(PLUGIN_ID, PROP_REMOTE_ENABLED));
			// check if it's enabled
			if(enabled){
				List<String> pollsToPublish = new ArrayList<String>();
				GregorianCalendar now = new GregorianCalendar();
				Logger.debug(this, "BEGIN: Check if some polls are expired...");
				int count = 0;
				List<Contentlet> polls = APILocator.getContentletAPI().findByStructure(StructureCache.getStructureByVelocityVarName(POLL_STRUCTURE_NAME), APILocator.getUserAPI().getSystemUser() , false, 0, 0);
				for(Contentlet poll : polls){
					Date expirationDate  = (Date)poll.getMap().get("expiration_date");		
					String expired = (String)poll.getMap().get("expired");
					if(expirationDate.before(now.getTime()) && !Boolean.parseBoolean(expired)){ // the contentlet is expired...disable it
						Logger.debug(this, "the poll with identifier "+poll.getIdentifier()+" is expired...disable it");
						poll.setStringProperty("expired", "true");
						poll.setInode("0");
						List<Category> categories = APILocator.getCategoryAPI().findTopLevelCategories( APILocator.getUserAPI().getSystemUser(), false );
						List<Permission> structurePermissions = APILocator.getPermissionAPI().getPermissions(StructureCache.getStructureByVelocityVarName(POLL_STRUCTURE_NAME));
						APILocator.getContentletAPI().validateContentlet( poll, categories );
						poll = APILocator.getContentletAPI().checkin( poll, categories, structurePermissions, APILocator.getUserAPI().getSystemUser(), true );
						APILocator.getContentletAPI().publish(poll, APILocator.getUserAPI().getSystemUser(), true);
						count++;
						if(handler.existOnReceivers(poll.getIdentifier(), poll.getLanguageId()))
							pollsToPublish.add(poll.getIdentifier());
					}
					
					// FIXME: we need some kind of REST service that can tell me if a Poll is on the other hands or not... 
					if(remoteEnabled && pollsToPublish.size()>0){
						PublisherAPI publisherAPI = PublisherAPI.getInstance();
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-H-m");
						String _stringDate = dateFormat.format(new GregorianCalendar().getTime());
						Date publishDate = dateFormat.parse(_stringDate);
						String bundleId = UUID.randomUUID().toString();
						publisherAPI.addContentsToPublish(pollsToPublish, bundleId, publishDate, userAPI.getSystemUser());
					}
						
				}
				Logger.debug(this, "END: Check if some polls are expired...");
				Logger.info(this, "Number of expired Polls updated: " + count);				
			}
		} catch (DotDataException e) {
			Logger.error(this, "Error...",e);
		} catch (DotSecurityException e) {
			Logger.error(this, "Error...",e);
		} catch (ParseException e){
			Logger.error(this, "Error...",e);
		} catch (DotPublisherException e) {
			Logger.error(this, "Error...",e);
		}

	}

}
