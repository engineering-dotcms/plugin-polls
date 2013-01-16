package com.eng.dotcms.polls.quartz.job;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import com.dotmarketing.beans.Permission;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.cache.StructureCache;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.categories.model.Category;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.util.Logger;

public class ExpiredPollsJob implements StatefulJob {

	@SuppressWarnings("deprecation")
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		GregorianCalendar now = new GregorianCalendar();
		Logger.debug(this, "BEGIN: Check if some polls is expired...");
		int count = 0;
		try {
			List<Contentlet> polls = APILocator.getContentletAPI().findByStructure(StructureCache.getStructureByName("Poll"), APILocator.getUserAPI().getSystemUser() , false, 0, 0);
			for(Contentlet poll : polls){
				Date expirationDate  = (Date)poll.getMap().get("expiration_date");		
				String expired = (String)poll.getMap().get("expired");
				if(expirationDate.before(now.getTime()) && !Boolean.parseBoolean(expired)){ // the contentlet is expired...disable it
					poll.setStringProperty("expired", "true");
					poll.setInode("0");
					List<Category> categories = APILocator.getCategoryAPI().findTopLevelCategories( APILocator.getUserAPI().getSystemUser(), false );
					List<Permission> structurePermissions = APILocator.getPermissionAPI().getPermissions(StructureCache.getStructureByName("Poll") );
					APILocator.getContentletAPI().validateContentlet( poll, categories );
					poll = APILocator.getContentletAPI().checkin( poll, categories, structurePermissions, APILocator.getUserAPI().getSystemUser(), true );
					APILocator.getContentletAPI().publish(poll, APILocator.getUserAPI().getSystemUser(), true);
					count++;
				}
			}
			Logger.debug(this, "END: Check if some polls is expired...");
			Logger.info(this, "Number of expired Polls updated: " + count);
		} catch (DotDataException e) {
			Logger.error(this, "Error...",e);
		} catch (DotSecurityException e) {
			Logger.error(this, "Error...",e);
		}

	}

}
