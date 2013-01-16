package com.eng.dotcms.polls.util;

import java.text.NumberFormat;
import java.util.List;

import com.dotmarketing.beans.Host;
import com.dotmarketing.cache.FieldsCache;
import com.dotmarketing.cache.StructureCache;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotHibernateException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.structure.factories.FieldFactory;
import com.dotmarketing.portlets.structure.factories.RelationshipFactory;
import com.dotmarketing.portlets.structure.factories.StructureFactory;
import com.dotmarketing.portlets.structure.model.Field;
import com.dotmarketing.portlets.structure.model.Field.DataType;
import com.dotmarketing.portlets.structure.model.Relationship;
import com.dotmarketing.portlets.structure.model.Structure;
import com.dotmarketing.portlets.structure.model.Field.FieldType;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.RegEX;
import com.dotmarketing.util.VelocityUtil;
import com.eng.dotcms.polls.PollsDeployer;
import com.eng.dotcms.polls.PollsManAPI;
import com.eng.dotcms.polls.PollsManFactory;

public class PollsUtil {
	
	private static List<Structure> structures = StructureFactory.getStructures();
	private static List<Relationship> relationships = RelationshipFactory.getAllRelationships();
	
	public static Structure createStructure(String name, String description, Host host, int type) throws DotHibernateException {
		Structure aStructure = new Structure();
		aStructure.setName(name);
		aStructure.setHost(host.getIdentifier());
		aStructure.setDescription(description);
		aStructure.setStructureType(type);
		String structureVelocityName = VelocityUtil.convertToVelocityVariable(aStructure.getName(), true);
		List<String> velocityvarnames = StructureFactory.getAllVelocityVariablesNames();
		int found = 0;
		if (VelocityUtil.isNotAllowedVelocityVariableName(structureVelocityName)) {
			found++;
		}

		for (String velvar : velocityvarnames) {
			if (velvar != null) {
				if (structureVelocityName.equalsIgnoreCase(velvar)) {
					found++;
				} else if (velvar.toLowerCase().contains(structureVelocityName.toLowerCase())) {
					String number = velvar.substring(structureVelocityName.length());
					if (RegEX.contains(number, "^[0-9]+$")) {
						found++;
					}
				}
			}
		}
		if (found > 0) {
			structureVelocityName = structureVelocityName + Integer.toString(found);
		}
		aStructure.setVelocityVarName(structureVelocityName);
		
		StructureFactory.saveStructure(aStructure);
		StructureCache.removeStructure(aStructure);
		StructureCache.addStructure(aStructure);
		return aStructure;
	}
	
	/**
	 * Create a generic Field
	 * 
	 * Jan 14, 2013 - 12:16:41 PM
	 */
	public static void createField(String structureInode, String name, FieldType type, DataType dataType, boolean required, boolean searchable, boolean unique, boolean listed) throws DotHibernateException{
		Field aField = new Field();
		aField.setFieldName(name);
		aField.setFieldType(type.toString());
		aField.setSearchable(searchable);
		aField.setIndexed(searchable);
		aField.setUnique(unique);
		aField.setRequired(required);
		aField.setVelocityVarName(name.toLowerCase().replaceAll(" ", "_"));
		aField.setStructureInode(structureInode);
		aField.setListed(listed);
		if(DataType.BOOL.equals(dataType))
			aField.setValues("True|1\nFalse|0");
		aField.setFieldContentlet(FieldFactory.getNextAvaliableFieldNumber(dataType.toString(), "", structureInode));
		FieldFactory.saveField(aField);
		FieldsCache.removeField(aField);
		FieldsCache.addField(aField);
	}
	
	public static boolean existStructure(String structureName){
		for(Structure s : structures){
			if(s.getName().equals(structureName)){
				Logger.info(PollsDeployer.class, "The Structure "+structureName+" already exists");
				return true;
			}
		}
		return false;
	}
	
