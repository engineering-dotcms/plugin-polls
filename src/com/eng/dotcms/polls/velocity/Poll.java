package com.eng.dotcms.polls.velocity;

import java.util.List;

public class Poll {
	
	private StringBuilder question;
	private List<PollChoice> choices;
	
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
	
}
