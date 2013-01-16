<%@page import="com.dotmarketing.business.APILocator"%>
<%@page import="com.dotmarketing.business.Layout"%>
<%@page import="java.util.List"%>
<%@page import="com.liferay.portal.language.LanguageUtil"%>
<%@ include file="/html/common/init.jsp" %>

<style type="text/css">
#tools {
    text-align:center;
    width: 100%;
    margin: 0;
    display: block;
}
#links_table td {
    padding: 8px;
}

.expiredPoll {
	font-color: #DDDDDD;
	font-style: italic;	
	background-color: #E0E9F6;
}
</style>

<script type="text/javascript">
<%
String contentLayout="";
List<Layout> list=APILocator.getLayoutAPI().loadLayoutsForUser(user);
for(Layout ll : list) {
    for(String pid : ll.getPortletIds())
        if(pid.equals("EXT_11"))
            contentLayout=ll.getId();
}

int pageNumber=1;
if(request.getParameter("pageNumber")!=null) 
    pageNumber=Integer.parseInt(request.getParameter("pageNumber"));

%>
function movePage(x) {
	var cp=parseInt(dojo.byId('currentPage').textContent);
	dojo.byId('currentPage').textContent=cp+x;
	loadTable();
}

function disableButtons(x) {
	dijit.byId('refreshBtn').set('disabled',x);
	dijit.byId('runBtn').set('disabled',x);
	if(x=="") {
		var cp=parseInt(dojo.byId('currentPage').textContent);
		var tp=parseInt(dojo.byId('totalPages').textContent);
		if(cp>1)
			dijit.byId('prevBtn').set('disabled','');
		else
			dijit.byId('prevBtn').set('disabled','disabled');
		
		if(cp<tp)
			dijit.byId('nextBtn').set('disabled','');
		else
			dijit.byId('nextBtn').set('disabled','disabled');
	}
	else {
		dijit.byId('nextBtn').set('disabled',x);
	    dijit.byId('prevBtn').set('disabled',x);
	}
}

function goToAddPoll(){
	var dialog = new dijit.Dialog({
		id: 'addPoll',
        title: "<%= LanguageUtil.get(pageContext, "add-poll")%>",
        style: "width: 800px; ",
        content: new dojox.layout.ContentPane({
            href: "/html/plugins/com.eng.dotcms.polls/add_polls.jsp"
        }),
        onHide: function() {
        	var dialog=this;
        	setTimeout(function() {
        		dialog.destroyRecursive();
        	},200);
        },
        onLoad: function() {
        	
        }
    });
    dialog.show();	    
    dojo.style(dialog.domNode,'top','80px');
}

function goToViewPollVotes(pollId){
	var dialog = new dijit.Dialog({
		id: 'viewPollVotes',
        title: "<%= LanguageUtil.get(pageContext, "view-poll-votes")%>",
        style: "width: 800px; ",
        content: new dojox.layout.ContentPane({
            href: "/html/plugins/com.eng.dotcms.polls/view_poll_votes.jsp?pollId="+pollId
        }),
        onHide: function() {
        	var dialog=this;
        	setTimeout(function() {
        		dialog.destroyRecursive();
        	},200);
        },
        onLoad: function() {
        	
        }
    });
    dialog.show();	    
    dojo.style(dialog.domNode,'top','80px');
}

function backToPollsList(id, reload){
	dijit.byId(id).hide();
	if(reload)
		loadTable();
}

