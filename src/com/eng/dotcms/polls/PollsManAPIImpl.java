package com.eng.dotcms.polls;

import java.util.GregorianCalendar;
import java.util.List;

import com.dotmarketing.beans.Permission;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.cache.StructureCache;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.categories.model.Category;
import com.dotmarketing.portlets.contentlet.business.ContentletAPI;
import com.dotmarketing.portlets.contentlet.business.DotContentletStateException;
import com.dotmarketing.portlets.contentlet.business.DotContentletValidationException;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.structure.factories.RelationshipFactory;
import com.dotmarketing.portlets.structure.model.Structure;
import com.dotmarketing.util.Logger;
import com.liferay.portal.model.User;
import static com.eng.dotcms.polls.util.PollsConstants.*;

/**
 * Implementation of PollsManAPI
 * 
 * This file is part of Poll Management for dotCMS.
 * Poll Management for dotCMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Poll Management for dotCMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Poll Management for dotCMS.  If not, see <http://www.gnu.org/licenses/>
 *  
 * @author Graziano Aliberti - Engineering Ingegneria Informatica S.p.a
 *
 * Mar 7, 2013 - 4:18:15 PM
 */
public class PollsManAPIImpl implements PollsManAPI {

	private ContentletAPI conAPI;
	private Structure pollVote;
	private List<Permission> structurePermissions;
	private List<Category> categories;
	
	public PollsManAPIImpl(){		
		try {
			conAPI = APILocator.getContentletAPI();
			pollVote = StructureCache.getStructureByVelocityVarName(VOTE_STRUCTURE_NAME);
			structurePermissions = APILocator.getPermissionAPI().getPermissions(pollVote);
			categories = APILocator.getCategoryAPI().findTopLevelCategories( APILocator.getUserAPI().getSystemUser(), false );
		} catch (DotDataException e) {
			Logger.error(this, e.getMessage(),e);
		} catch (DotSecurityException e) {
			Logger.error(this, e.getMessage(),e);
		}
	}
	
	@Override
	public boolean hasVoted(String userId, String pollIdentifier, long languageId) throws DotContentletStateException, DotDataException, DotSecurityException {
		List<Contentlet> votes = conAPI.findByStructure(pollVote, APILocator.getUserAPI().getSystemUser(), true, 0, 0);
		for(Contentlet vote : votes){
			String _userId = (String)vote.get("user");
			String _pollIdentifier = (String)vote.get("poll");			
			if(userId.equals(_userId) && pollIdentifier.equals(_pollIdentifier))
				return true;
		}
		return false;
	}

	@Override
	public void vote(String userId, String pollIdentifier, String choiceIdentifier, User user) throws DotDataException, DotContentletValidationException, DotContentletStateException, IllegalArgumentException, DotSecurityException {
		Contentlet vote = new Contentlet();
		vote.setStructureInode(pollVote.getInode());
		vote.setStringProperty("user", userId);
		vote.setStringProperty("poll", pollIdentifier);
		vote.setStringProperty("choice", choiceIdentifier);
		vote.setBoolProperty("sent_to_sender", false);
		vote.setModDate(new GregorianCalendar().getTime());
		vote.setModUser(null!=user?user.getUserId():APILocator.getUserAPI().getAnonymousUser().getUserId());
		conAPI.validateContentlet(vote, categories);
		vote = APILocator.getContentletAPI().checkin(vote, categories, structurePermissions, null!=user?user:APILocator.getUserAPI().getAnonymousUser(), true);
		conAPI.publish(vote, null!=user?user:APILocator.getUserAPI().getAnonymousUser(), true);
	}

	@Override
	public int getTotalPollVotes(String pollIdentifier) throws DotDataException, DotSecurityException {
		List<Contentlet> votes = conAPI.findByStructure(pollVote, APILocator.getUserAPI().getSystemUser(), true, 0, 0);
		int count = 0;
		for(Contentlet vote: votes){
			String _votePollIdentifier = (String)vote.get("poll");
			if(_votePollIdentifier.equals(pollIdentifier))
				count++;
		}
		return count;
	}

	@Override
	public List<Contentlet> getChoiceByPoll(String pollIdentifier, long languageId) throws DotDataException, DotSecurityException {
		Contentlet poll = conAPI.findContentletByIdentifier(pollIdentifier, true, languageId, APILocator.getUserAPI().getSystemUser(), true);		
		return conAPI.getRelatedContent(poll, RelationshipFactory.getRelationshipByRelationTypeValue(RELATIONSHIP_NAME), APILocator.getUserAPI().getSystemUser(), true);
	}

	@Override
	public int getChoiceVotes(String pollIdentifier, String choiceIdentifier) throws DotDataException, DotSecurityException {
		List<Contentlet> votes = conAPI.findByStructure(pollVote, APILocator.getUserAPI().getSystemUser(), true, 0, 0);
		int count = 0;
		for(Contentlet vote: votes){
			String _votePollIdentifier = (String)vote.get("poll");
			String _voteChoiceIdentifier = (String)vote.get("choice");
			if(_votePollIdentifier.equals(pollIdentifier) && _voteChoiceIdentifier.equals(choiceIdentifier))
				count++;
		}
		return count;
	}
	
	
	
	

}
