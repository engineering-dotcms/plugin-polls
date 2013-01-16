package com.eng.dotcms.polls;

public class PollsManFactory {
	
	public static PollsManAPI getPollsManAPI(){
		return new PollsManAPIImpl();
	}
}
