package com.eng.dotcms.polls.ajax;

import javax.servlet.http.HttpSession;
import uk.ltd.getahead.dwr.WebContextFactory;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.contentlet.business.DotContentletStateException;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.WebKeys;
import com.eng.dotcms.polls.PollsManFactory;
import com.eng.dotcms.polls.util.PollsUtil;
import com.liferay.counter.ejb.CounterManagerUtil;
import com.liferay.portal.model.User;

@SuppressWarnings("deprecation")
public class PollsManAjax {
		
	public PollsManAjax.VoteResult vote(String pollTitle, String pollIdentifier, String choiceIdentifier, boolean showVotes) {
		HttpSession session = WebContextFactory.get().getSession();
		String language = (String) session.getAttribute(com.dotmarketing.util.WebKeys.HTMLPAGE_LANGUAGE);
		User user = (User) session.getAttribute(WebKeys.CMS_USER);
		String htmlCode = "";
		String userId = null;
		if (user != null) { // is logged user
			userId = user.getUserId();
		} else { // is anonymous...increment the user id based on the Poll identifier 
			try {
				userId = Long.toString(CounterManagerUtil.increment(pollIdentifier+"."+choiceIdentifier+".anonymous"));
			} catch (Exception e) {
				Logger.error(this, e.getMessage(),e);
			}			
		}
		try {
			// check if the current user has already voted
			boolean hasVoted = PollsManFactory.getPollsManAPI().hasVoted(userId, pollIdentifier, Long.parseLong(language));
			
			//check also if there is a parameter in session for this user
			hasVoted = null!=session.getAttribute(pollTitle+"_voted");
			
			if(!hasVoted){ // if the user never votes this polls...vote it and set in session the vote
				PollsManFactory.getPollsManAPI().vote(userId, pollIdentifier, choiceIdentifier, user);
				session.setAttribute(pollTitle+"_voted",true);
			}
			if(showVotes){ //if the users want to see the results build the html				
				htmlCode = PollsUtil.getVotesHtmlCode(pollIdentifier, Long.parseLong(language));
			}
		} catch (DotContentletStateException e) {
			Logger.error(this, e.getMessage(),e);
		} catch (NumberFormatException e) {
			Logger.error(this, e.getMessage(),e);
		} catch (DotDataException e) {
			Logger.error(this, e.getMessage(),e);
		} catch (DotSecurityException e) {
			Logger.error(this, e.getMessage(),e);
		}
		return new VoteResult(htmlCode);
	}		
	
	public class VoteResult {
		
		public String htmlCode;
		
		public VoteResult(String htmlCode){
			this.htmlCode = htmlCode;
		}
		
		public String getHtmlCode() {
			return htmlCode;
		}

		public void setHtmlCode(String htmlCode) {
			this.htmlCode = htmlCode;
		}
	}
}
