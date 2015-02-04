/**
 * Project : Classify Urls
 */
package xlong.urlclassify.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;

import xlong.urlclassify.data.IO.UrlTypePairIO;
import xlong.urlclassify.data.filter.EntityFilter;
import xlong.util.MyWriter;
import xlong.wm.ontology.OntologyTree;

/**
 * Class for merging information of external links and entity types. A simple
 * entity can contain a list of Urls or a list of types The combine method can
 * merge two simple entity.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 */

public class Entity implements Comparable<Entity>{

	private static final String mySpliter = " |-| ";
	private static final String mySpliterReg = " \\|-\\| ";
	private static final String typeStart = "http://dbpedia.org/ontology/";
	
	/** The name of a entity */
	protected String name;

	/** The list of Urls */
	protected ArrayList<String> urls;

	/** The list of types */
	protected ArrayList<String> types;


	/**
	 * Constructor
	 * 
	 * @param name
	 *            the name of the entity.
	 */
	public Entity(String name) {
		this.name = name;
		urls = new ArrayList<String>();
		types = new ArrayList<String>();
	}
	
	public int cntTypes() {
		return types.size();
	}
	
	public int cntUrls() {
		return urls.size();
	}

	/**
	 * Returns the name of the entity.
	 * 
	 * @return the name of the entity.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the list of Urls of the entity.
	 * 
	 * @return the list of Urls of the entity.
	 */
	public ArrayList<String> getUrls() {
		return urls;
	}

	/**
	 * Returns the list of types of the entity.
	 * 
	 * @return the list of types of the entity.
	 */
	public ArrayList<String> getTypes() {
		return types;
	}

	/**
	 * Add a Url into ULRs list
	 * @param url the Url need to be added.
	 */
	public void addUrl(String url) {
		urls.add(url);
	}

	/**
	 * Add a type into types list.
	 * @param typethe type need to be added.
	 */
	public void addType(String type) {
		if (type.startsWith(typeStart)){
			types.add(type.substring(typeStart.length()));
		}
	}
	
	public void addTypeName(String type) {
		types.add(type);
	}

	
	/**
	 * Filter types. Exclude types don't start with
	 * 'http://dbpedia.org/ontology/'. If type A is subclass of type B, then
	 * exclude type B.
	 * 
	 * @param ontology
	 *            the subclass of relationship map.
	 * @return success or not.
	 */
	public void filterTypes(OntologyTree ontology) {
		HashSet<String> dels = new HashSet<String>();
		if (ontology != null) {
			Map<String, TreeSet<String>> ancestorsMap = ontology.getAncestorsMap();
			for (String type : types) {
				if (ancestorsMap.containsKey(type)) {
					dels.addAll(ancestorsMap.get(type));
				}
			}
		}
		HashSet<String> newTypes = new HashSet<String>();
		for (String type : types) {
			if (!dels.contains(type)) {
				newTypes.add(type);
			}
		}
		types = new ArrayList<String>(newTypes);
	}
	
	public void filterUrls() {
		HashSet<String> newUrls = new HashSet<String>();
		for (String url : urls) {
			newUrls.add(url);
		}
		urls = new ArrayList<String>(newUrls);
	}
	
	public static Collection<Entity> generateEntities(ArrayList<String[]> types, ArrayList<String[]> urls, OntologyTree ontology) {
		HashMap<String, Entity> entityMap = new HashMap<String, Entity>();
		for (String[] ss:types){
			if (!entityMap.containsKey(ss[0])) {
				entityMap.put(ss[0], new Entity(ss[0]));
			}
			entityMap.get(ss[0]).addType(ss[1]);
		}

		for (String[] ss:urls){
			if (!entityMap.containsKey(ss[0])) {
				entityMap.put(ss[0], new Entity(ss[0]));
			}
			entityMap.get(ss[0]).addUrl(ss[1]);
		}
		
		for (Entity en:entityMap.values()){
			en.filterTypes(ontology);
			en.filterUrls();
		}
		return entityMap.values();
	}
	
	public static Collection<Entity> generateEntities(String typesFile, String urlsFile, OntologyTree ontology) throws IOException {
		HashMap<String, Entity> entityMap = new HashMap<String, Entity>();
		UrlTypePairIO typeIO = new UrlTypePairIO(typesFile);
		String[] ss;
		while ((ss = typeIO.next()) != null){
			if (!entityMap.containsKey(ss[0])) {
				entityMap.put(ss[0], new Entity(ss[0]));
			}
			entityMap.get(ss[0]).addType(ss[1]);
		}
		typeIO.close();
		

		UrlTypePairIO urlIO = new UrlTypePairIO(urlsFile);
		while ((ss = urlIO.next()) != null){
			if (!entityMap.containsKey(ss[0])) {
				entityMap.put(ss[0], new Entity(ss[0]));
			}
			entityMap.get(ss[0]).addUrl(ss[1]);
		}
		
		for (Entity en:entityMap.values()){
			en.filterTypes(ontology);
			en.filterUrls();
		}	
		urlIO.close();
		
		return entityMap.values();
	}
	
	public static Collection<Entity> generateEntities(String types, String urls) throws IOException {
		return generateEntities(types, urls, null);
	}
	
