package com.eng.dotcms.polls;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.quartz.CronTrigger;
import org.quartz.SchedulerException;

import com.dotmarketing.beans.Permission;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.CacheLocator;
import com.dotmarketing.business.PermissionAPI;
import com.dotmarketing.cache.StructureCache;
import com.dotmarketing.cms.content.submit.PluginDeployer;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.plugin.business.PluginAPI;
import com.dotmarketing.portlets.contentlet.business.Contentlet;
import com.dotmarketing.portlets.structure.factories.RelationshipFactory;
import com.dotmarketing.portlets.structure.model.Field.DataType;
import com.dotmarketing.portlets.structure.model.Field.FieldType;
import com.dotmarketing.portlets.structure.model.Relationship;
import com.dotmarketing.portlets.structure.model.Structure;
import com.dotmarketing.quartz.CronScheduledTask;
import com.dotmarketing.quartz.QuartzUtils;
import com.dotmarketing.util.Logger;
import com.eng.dotcms.polls.util.PollsUtil;

import static com.eng.dotcms.polls.util.PollsConstants.*;

/**
 * This class create, if setted and if there aren't, all the structures and the relationship that we need for make it works.
 * 
 * 
 * @author Graziano Aliberti - Engineering Ingegneria Informatica S.p.a
 *
 * Jan 14, 2013 - 2:43:56 PM
 */
public class PollsDeployer extends PluginDeployer {
		
	private PluginAPI pluginAPI = APILocator.getPluginAPI();
	private PermissionAPI perAPI = APILocator.getPermissionAPI();
	
