package com.eng.dotcms.polls.velocity;

import java.util.List;

public class Poll {
	
	private String identifier;
	private StringBuilder question;
	private List<PollChoice> choices;
	private boolean expired;
	
	public StringBuilder getQuestion() {
		return question;
	}
	public void setQuestion(StringBuilder question) {
		this.question = question;
	}
	public List<PollChoice> getChoices() {
		return choices;
	}
	public void setChoices(List<PollChoice> choices) {
		this.choices = choices;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public boolean isExpired() {
		return expired;
	}
	public void setExpired(boolean expired) {
		this.expired = expired;
	}
	
}
