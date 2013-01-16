package com.eng.dotcms.polls.velocity;

import java.util.ArrayList;
import java.util.List;
import org.apache.velocity.tools.view.tools.ViewTool;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.cache.StructureCache;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.util.Logger;
import com.eng.dotcms.polls.util.PollsUtil;

@SuppressWarnings("deprecation")
public class PollsViewTool implements ViewTool {
	
	public Poll getPollByTitle(String title) throws DotDataException, DotSecurityException{
		Poll result = new Poll();
		List<PollChoice> choicesList = new ArrayList<PollChoice>();
		List<Contentlet> polls = APILocator.getContentletAPI().findByStructure(StructureCache.getStructureByName("Poll"), APILocator.getUserAPI().getSystemUser() , false, 0, 0);
		for(Contentlet poll : polls){
			String _title = (String)poll.getMap().get("title");
			if(_title.equals(title)){
				String expired = (String)poll.getMap().get("expired");
				if(!Boolean.parseBoolean(expired)){
					result.setIdentifier(poll.getIdentifier());
					result.setQuestion(new StringBuilder((String)poll.getMap().get("question")));
					List<Contentlet> choices = APILocator.getContentletAPI().getRelatedContent(poll, PollsUtil.getRelationshipByParentAndName(poll.getStructure(), "Parent_Poll-Child_PollChoice"), APILocator.getUserAPI().getSystemUser(), true);
					for(Contentlet c : choices){
						PollChoice aChoice = new PollChoice();
						aChoice.setIdentifier(c.getIdentifier());
						aChoice.setText((String)c.getMap().get("text"));
						choicesList.add(aChoice);
					}
					result.setChoices(choicesList);
					return result;					
				}else{
					result.setExpired(true);
					return result;
				}
			}
		}
		result.setExpired(true);
		return result;
	}
	
	public String getVotesHtmlCode(String pollIdentifier, String language) {
		String htmlCode = "";
		try {
			htmlCode = PollsUtil.getVotesHtmlCode(pollIdentifier, Long.parseLong(language));
		} catch (NumberFormatException e) {
			Logger.error(this, e.getMessage(),e);
		} catch (DotDataException e) {
			Logger.error(this, e.getMessage(),e);
		} catch (DotSecurityException e) {
			Logger.error(this, e.getMessage(),e);
		}
		return htmlCode;
	}
	
	@Override
	public void init(Object initData) {
		// TODO Auto-generated method stub

	}

}