	@Override
	public boolean deploy() {
		try {
			if(Boolean.valueOf(pluginAPI.loadProperty(PLUGIN_ID, PROP_AUTO_CREATE_STRUCTURES))){
				// create Poll Structure
				if(!PollsUtil.existStructure(POLL_STRUCTURE_NAME)) {
					Logger.info(PollsDeployer.class, "The Structure Polls must be created");
					Structure pollStructure = PollsUtil.createStructure(POLL_STRUCTURE_NAME, POLL_STRUCTURE_NAME, APILocator.getHostAPI().findDefaultHost(APILocator.getUserAPI().getSystemUser(), true), Structure.STRUCTURE_TYPE_CONTENT);					
					// start field creation
					PollsUtil.createField(pollStructure.getInode(), "Title", FieldType.TEXT, DataType.TEXT, true, true, true, true);
					PollsUtil.createField(pollStructure.getInode(), "Question", FieldType.TEXT_AREA, DataType.LONG_TEXT, true, true, false, true);
					PollsUtil.createField(pollStructure.getInode(), "Expiration Date", FieldType.DATE_TIME, DataType.DATE, true, true, false, false);
					PollsUtil.createField(pollStructure.getInode(), "Expired", FieldType.TEXT, DataType.TEXT, true, true, false, false);
					PollsUtil.createField(pollStructure.getInode(), "PollPath", FieldType.HOST_OR_FOLDER, DataType.TEXT, true, true, false, false);
				}
				
				// create PollChoice Structure
				if(!PollsUtil.existStructure(CHOICE_STRUCTURE_NAME)) {
					Logger.info(PollsDeployer.class, "The Structure PollsChoice must be created");
					Structure pollChoiceStructure = PollsUtil.createStructure(CHOICE_STRUCTURE_NAME, CHOICE_STRUCTURE_NAME, APILocator.getHostAPI().findDefaultHost(APILocator.getUserAPI().getSystemUser(), true), Structure.STRUCTURE_TYPE_CONTENT);
					PollsUtil.createField(pollChoiceStructure.getInode(), "Text", FieldType.TEXT, DataType.TEXT, true, true, false, true);
					PollsUtil.createField(pollChoiceStructure.getInode(), "Id", FieldType.TEXT, DataType.TEXT, true, true, false, false);
					PollsUtil.createField(pollChoiceStructure.getInode(), "ChoicePath", FieldType.HOST_OR_FOLDER, DataType.TEXT, true, true, false, false);
				}	
				
				// create relationship between the Poll structures
				if(!PollsUtil.existRelationship(RELATIONSHIP_NAME, StructureCache.getStructureByVelocityVarName(POLL_STRUCTURE_NAME))){
					Logger.info(PollsDeployer.class, "The Relationship must be created");
					Relationship relationshipPollPollChoice = new Relationship();
					relationshipPollPollChoice.setParentStructureInode(StructureCache.getStructureByVelocityVarName(POLL_STRUCTURE_NAME).getInode());
					relationshipPollPollChoice.setParentRelationName("Parent "+POLL_STRUCTURE_NAME);
					relationshipPollPollChoice.setParentRequired(false);
					relationshipPollPollChoice.setChildStructureInode(StructureCache.getStructureByVelocityVarName(CHOICE_STRUCTURE_NAME).getInode());
					relationshipPollPollChoice.setChildRelationName("Child "+CHOICE_STRUCTURE_NAME);
					relationshipPollPollChoice.setChildRequired(false);
					relationshipPollPollChoice.setCardinality(0);
					relationshipPollPollChoice.setRelationTypeValue(RELATIONSHIP_NAME);
					RelationshipFactory.saveRelationship(relationshipPollPollChoice);					
				}	
			}
			// create PollVote Structure
			if(!PollsUtil.existStructure(VOTE_STRUCTURE_NAME)) {
				Logger.info(PollsDeployer.class, "The Structure PollsVote must be created");
				Structure pollVoteStructure = PollsUtil.createStructure(VOTE_STRUCTURE_NAME, VOTE_STRUCTURE_NAME, APILocator.getHostAPI().findDefaultHost(APILocator.getUserAPI().getSystemUser(), true), Structure.STRUCTURE_TYPE_CONTENT);
				PollsUtil.createField(pollVoteStructure.getInode(), "Poll", FieldType.TEXT, DataType.TEXT, true, true, false, true);
				PollsUtil.createField(pollVoteStructure.getInode(), "Choice", FieldType.TEXT, DataType.TEXT, true, true, false, true);
				PollsUtil.createField(pollVoteStructure.getInode(), "User", FieldType.TEXT, DataType.TEXT, true, false, false, true);				
				// handle the vote back for remote publishing
				PollsUtil.createField(pollVoteStructure.getInode(), "Sent to Sender", FieldType.RADIO, DataType.BOOL, true, true, false, false);
				
				perAPI.assignPermissions(addAnonymousPermissions(pollVoteStructure), pollVoteStructure, APILocator.getUserAPI().getSystemUser(), true);
			}
					
			// scheduled all the configured jobs.
			scheduleJobs();
			
			return true;
		}catch(Exception e){
			Logger.error(PollsDeployer.class, "Error in deploy plugin "+PLUGIN_ID, e);
			CacheLocator.getCacheAdministrator().flushAll();
			return false;
		}
	}

	private List<Permission> addAnonymousPermissions(Structure pollVoteStructure) throws DotDataException {
		List<Permission> votePermissions = new ArrayList<Permission>();
		Permission cmsAnonPublish = new Permission();
		cmsAnonPublish.setType(Contentlet.class.getCanonicalName());
		cmsAnonPublish.setRoleId(APILocator.getRoleAPI().loadCMSAnonymousRole().getId());
		cmsAnonPublish.setPermission(PermissionAPI.PERMISSION_PUBLISH);
		cmsAnonPublish.setBitPermission(true);
		cmsAnonPublish.setInode(pollVoteStructure.getPermissionId());
		
		Permission cmsAnonEdit = new Permission();
		cmsAnonEdit.setType(Contentlet.class.getCanonicalName());
		cmsAnonEdit.setRoleId(APILocator.getRoleAPI().loadCMSAnonymousRole().getId());
		cmsAnonEdit.setPermission(PermissionAPI.PERMISSION_EDIT);
		cmsAnonEdit.setBitPermission(true);
		cmsAnonEdit.setInode(pollVoteStructure.getPermissionId());
		
		Permission cmsAnonUse = new Permission();
		cmsAnonUse.setType(Contentlet.class.getCanonicalName());
		cmsAnonUse.setRoleId(APILocator.getRoleAPI().loadCMSAnonymousRole().getId());
		cmsAnonUse.setPermission(PermissionAPI.PERMISSION_USE);
		cmsAnonUse.setBitPermission(true);
		cmsAnonUse.setInode(pollVoteStructure.getPermissionId());
		votePermissions.add(cmsAnonUse);
		votePermissions.add(cmsAnonEdit);
		votePermissions.add(cmsAnonPublish);
		return votePermissions;
	}
	
