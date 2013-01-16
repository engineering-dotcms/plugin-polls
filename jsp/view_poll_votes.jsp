<%@page import="com.eng.dotcms.polls.util.PollsUtil"%>
<%@ include file="/html/portlet/ext/contentlet/publishing/init.jsp" %>
<%@ page import="com.liferay.portal.language.LanguageUtil"%>



<style>
	.poll-result-table{width:99%;border:1px solid #d0d0d0;border-collapse:collapse;margin:0 auto;font-size:11px;}
	.poll-result-table tr{}
	.poll-result-table th, .poll-result-table td{padding:5px 8px;border:1px solid #d0d0d0;border-top: none;}
	.poll-result-table th{font-weight:bold;background:#ececec;}
	.poll-result-table td a{text-decoration:none;}
	.poll-result-table td table tr{border:none;}
	.poll-result-table td button, .poll-result-table table.dijitSelect, .poll-result-table td .dijitTextBox{font-size:12px;}
	
	.poll-responses { font-weight: bold;}
</style>

<div style="margin:auto;">
		<%
			String pollId = request.getParameter("pollId");
			String languageId = (String)session.getAttribute("com.dotmarketing.htmlpage.language");
		%>
		<br />
		<%=PollsUtil.getVotesHtmlCode(pollId, Long.parseLong(languageId)) %>
		<br />
		<table align="center">
			<tr>
				<td colspan="2" class="buttonRow" style="text-align: center;white-space: nowrap;">
					<button dojoType="dijit.form.Button" onClick="backToPollsList('viewPollVotes',false)" id="close" iconClass="cancelIcon"><%= LanguageUtil.get(pageContext, "Close") %></button>
			    </td>
		    </tr>
	   </table>	
	</div>
</div>