	public static String getStructureInode(String structureName) {
		for(Structure s : structures){
			if(s.getName().equals(structureName)){
				return s.getInode();
			}
		}
		return null;
	}
	
	public static boolean existRelationship(String relationshipName, Structure parentStructure){
		relationships = RelationshipFactory.getRelationshipsByParent(parentStructure);
		for(Relationship r : relationships){
			if(r.getRelationTypeValue().equals(relationshipName)){
				Logger.info(PollsDeployer.class, "The Relationship "+relationshipName+" already exists");
				return true;
			}
		}
		return false;
	}
	
	public static Relationship getRelationshipByParentAndName(Structure parent, String name){
		relationships = RelationshipFactory.getRelationshipsByParent(parent);
		for(Relationship r : relationships){
			if(r.getRelationTypeValue().equals(name)){
				return r;
			}
		}
		return null;
	}
	
	public static String getVotesHtmlCode(String pollIdentifier, long languageId) throws DotDataException, DotSecurityException{
		PollsManAPI pollsAPI = PollsManFactory.getPollsManAPI(); 
		StringBuilder htmlCodeBuilder = new StringBuilder();
		NumberFormat numberFormat = NumberFormat.getNumberInstance();
		NumberFormat percentFormat = NumberFormat.getPercentInstance();
		int totalVotes = pollsAPI.getTotalPollVotes(pollIdentifier);
		List<Contentlet> choices = pollsAPI.getChoiceByPoll(pollIdentifier, languageId); 
		htmlCodeBuilder.append("<div id='result" + pollIdentifier + "'>");
		htmlCodeBuilder.append("<table class='poll-result-table'>");
		htmlCodeBuilder.append("<thead>");
		htmlCodeBuilder.append("<tr>");
		htmlCodeBuilder.append("<th>");
		htmlCodeBuilder.append("Choice");
		htmlCodeBuilder.append("</th>");
		htmlCodeBuilder.append("<th>");
		htmlCodeBuilder.append("# Votes");
		htmlCodeBuilder.append("</th>");
		htmlCodeBuilder.append("<th>");
		htmlCodeBuilder.append("Percentage");
		htmlCodeBuilder.append("</th>");
		htmlCodeBuilder.append("</thead>");
		htmlCodeBuilder.append("<tbody>");
		for(Contentlet choice : choices){
			int choiceVotes = pollsAPI.getChoiceVotes(pollIdentifier, choice.getIdentifier());
			double votesPercent = 0.0;
			if (totalVotes > 0) {
				votesPercent = (double) choiceVotes / totalVotes;
			}
			htmlCodeBuilder.append("<tr>");
			htmlCodeBuilder.append("<td class=\"poll-choice\">");
			htmlCodeBuilder.append(choice.get("text").toString());
			htmlCodeBuilder.append("</td>");
			htmlCodeBuilder.append("<td class=\"poll-votes\">");
			htmlCodeBuilder.append(numberFormat.format(choiceVotes));
			htmlCodeBuilder.append("</td>");
			htmlCodeBuilder.append("<td class=\"poll-percents\">");
			htmlCodeBuilder.append(percentFormat.format(votesPercent));
			htmlCodeBuilder.append("</td>");			
			htmlCodeBuilder.append("</tr>");			
			i++;
		}
		htmlCodeBuilder.append("<tr>");
		htmlCodeBuilder.append("<td colspan=\"3\" class=\"poll-responses\">");
		if(totalVotes ==0){
			htmlCodeBuilder.append("No responses");
		} else{
			htmlCodeBuilder.append("Total: " + totalVotes + " responses");
		}			
		htmlCodeBuilder.append("</td>");
		htmlCodeBuilder.append("</tr>");
		htmlCodeBuilder.append("</tbody>");
		htmlCodeBuilder.append("</table>");
		htmlCodeBuilder.append("</div>");
		
		return htmlCodeBuilder.toString();
	}
	
}
