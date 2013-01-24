package com.eng.dotcms.polls.quartz.job.util;

import java.io.File;
import java.io.FilenameFilter;

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
