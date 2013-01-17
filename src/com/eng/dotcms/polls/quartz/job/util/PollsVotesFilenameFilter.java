package com.eng.dotcms.polls.quartz.job.util;

import java.io.File;
import java.io.FilenameFilter;
import static com.eng.dotcms.polls.util.PollsConstants.PROP_POLL_VOTES_FILENAME;

public class PollsVotesFilenameFilter implements FilenameFilter {
	
	private String name;
	
	public PollsVotesFilenameFilter(String aName){
		name = aName;
	}
	
	@Override
	public boolean accept(File dir, String name) {		
		return name.startsWith(this.name);
	}

}