	public static Collection<Entity> generateEntities(ArrayList<String[]> types, ArrayList<String[]> urls) {
		return generateEntities(types, urls, null);
	}
	
	public static ArrayList<Entity> filtEntities(Collection<Entity> entities, EntityFilter filter) {
		ArrayList<Entity> newEntities = new ArrayList<Entity>();
		for (Entity en:entities) {
			if (filter.filter(en)) {
				newEntities.add(en);
			}
		}
		return newEntities;
	}
	
	public static ArrayList<String[]> entity2UrlTypePairs(Entity entity) {
		ArrayList<String[]> pairs = new ArrayList<String[]>();
		Collection<String> urls = entity.getUrls();
		Collection<String> types = entity.getTypes();
		if (urls == null || types == null) {
			return pairs;
		}
		for (String url:urls) {
			for (String type:types) {
				pairs.add(new String[] {url, type});
			}
		}
		return pairs;
	}
	
	public static ArrayList<String[]> entities2UrlTypePairs(Collection<Entity> entities) {
		ArrayList<String[]> pairs = new ArrayList<String[]>();
		for (Entity en:entities) {
			pairs.addAll(entity2UrlTypePairs(en));
		}
		return pairs;
	}
	
	public static HashMap<String, HashSet<String>> entities2TypeMap(Collection<Entity> entities) {
		HashMap<String, HashSet<String>> typeMap = new HashMap<String, HashSet<String>>();
		for (Entity entity:entities) {
			Collection<String> urls = entity.getUrls();
			Collection<String> types = entity.getTypes();		
			if (urls == null || types == null) {
				continue;
			}
			for (String type:types) {
				if (!typeMap.containsKey(type)) {
					typeMap.put(type, new HashSet<String>());
				}	
				HashSet<String> typedUrls = typeMap.get(type);
				typedUrls.addAll(urls);
			}
		}
		return typeMap;
	}
	
	public static HashMap<String, TreeSet<String>> entities2UrlMap(Collection<Entity> entities){
		HashMap<String, TreeSet<String>> urlMap = new HashMap<String, TreeSet<String>>();
		//int cnt = 0;
		for (Entity entity:entities) {
			Collection<String> urls = entity.getUrls();
			Collection<String> types = entity.getTypes();
			
//			cnt ++;
//			System.out.println(cnt + ": " + entity.name + " " + urls.size() + " " + types.size());
			if (urls == null || types == null) {
				continue;
			}
			for (String url:urls) {
				//System.out.println(url);
				if (!urlMap.containsKey(url)) {
					urlMap.put(url, new TreeSet<String>());
				}	
				urlMap.get(url).addAll(types);
			}
		}	
		return urlMap;
	}
	
	public static HashMap<String, TreeSet<Entity>> entities2UrlEntityMap(Collection<Entity> entities){
		HashMap<String, TreeSet<Entity>> urlMap = new HashMap<String, TreeSet<Entity>>();
		//int cnt = 0;
		for (Entity entity:entities) {
			Collection<String> urls = entity.getUrls();
			Collection<String> types = entity.getTypes();
			
//			cnt ++;
//			System.out.println(cnt + ": " + entity.name + " " + urls.size() + " " + types.size());
			if (urls == null || types == null) {
				continue;
			}
			for (String url:urls) {
				//System.out.println(url);
				if (!urlMap.containsKey(url)) {
					urlMap.put(url, new TreeSet<Entity>());
				}	
				urlMap.get(url).add(entity);
			}
		}	
		return urlMap;
	}

	/**
	 * Gets if this entity is equals to another entity or not.
	 * 
	 * @param b
	 *            entity to compare with.
	 * @return this entity equals to entity b or not.
	 */
	public boolean equals(Entity b) {
		if (name.equals(b.name))
			return true;
		else
			return false;
	}
	
	public static void write(Collection<Entity>entities, String filePath){
		//int cnt = 0;
		MyWriter.setFile(filePath, false);
		for (Entity en:entities){
			MyWriter.write(en.toString());
			//cnt ++;
			//if (cnt>100) break;
		}
		MyWriter.close();
	}
	
	public static Collection<Entity> read(String filePath) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(filePath));
		ArrayList<Entity> entities = new ArrayList<Entity>();
		
		String line;
		while((line = in.readLine()) != null) {
			Entity entity = new Entity(line);
			line = in.readLine();
			if (line == null) break;
			String[] urls = line.split(mySpliterReg);
			for (String url:urls){
				entity.addUrl(url);
			}
			line = in.readLine();
			if (line == null) break;
			String[] types = line.split(mySpliterReg);
			for (String type:types){
				entity.addTypeName(type);
			}
			entities.add(entity);
		}
		
		in.close();
		return entities;
	}

	/**
	 * To string method.
	 */
	@Override
	public String toString() {
		String s = name + "\n";
		boolean first;
		first = true;
		for (String url : urls) {
			if (!first) {
				s += mySpliter;
			} else {
				first = false;
			}
			s = s + url;
		}
		s = s + "\n";
		first = true;
		for (String type : types) {
			if (!first) {
				s += mySpliter;
			} else {
				first = false;
			}
			s = s + type;
		}
		s = s + "\n";
		return s;
	}

	@Override
	public int compareTo(Entity o) {
		return this.name.compareTo(o.name);
	}
}
