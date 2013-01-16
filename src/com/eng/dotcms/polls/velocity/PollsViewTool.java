package com.eng.dotcms.polls.velocity;

import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.tools.view.tools.ViewTool;

import com.dotmarketing.business.APILocator;
import com.dotmarketing.cache.StructureCache;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.eng.dotcms.polls.util.PollsUtil;

public class PollsViewTool implements ViewTool {
	
	@SuppressWarnings("deprecation")
	public Poll getPollByTitle(String title) throws DotDataException, DotSecurityException{
		Poll result = new Poll();
		List<PollChoice> choicesList = new ArrayList<PollChoice>();
		List<Contentlet> polls = APILocator.getContentletAPI().findByStructure(StructureCache.getStructureByName("Poll"), APILocator.getUserAPI().getSystemUser() , false, 0, 0);
		for(Contentlet poll : polls){
			if(poll.getMap().get("title").equals(title)){
				String expired = (String)poll.getMap().get("expired");
				if(!Boolean.parseBoolean(expired)){
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
				}else
					return null;
			}
		}
		return null;		
	}
	
	
	@Override
	public void init(Object initData) {
		// TODO Auto-generated method stub

	}

}
