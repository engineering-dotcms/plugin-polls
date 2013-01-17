package com.eng.dotcms.polls.quartz.job;

import java.io.File;
import java.text.SimpleDateFormat;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.UserAPI;
import com.dotmarketing.plugin.business.PluginAPI;
import com.dotmarketing.portlets.contentlet.business.ContentletAPI;

/**
 * 
 * @author Graziano Aliberti - Engineering Ingegneria Informatica S.p.a
 *
 * Jan 17, 2013 - 10:22:50 AM
 */
public class PutVotesIntoCSVJob implements StatefulJob {
	
	private ContentletAPI conAPI;
	private UserAPI userAPI;
	private PluginAPI pluginAPI;
	private SimpleDateFormat sdf;
	
	public PutVotesIntoCSVJob(){
		conAPI = APILocator.getContentletAPI();
		userAPI = APILocator.getUserAPI();
		pluginAPI = APILocator.getPluginAPI();
		sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		File destinationFolder = new File(pathname)
	}

}