function loadTable() {
	var currentUser="<%=user.getUserId()%>";
	var lid="<%=contentLayout%>";
	var lidBL="<%=layout.getId()%>";	
	dojo.empty('table_body');
	var pageSize=10;
	var page=(parseInt(dojo.byId('currentPage').textContent)-1)*pageSize;
	dojo.xhr('GET',{
		url:'/DotAjaxDirector/com.eng.dotcms.polls.ajax.PollsAjaxAction/cmd/getPolls/offset/'+page+'/pageSize/'+pageSize,
		handleAs: 'json',
		load: function(data) {
			for(var i=0;i<data.list.length;i++) {
				var identifier=data.list[i].identifier;
				var title=data.list[i].title;
				var question=data.list[i].question;
				var moduser=data.list[i].user;
				var moddate=data.list[i].date;
				var expired=data.list[i].expired;
				
				if(expired=="true"){
					var row="<tr class=\"expiredPoll\">"+
					  "<td style=\"width: 5%\"><strong><%=LanguageUtil.get(pageContext, "Expired")%></strong></td>"+	
					  "<td style=\"width: 15%\">"+title+"</td>"+
			          "<td style=\"width: 55%\">"+question+"</td>"+
			          "<td style=\"width: 10%\">"+moduser+"</td>"+
			          "<td style=\"width: 13%\">"+moddate+"</td>"+
			          "<td style=\"width: 2%\"><a style=\"cursor: pointer\" onclick=\"goToViewPollVotes('"+identifier+"')\" title=\"<%=LanguageUtil.get(pageContext,"view-poll-votes")%>\"><span class='previewIcon'></span></a></td>"+
			         "</tr>";					
				}else{
					var row="<tr>"+ 
					  "<td style=\"width: 5%\"></td>"+
			          "<td style=\"width: 15%\">"+title+"</td>"+
			          "<td style=\"width: 55%\">"+question+"</td>"+
			          "<td style=\"width: 10%\">"+moduser+"</td>"+
			          "<td style=\"width: 13%\">"+moddate+"</td>"+
			          "<td style=\"width: 2%\"><a style=\"cursor: pointer\" onclick=\"goToViewPollVotes('"+identifier+"')\" title=\"<%=LanguageUtil.get(pageContext,"view-poll-votes")%>\"><span class='previewIcon'></span></a></td>"+
			         "</tr>";					
				}
				dojo.place(dojo.toDom(row),'table_body');				
			}
			dojo.byId('totalPages').textContent=Math.ceil(data.total/pageSize);
            disableButtons('');
		},
		error: function(err) {
			console.log(err);
		}
	});
}

function resized() {
    var viewport = dijit.getViewport();
    var viewport_height = viewport.h;
    
    var  e =  dojo.byId("borderContainer");
    dojo.style(e, "height", viewport_height -150+ "px");
    
    dijit.byId("borderContainer").resize();
}

dojo.ready(function(){
    dojo.connect(window,"onresize",resized);
    resized();
    loadTable();
});
</script>

<div class="portlet-wrapper">
	<div class="subNavCrumbTrail">
		<ul id="subNavCrumbUl">        
			<li><%=LanguageUtil.get(pageContext, "javax.portlet.title.EXT_POLLS_MANAGEMENT")%></li>
			<li class="lastCrumb"><span><%=LanguageUtil.get(pageContext, "javax.portlet.title.EXT_POLLS_MANAGEMENT_VIEW")%></span></li>
		</ul>
		<div class="clear"></div>
	</div>
	
	<div id="brokenLinkMain">
        <div id="borderContainer" dojoType="dijit.layout.BorderContainer" style="width:100%;">
            <div dojoType="dijit.layout.ContentPane" region="top">
              <span id="tools">
                <button id="addPollBtn" type="button" dojoType="dijit.form.Button" onClick="goToAddPoll()">
                   <span class="plusIcon"></span>
                   <%=LanguageUtil.get(pageContext,"add-poll")%>
                </button>
              </span>
            </div>
            <div dojoType="dijit.layout.ContentPane" region="center">
                <table id="links_table" class="listingTable" border=1>
                <thead>
                    <tr>
                    	<th style="width: 5%">Status</th>
                        <th style="width: 15%">Title</th>
                        <th style="width: 55%">Question</th>
                        <th style="width: 10%">Mod User</th>                         
                        <th style="width: 13%">Mod Date</th>
                        <th style="width: 2%">Action</th>                       
                    </tr>
                </thead>
                <tbody id="table_body">
                </tbody>
                </table>
            </div>
            <div dojoType="dijit.layout.ContentPane" region="bottom">
                <span id="tools">
                   <button id="prevBtn" type="button" dojoType="dijit.form.Button" onClick="movePage(-1)">
	                   <span class="previousIcon"></span>
	               </button>
	               
	               <span id="currentPage"><%=pageNumber %></span> / <span id="totalPages"></span>
	               
	               <button id="nextBtn" type="button" dojoType="dijit.form.Button" onClick="movePage(1)">
                       <span class="nextIcon"></span>
                   </button>
                </span>
            </div>
        </div>
    </div>
</div>