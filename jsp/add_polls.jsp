<%@ include file="/html/portlet/ext/contentlet/publishing/init.jsp" %>
<%@ page import="com.liferay.portal.language.LanguageUtil"%>

<script type="text/javascript">
	require(["dojo/parser", "dijit/form/SimpleTextarea"]);
	dojo.require("dotcms.dijit.form.HostFolderFilteringSelect");
	
	function cleanFields() {
		dojo.forEach(dijit.byId('formSavePoll').getDescendants(), function(formWidget) {
		    formWidget.attr('value', null);
		    
		});
		dijit.byId("save").setAttribute('disabled',false);
	}
	
	function savePoll(){
		var form = dijit.byId("formSavePoll");
		var closeBtn = dijit.byId("closeSave");
		var divResponse = dojo.byId("response");
		dijit.byId("save").setAttribute('disabled',true);
		dijit.byId("closeSave").setAttribute('disabled',true);
		dijit.byId("cancelSave").setAttribute('disabled',true);
		dijit.byId("pollTitle").setAttribute('required',true);
		dijit.byId("pollQuestion").setAttribute('required',true);
		dijit.byId("pollChoice").setAttribute('required',true);
		
		if (form.validate()) {
			var xhrArgs = {
				url: "/DotAjaxDirector/com.eng.dotcms.polls.ajax.PollsAjaxAction/cmd/addPoll",
				form: dojo.byId("formSavePoll"),
				handleAs: "text",
				load: function(data){
					if(data.indexOf("FAILURE") > -1){
						dojo.style(divResponse, {color:'#CC3333'});
						divResponse.innerHTML = '<%= LanguageUtil.get(pageContext, "not-saved-poll") %><br />'+data
						dijit.byId("save").setAttribute('disabled',false);
						dijit.byId("closeSave").setAttribute('disabled',false);
						dijit.byId("cancelSave").setAttribute('disabled',false);
					}
					else{
						dojo.style(divResponse, {color:'#009933'});
						divResponse.innerHTML = '<%= LanguageUtil.get(pageContext, "saved-poll") %>'
						dijit.byId("closeSave").setAttribute('disabled',false);
						dijit.byId("cancelSave").setAttribute('disabled',false);						
					}
				},
				error: function(error) {
					dojo.style(divResponse, {color:'#CC3333'});
					divResponse.innerHTML = '<%= LanguageUtil.get(pageContext, "not-saved-poll") %><br />'+error
					dijit.byId("save").setAttribute('disabled',false);
					dijit.byId("closeSave").setAttribute('disabled',false);
					dijit.byId("cancelSave").setAttribute('disabled',false);					
				}
			}
			
			dojo.style(divResponse, {color:'#FFCC33'});
			divResponse.innerHTML = '<%= LanguageUtil.get(pageContext, "saving-poll") %>'
			var deferred = dojo.xhrPost(xhrArgs);				
		}

	}
	
	function enableSave(){
		var pollTitle = dojo.byId("pollTitle").getValue();

		if(pollTitle && pollTitle.length > 0){
			dijit.byId("save").setAttribute('disabled',false);	
		}else{			
			dijit.byId("save").setAttribute('disabled',true);	
		}
	}
	
	function updateHostFolderValues(field){
	  if(!isInodeSet(dijit.byId('HostSelector').attr('value'))){
		 dojo.byId(field).value = "";
		 dojo.byId('hostId').value = "";
		 dojo.byId('folderInode').value = "";
	  }else{
		 var data = dijit.byId('HostSelector').attr('selectedItem');
		 if(data["type"]== "host"){
			dojo.byId(field).value =  dijit.byId('HostSelector').attr('value');
			dojo.byId('hostId').value =  dijit.byId('HostSelector').attr('value');
			dojo.byId('folderInode').value = "";
		 }else if(data["type"]== "folder"){
			dojo.byId(field).value =  dijit.byId('HostSelector').attr('value');
			dojo.byId('folderInode').value =  dijit.byId('HostSelector').attr('value');
			dojo.byId('hostId').value = "";
		}
	  }
	}
	
</script>

<style>
	.myTable {margin:20px;padding:10px;}
	.myTable tr td{padding:5px;vertical-align: top;}
</style>

<div style="margin:auto;">
	<i><%= LanguageUtil.get(pageContext, "Create-Poll-Description") %>.</i><br />
	<div id="response" style="font-weight: bold; margin-top: 5px; margin-bottom: 5px;"></div>
	<div dojoType="dijit.form.Form"  name="formSavePoll"  id="formSavePoll" onsubmit="return false;">
		<table class="myTable" border=0 style="margin: auto" align="center">
			<tr>
				<td align="right" width="40%">
					<%= LanguageUtil.get(pageContext, "Title") %>:
				</td>
				<td>
					<input type="text" dojoType="dijit.form.TextBox" 
							  name="pollTitle" 
							  id="pollTitle" 
							  style="width:300px;"
							  value="" 
							  required="true" onblur="enableSave()"/>
				</td>
			</tr>			
					
			<tr>
				<td align="right">
					<%= LanguageUtil.get(pageContext, "Question") %>:
				</td>
				<td>						          	
					<textarea dojoType="dijit.form.SimpleTextarea" name="pollQuestion" id="pollQuestion" style="width:400px;height:190px;" required="true"></textarea>
				</td>		
			</tr>	
			<tr>
				<td align="right">
					<%= LanguageUtil.get(pageContext, "expiration-date") %>:
				</td>
				<td>						          	
					<input type="text"
		                value=now"
		                dojoType="dijit.form.DateTextBox"
		                name="pollExpireDate"
		                id="pollExpireDate"
		                style="width:120px;" required="true">
		                
					<input type="text" name="pollExpireTime" id="pollExpireTime" value="now" style="width:100px;"
					  data-dojo-type="dijit.form.TimeTextBox"					  
					  required="true" />
				</td>		
			</tr>	
			
			<tr>
				<td align="right">
					<%= LanguageUtil.get(pageContext, "path") %>:
				</td>
				<td>						          	
					<div id="HostSelector" dojoType="dotcms.dijit.form.HostFolderFilteringSelect" onChange="updateHostFolderValues('path');"
			            value=""></div>
		                
					<input type="hidden" name="path" id="path" value=""/>
			     	<input type="hidden" name="hostId" id="hostId" value=""/>
			     	<input type="hidden" name="folderInode" id="folderInode" value=""/>
				</td>	
			</tr>	
													
			<tr>
				<td align="right" width="40%">
					<%= LanguageUtil.get(pageContext, "choice-text") %>:
				</td>
				<td>						          	
					<textarea dojoType="dijit.form.SimpleTextarea" name="pollChoice" id="pollChoice" style="width:400px;height:105px;" required="true"></textarea>
				</td>		
			</tr>
		</table>
		
		<table align="center">
			<tr>
				<td colspan="2" class="buttonRow" style="text-align: center;white-space: nowrap;">
					<button dojoType="dijit.form.Button" type="submit" id="save" iconClass="saveIcon"  onclick="savePoll()"><%= LanguageUtil.get(pageContext, "Save") %></button>
					&nbsp;
					<button dojoType="dijit.form.Button" onClick="cleanFields()" id="cancelSave" iconClass="cancelIcon"><%= LanguageUtil.get(pageContext, "Cancel") %></button>
					&nbsp;
					<button dojoType="dijit.form.Button" onClick="backToPollsList('addPoll',true)" id="closeSave" iconClass="closeIcon"><%= LanguageUtil.get(pageContext, "Close") %></button>
					
			    </td>
		    </tr>
	   </table>	
	</div>
</div>