	private void scheduleJobs() throws SchedulerException, ParseException, ClassNotFoundException, DotDataException {
				
		//scheduled expired job (if enable)
		String enableExpiredJob = pluginAPI.loadProperty(PLUGIN_ID, PROP_ENABLE_EXPIRED_JOB);
		if(Boolean.parseBoolean(enableExpiredJob)){
			String jobName = pluginAPI.loadProperty(PLUGIN_ID, PROP_EXPIRED_JOB_NAME);
			String jobGroup = pluginAPI.loadProperty(PLUGIN_ID, PROP_POLLS_JOB_GROUP);
			String jobDescription = pluginAPI.loadProperty(PLUGIN_ID, PROP_EXPIRED_JOB_DESCRIPTION);
			String javaClassname = pluginAPI.loadProperty(PLUGIN_ID, PROP_EXPIRED_JOB_CLASS);
			String cronExpression = pluginAPI.loadProperty(PLUGIN_ID, PROP_EXPIRED_JOB_CRON_EXP);
			CronScheduledTask cronScheduledTask = new CronScheduledTask(jobName, jobGroup, jobDescription, javaClassname, new Date(), null, CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW, new HashMap<String, Object>(), cronExpression);
			QuartzUtils.scheduleTask(cronScheduledTask);
		}
		//scheduled put csv job (if enable)
		String enablePutCSV = pluginAPI.loadProperty(PLUGIN_ID, PROP_ENABLE_PUT_CSV_JOB);
		if(Boolean.parseBoolean(enablePutCSV)){
			String jobPutName = pluginAPI.loadProperty(PLUGIN_ID, PROP_PUT_CSV_JOB_NAME);
			String jobPutGroup = pluginAPI.loadProperty(PLUGIN_ID, PROP_POLLS_JOB_GROUP);
			String jobPutDescription = pluginAPI.loadProperty(PLUGIN_ID, PROP_PUT_CSV_JOB_DESCRIPTION);
			String javaPutClassname = pluginAPI.loadProperty(PLUGIN_ID, PROP_PUT_CSV_JOB_CLASS);
			String cronPutExpression = pluginAPI.loadProperty(PLUGIN_ID, PROP_PUT_CSV_JOB_CRON_EXP);
			CronScheduledTask cronPutScheduledTask = new CronScheduledTask(jobPutName, jobPutGroup, jobPutDescription, javaPutClassname, new Date(), null, CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW, new HashMap<String, Object>(), cronPutExpression);
			QuartzUtils.scheduleTask(cronPutScheduledTask);			
		}
		
		//scheduled get csv job (if enable)
		String enableGetCSV = pluginAPI.loadProperty(PLUGIN_ID, PROP_ENABLE_GET_CSV_JOB);
		if(Boolean.parseBoolean(enableGetCSV)){
			String jobGetName = pluginAPI.loadProperty(PLUGIN_ID, PROP_GET_CSV_JOB_NAME);
			String jobGetGroup = pluginAPI.loadProperty(PLUGIN_ID, PROP_POLLS_JOB_GROUP);
			String jobGetDescription = pluginAPI.loadProperty(PLUGIN_ID, PROP_GET_CSV_JOB_DESCRIPTION);
			String javaGetClassname = pluginAPI.loadProperty(PLUGIN_ID, PROP_GET_CSV_JOB_CLASS);
			String cronGetExpression = pluginAPI.loadProperty(PLUGIN_ID, PROP_GET_CSV_JOB_CRON_EXP);
			CronScheduledTask cronGetScheduledTask = new CronScheduledTask(jobGetName, jobGetGroup, jobGetDescription, javaGetClassname, new Date(), null, CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW, new HashMap<String, Object>(), cronGetExpression);
			QuartzUtils.scheduleTask(cronGetScheduledTask);
		}

	}
	
}

