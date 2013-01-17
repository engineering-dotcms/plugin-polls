package com.eng.dotcms.polls.ajax;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

import com.dotmarketing.beans.Permission;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.web.WebAPILocator;
import com.dotmarketing.cache.StructureCache;
import com.dotmarketing.portlets.categories.model.Category;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.servlets.ajax.AjaxAction;
import com.dotmarketing.util.Logger;
import com.eng.dotcms.polls.util.PollsUtil;
import com.liferay.portal.model.User;
import static com.eng.dotcms.polls.util.PollsConstants.*;

public class PollsAjaxAction extends AjaxAction {
	
	@SuppressWarnings("rawtypes")
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String cmd = getURIParams().get("cmd");
        java.lang.reflect.Method meth = null;
        Class partypes[] = new Class[] { HttpServletRequest.class, HttpServletResponse.class };
        Object arglist[] = new Object[] { request, response };
        try {
            if (getUser() == null ) {
                response.sendError(401);
                return;
            }
            meth = this.getClass().getMethod(cmd, partypes);
            meth.invoke(this, arglist);
        } catch (Exception e) {
            Logger.error(this.getClass(), "Trying to run method:" + cmd);
            Logger.error(this.getClass(), e.getMessage(), e.getCause());
            throw new RuntimeException(e.getMessage(),e);
        }
    }
	
	@SuppressWarnings({ "rawtypes"})
    public void getPolls(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String,String> pmap=getURIParams();
        int offset=Integer.parseInt(pmap.get("offset"));
        int pageSize=Integer.parseInt(pmap.get("pageSize"));
        
        Map<String,Object> result=new HashMap<String,Object>();
        List<Map> list=new ArrayList<Map>();
        SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd hh:mm");
        try {
        	List<Contentlet> polls = APILocator.getContentletAPI().findByStructure(StructureCache.getStructureByVelocityVarName(POLL_STRUCTURE_NAME), WebAPILocator.getUserWebAPI().getLoggedInUser(request), false, pageSize, offset);
            for(Contentlet poll : polls) {
                User modUser=APILocator.getUserAPI().loadUserById(poll.getModUser());                
                Map<String,String> mm=new HashMap<String,String>();
                mm.put("inode", poll.getInode());
                mm.put("identifier", poll.getIdentifier());
                mm.put("title", poll.getTitle());
                mm.put("question", (String)poll.getMap().get("question"));
                mm.put("date", df.format(poll.getModDate()));
                mm.put("expired", (String)poll.getMap().get("expired"));
                mm.put("user", modUser.getFullName()+"<"+modUser.getEmailAddress()+">");
                list.add(mm);
            }
                            
            result.put("list", list);
            result.put("total", list.size());
            
            response.setContentType("application/json");
            new ObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValue(response.getOutputStream(), result);
            
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

	public void addPoll(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {        	
			String language = (String) request.getSession().getAttribute(com.dotmarketing.util.WebKeys.HTMLPAGE_LANGUAGE);
			User user = WebAPILocator.getUserWebAPI().getLoggedInUser(request);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 'T'hh:mm:ss");
			String expireDate = request.getParameter("pollExpireDate");
			String expireTime = request.getParameter("pollExpireTime");
			Date expirationDate =  sdf.parse(expireDate + " " + expireTime);
			GregorianCalendar gc = new GregorianCalendar();
			if(expirationDate.after(gc.getTime())){
				Contentlet poll = new Contentlet();
				List<Category> categories = APILocator.getCategoryAPI().findTopLevelCategories( APILocator.getUserAPI().getSystemUser(), false );
				List<Permission> structurePermissions = APILocator.getPermissionAPI().getPermissions(StructureCache.getStructureByVelocityVarName(POLL_STRUCTURE_NAME));				
				poll.setStructureInode(StructureCache.getStructureByVelocityVarName(POLL_STRUCTURE_NAME).getInode());
				poll.setStringProperty("title", request.getParameter("pollTitle"));
				poll.setStringProperty("question", request.getParameter("pollQuestion"));
				poll.setDateProperty("expiration_date",expirationDate);
				poll.setStringProperty("expired", "false");
				poll.setHost(WebAPILocator.getHostWebAPI().getCurrentHost(request).getIdentifier());
				poll.setLanguageId(Long.parseLong(language));
				poll.setModUser(user.getUserId());
				poll.setModDate(new GregorianCalendar().getTime());
				
				//add choices
				String _choices = request.getParameter("pollChoice");
				String[] choices = _choices.split("[|]");
				List<Contentlet> contentRelationships = new ArrayList<Contentlet>();
				List<Contentlet> contentSavedRelationships = new ArrayList<Contentlet>();
				for(String c:choices){
					Contentlet choice = new Contentlet();
					choice.setStructureInode(StructureCache.getStructureByVelocityVarName(CHOICE_STRUCTURE_NAME).getInode());
					choice.setStringProperty("id", UUID.randomUUID().toString());
					choice.setStringProperty("text", c);
					choice.setHost(WebAPILocator.getHostWebAPI().getCurrentHost(request).getIdentifier());
					choice.setLanguageId(Long.parseLong(language));
					choice.setModUser(user.getUserId());
					choice.setModDate(new GregorianCalendar().getTime());				
					// add relationship
					contentRelationships.add(choice);
				}
				
				// save all choice
				structurePermissions = APILocator.getPermissionAPI().getPermissions(StructureCache.getStructureByVelocityVarName(CHOICE_STRUCTURE_NAME));
				
				for(Contentlet c : contentRelationships){
					APILocator.getContentletAPI().validateContentlet( c, categories );
				}
				
				for(Contentlet c : contentRelationships){
					c = APILocator.getContentletAPI().checkin(c, categories, structurePermissions, user, true);
					APILocator.getContentletAPI().publish(c, user, true);
					contentSavedRelationships.add(c);
				}
				
				// save poll
				APILocator.getContentletAPI().validateContentlet( poll, categories );
				poll = APILocator.getContentletAPI().checkin( poll, categories, structurePermissions, user, true );
				APILocator.getContentletAPI().publish(poll, user, true);
				// relate the contents
				APILocator.getContentletAPI().relateContent( poll, PollsUtil.getRelationshipByParentAndName(poll.getStructure(), "Parent_Poll-Child_PollChoice"), contentSavedRelationships, user, false );
			}else
				throw new Exception("The expiration date must be after the actual time.");
		} catch (Exception e) {
			response.getWriter().println("FAILURE: " + e.getMessage());
		}
		
    }
	
	@Override
	public void action(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

}
