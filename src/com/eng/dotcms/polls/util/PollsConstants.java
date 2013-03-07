package com.eng.dotcms.polls.util;

public class PollsConstants {
	
	// plugin id
	public static final String PLUGIN_ID 									= 	"com.eng.dotcms.polls";
	
	
	// global
	public static final String PROP_AUTO_CREATE_STRUCTURES 					= 	"polls.autoCreateStructures";
	public static final String PROP_POLLS_JOB_GROUP		 					= 	"quartz.job.group";
	public static final String PROP_POLL_VOTES_FILENAME						=	"quartz.csv.filename";
	public static final String PROP_POLL_STATUS_FILENAME					=	"quartz.csv.status.filename";
	public static final String PROP_POLL_STATUS_BUSY						=	"quartz.csv.status.busy";
	public static final String PROP_POLL_STATUS_FREE						=	"quartz.csv.status.free";
	public static final String PROP_REMOTE_ENABLED							=	"plugin.remote.enabled";
	
	
	
	// scheduled jobs
	public static final String PROP_ENABLE_EXPIRED_JOB						= 	"quartz.expired.job.enable";
	public static final String PROP_EXPIRED_JOB_NAME	 					= 	"quartz.expired.job.name";
	public static final String PROP_EXPIRED_JOB_DESCRIPTION					= 	"quartz.expired.job.description";
	public static final String PROP_EXPIRED_JOB_CLASS						= 	"quartz.expired.job.java.classname";
	public static final String PROP_EXPIRED_JOB_CRON_EXP					= 	"quartz.expired.job.cron.expression";
	
	public static final String PROP_ENABLE_PUT_CSV_JOB 						= 	"quartz.putCSV.enable";
	public static final String PROP_PUT_CSV_JOB_NAME	 					= 	"quartz.putCSV.job.name";
	public static final String PROP_PUT_CSV_JOB_DESCRIPTION					= 	"quartz.putCSV.job.description";
	public static final String PROP_PUT_CSV_JOB_CLASS						= 	"quartz.putCSV.job.java.classname";
	public static final String PROP_PUT_CSV_JOB_CRON_EXP					= 	"quartz.putCSV.job.cron.expression";
	public static final String PROP_PUT_CSV_JOB_DEST_PATH 					= 	"quartz.putCSV.destinationPath";

	public static final String PROP_ENABLE_GET_CSV_JOB 						= 	"quartz.getCSV.enable";
	public static final String PROP_GET_CSV_JOB_NAME	 					= 	"quartz.getCSV.job.name";
	public static final String PROP_GET_CSV_JOB_DESCRIPTION					= 	"quartz.getCSV.job.description";
	public static final String PROP_GET_CSV_JOB_CLASS						= 	"quartz.getCSV.job.java.classname";
	public static final String PROP_GET_CSV_JOB_CRON_EXP					= 	"quartz.getCSV.job.cron.expression";
	public static final String PROP_GET_CSV_JOB_SRC_PATH 					= 	"quartz.getCSV.sourcePath";
	
	
	// deployer	
	public static final String POLL_STRUCTURE_NAME 							= 	"Poll";
	public static final String CHOICE_STRUCTURE_NAME 						= 	"PollChoice";
	public static final String VOTE_STRUCTURE_NAME 							= 	"PollVote";
	public static final String RELATIONSHIP_NAME 							= 	"Parent_Poll-Child_PollChoice";

	
	
}
