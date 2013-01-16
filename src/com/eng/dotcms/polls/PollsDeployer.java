package com.eng.dotcms.polls;

import java.util.Date;
import java.util.HashMap;

import org.quartz.CronTrigger;

import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.CacheLocator;
import com.dotmarketing.cache.StructureCache;
import com.dotmarketing.cms.content.submit.PluginDeployer;
import com.dotmarketing.plugin.business.PluginAPI;
import com.dotmarketing.portlets.structure.factories.RelationshipFactory;
import com.dotmarketing.portlets.structure.model.Field.DataType;
import com.dotmarketing.portlets.structure.model.Field.FieldType;
import com.dotmarketing.portlets.structure.model.Relationship;
import com.dotmarketing.portlets.structure.model.Structure;
import com.dotmarketing.quartz.CronScheduledTask;
import com.dotmarketing.quartz.QuartzUtils;
import com.dotmarketing.util.Logger;
import com.eng.dotcms.polls.util.PollsUtil;

/**
 * This class create, if setted and if there aren't, all the structures and the relationship that we need for make it works.
 * 
 * 
 * @author Graziano Aliberti - Engineering Ingegneria Informatica S.p.a
 *
 * Jan 14, 2013 - 2:43:56 PM
 */
public class PollsDeployer extends PluginDeployer {
	
	private static String POLL_STRUCTURE_NAME = "Poll";
	private static String CHOICE_STRUCTURE_NAME = "PollChoice";
	private static String VOTE_STRUCTURE_NAME = "PollVote";
	private static String RELATIONSHIP_NAME = "Parent_Poll-Child_PollChoice";
	private static String PLUGIN_ID = "com.eng.dotcms.polls";
	private static String AUTO_CREATE_STRUCTURES = "polls.autoCreateStructures";
	
	private PluginAPI pluginAPI = APILocator.getPluginAPI();
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean deploy() {
		try {
			if(Boolean.valueOf(pluginAPI.loadProperty(PLUGIN_ID, AUTO_CREATE_STRUCTURES))){
				// create Poll Structure
				if(!PollsUtil.existStructure(POLL_STRUCTURE_NAME)) {
					Logger.info(PollsDeployer.class, "The Structure Polls must be created");
					Structure pollStructure = PollsUtil.createStructure(POLL_STRUCTURE_NAME, POLL_STRUCTURE_NAME, APILocator.getHostAPI().findDefaultHost(APILocator.getUserAPI().getSystemUser(), true), Structure.STRUCTURE_TYPE_CONTENT);					
					// start field creation
					PollsUtil.createField(pollStructure.getInode(), "Title", FieldType.TEXT, DataType.TEXT, true, true, true, true);
					PollsUtil.createField(pollStructure.getInode(), "Question", FieldType.TEXT_AREA, DataType.LONG_TEXT, true, true, false, true);
					PollsUtil.createField(pollStructure.getInode(), "Expiration Date", FieldType.DATE_TIME, DataType.DATE, true, true, false, false);
					PollsUtil.createField(pollStructure.getInode(), "Expired", FieldType.TEXT, DataType.TEXT, true, true, false, false);
				}
				
				// create PollChoice Structure
				if(!PollsUtil.existStructure(CHOICE_STRUCTURE_NAME)) {
					Logger.info(PollsDeployer.class, "The Structure PollsChoice must be created");
					Structure pollChoiceStructure = PollsUtil.createStructure(CHOICE_STRUCTURE_NAME, CHOICE_STRUCTURE_NAME, APILocator.getHostAPI().findDefaultHost(APILocator.getUserAPI().getSystemUser(), true), Structure.STRUCTURE_TYPE_CONTENT);
					PollsUtil.createField(pollChoiceStructure.getInode(), "Text", FieldType.TEXT, DataType.TEXT, true, true, false, true);
					PollsUtil.createField(pollChoiceStructure.getInode(), "Id", FieldType.TEXT, DataType.TEXT, true, true, false, false);
				}
		
				// create PollVote Structure
				if(!PollsUtil.existStructure(VOTE_STRUCTURE_NAME)) {
					Logger.info(PollsDeployer.class, "The Structure PollsVote must be created");
					Structure pollVoteStructure = PollsUtil.createStructure(VOTE_STRUCTURE_NAME, VOTE_STRUCTURE_NAME, APILocator.getHostAPI().findDefaultHost(APILocator.getUserAPI().getSystemUser(), true), Structure.STRUCTURE_TYPE_CONTENT);
					PollsUtil.createField(pollVoteStructure.getInode(), "Poll", FieldType.TEXT, DataType.TEXT, true, true, false, true);
					PollsUtil.createField(pollVoteStructure.getInode(), "Choice", FieldType.TEXT, DataType.TEXT, true, true, false, true);
					PollsUtil.createField(pollVoteStructure.getInode(), "User", FieldType.TEXT, DataType.TEXT, true, false, false, true);
				}
				if(!PollsUtil.existRelationship(RELATIONSHIP_NAME, StructureCache.getStructureByName(POLL_STRUCTURE_NAME))){
					Logger.info(PollsDeployer.class, "The Relationship must be created");
					Relationship relationshipPollPollChoice = new Relationship();
					relationshipPollPollChoice.setParentStructureInode(StructureCache.getStructureByName(POLL_STRUCTURE_NAME).getInode());
					relationshipPollPollChoice.setParentRelationName("Parent "+POLL_STRUCTURE_NAME);
					relationshipPollPollChoice.setParentRequired(false);
					relationshipPollPollChoice.setChildStructureInode(StructureCache.getStructureByName(CHOICE_STRUCTURE_NAME).getInode());
					relationshipPollPollChoice.setChildRelationName("Child "+CHOICE_STRUCTURE_NAME);
					relationshipPollPollChoice.setChildRequired(false);
					relationshipPollPollChoice.setCardinality(0);
					relationshipPollPollChoice.setRelationTypeValue(RELATIONSHIP_NAME);
					RelationshipFactory.saveRelationship(relationshipPollPollChoice);					
				}
				
			}
			//scheduled job
			String jobName = pluginAPI.loadProperty(PLUGIN_ID, "quartz.job.name");
			String jobGroup = pluginAPI.loadProperty(PLUGIN_ID, "quartz.job.group");
			String jobDescription = pluginAPI.loadProperty(PLUGIN_ID, "quartz.job.description");
			String javaClassname = pluginAPI.loadProperty(PLUGIN_ID, "quartz.job.java.classname");
			String cronExpression = pluginAPI.loadProperty(PLUGIN_ID, "quartz.job.cron.expression");
			CronScheduledTask cronScheduledTask = new CronScheduledTask(jobName, jobGroup, jobDescription, javaClassname, new Date(), null, CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW, new HashMap<String, Object>(), cronExpression);
			QuartzUtils.scheduleTask(cronScheduledTask);
			CacheLocator.getCacheAdministrator().flushAll();
			return true;
		}catch(Exception e){
			Logger.error(PollsDeployer.class, "Error in deploy plugin "+PLUGIN_ID, e);
			CacheLocator.getCacheAdministrator().flushAll();
			return false;
		}
	}
	
}

