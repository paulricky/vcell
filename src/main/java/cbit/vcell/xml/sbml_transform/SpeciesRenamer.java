/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

/**
 * 
 */
package cbit.vcell.xml.sbml_transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vcell.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * @author mlevin
 *
 */
class SpeciesRenamer extends ASbmlTransformer {
	public static final String Name = "speciesRenamePattern";

	private List<Pair<Pattern, String> > speciePatternIds = null;
	
	/** Map of ids - old to new
	 */
	private Map<String, String> mapIds = null;
	
	/** New specie ids */
	private Set<String> idsNew = null;
	


	/**
	 * @return a list of pairs of strings that can be used as regular expression 
	 * match-replace pairs for creating new specie IDs based on specie names 
	 * generated by BMGL
	 */
	public static List<String[] > getDefaultSpecieRenames() {
		List<String[] > list = new ArrayList<String[] >();
		
		list.add(new String[] {"\\.", "_"});
		list.add(new String[] {"[\\(,][a-zA-Z]\\w*", ""});
		list.add(new String[] {"~|!\\d*", ""});
		list.add(new String[] {"\\(\\)", ""});
		list.add(new String[] {"\\)", ""});
		
		return list;
	}
	

	public SpeciesRenamer() {}
	
	/** Map the proposed specie id changes
	 * @param doc
	 */
	private void mapChanges(Document doc) {
		if( null == speciePatternIds ) return;
		mapIds = new HashMap<String, String>();
		idsNew = new HashSet<String>();
		
		NodeList nl = doc.getElementsByTagName(SbmlElements.Species_tag);
		for( int i = 0, max = nl.getLength(); i < max; ++i ) {
			Element e = (Element) nl.item(i);
			String id = e.getAttribute(SbmlElements.Id_attrib);
			String name = e.getAttribute(SbmlElements.Name_attrib);
			reNameSpecie(id, name);
		}
	}
	
	private void reNameSpecie(String id, String name) {
		for( int ip = 0, maxP = speciePatternIds.size(); ip < maxP; ++ip ) {
			try {
			Pair<Pattern, String> pair = speciePatternIds.get(ip);
			Matcher matcher = pair.one.matcher(name);
			name = matcher.replaceAll(pair.two);
			} catch( Exception e) {
				throw new SbmlTransformException(getErrorMessage(ip), e);
			}
		}
		
		//assure uniqueness
		String idNew = name;
		for( int n = 0; ! idsNew.add(idNew); idNew = name + (++n) ) {}
		
		mapIds.put(id, idNew);
	}
	
	/** Adds a rule for creating new SBML specie id based on specie name
	 * @param parameters two strings expected; 
	 * first is regex pattern that will be applied to SBML species name
	 * second is replacement string 
	 */
	public void addTransformation(String[] parameters, String comment) {
		super.storeTransformationInfo(parameters, comment);
		if( speciePatternIds == null ) {
			speciePatternIds = new ArrayList<Pair<Pattern, String> >();
		}
		speciePatternIds.add(new Pair<Pattern, String>(Pattern.compile(parameters[0]), parameters[1]) );
	}
	
	public int countParameters() {	return 2;}
	
	public void transform(Document doc) {
		if( null == speciePatternIds ) return;
		try {
			mapChanges(doc);
		} catch(SbmlTransformException e) {throw e;
		
		} catch(Exception e) {
			String msg = "error mapping name changes";
			throw new Exceptn(msg, e);
		}
		NodeList nl;

		try {
			//change specie ids
			nl = doc.getElementsByTagName(SbmlElements.Species_tag);
			for( int i = 0, max = nl.getLength(); i < max; ++i ) {
				Element e = (Element) nl.item(i);
				String id = e.getAttribute(SbmlElements.Id_attrib);
				String idNew = mapIds.get(id);
				e.setAttribute(SbmlElements.Id_attrib, idNew);
			}
		} catch(Exception e) {
			String msg = "error changing specie ids";
			throw new Exceptn(msg, e);
		}

		try {
			//change specie references
			nl = doc.getElementsByTagName(SbmlElements.SpRef_tag);
			for( int i = 0, max = nl.getLength(); i < max; ++i ) {
				Element e = (Element) nl.item(i);
				String id = e.getAttribute(SbmlElements.Species_attr);
				String idNew = mapIds.get(id);
				e.setAttribute(SbmlElements.Species_attr, idNew);
			}
		} catch(Exception e) {
			String msg = "error changing specie references";
			throw new Exceptn(msg, e);
		}

		try {
			//change ci elements
			nl = doc.getElementsByTagName(SbmlElements.CiMath_tag);
			for( int i = 0, max = nl.getLength(); i < max; ++i ) {
				Element e = (Element) nl.item(i);
				String ci = e.getTextContent().replace(" ", "");
				String ciNew = mapIds.get(ci);
				if( null != ciNew ) {
					e.setTextContent(ciNew); //it might not be a specie but a constant
				}
			}
		} catch(Exception e) {
			String msg = "error changing specie references in math expressions";
			throw new Exceptn(msg, e);
		}


	}


	public int countTransformations() {
		return speciePatternIds.size();
	}


	public String[] getTransformation(int i) {
		if( i >= 0 || i < speciePatternIds.size() ) {
			Pair<Pattern, String> p = speciePatternIds.get(i);
			return new String[] {p.one.pattern(), p.two};
		}
		return new String[] {"",""};
	}


	public void removeTransformation(int i) {
		speciePatternIds.remove(i);
	}
	


	private static class Exceptn extends SbmlTransformException {
		private static final long serialVersionUID = 3014914496661351019L;
		private static final String messageDefault = "error renaiming species";
		
		public Exceptn() {
			super(messageDefault);
		}

		public Exceptn(String message, Throwable cause) {
			super(message, cause);
		}

		public Exceptn(String message) {
			super(message);
		}

		public Exceptn(Throwable cause) {
			super(messageDefault, cause);
		}
		
		
	}



	public String getName() {return Name;}


}