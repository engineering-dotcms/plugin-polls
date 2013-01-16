package com.eng.dotcms.polls;

import java.util.List;

import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.contentlet.business.DotContentletStateException;
import com.dotmarketing.portlets.contentlet.business.DotContentletValidationException;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.liferay.portal.model.User;

public interface PollsManAPI {
	
	/**
	 * Checks if a user has already voted the given Poll
	 * 
	 * Jan 16, 2013 - 10:43:40 AM
	 */
	boolean hasVoted(String userId, String pollIdentifier, long languageId) throws DotContentletStateException, DotDataException, DotSecurityException;
	
	void vote(String userId, String pollIdentifier, String choiceIdentifier, User user) throws DotDataException, DotContentletValidationException, DotContentletStateException, IllegalArgumentException, DotSecurityException;
	
	int getTotalPollVotes(String pollIdentifier) throws DotDataException, DotSecurityException;
	
	List<Contentlet> getChoiceByPoll(String pollIdentifier, long languageId) throws DotDataException, DotSecurityException;
	
	int getChoiceVotes(String pollIdentifier, String choiceIdentifier) throws DotDataException, DotSecurityException;
}
