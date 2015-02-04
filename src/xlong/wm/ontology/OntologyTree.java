package xlong.wm.ontology;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * 
 * @author longx
 *
 */
public final class OntologyTree implements Comparable<OntologyTree>  {
	/** */
	private static String typeStart = "http://dbpedia.org/ontology/";
	/** */
	private final String name;
	/** */
	private final TreeSet<OntologyTree> sons;
	/** */
	private final TreeSet<OntologyTree> parents;
	/** */
	private static TreeSet<String> types;
	/** */
	private static final Map<String, TreeSet<String>> ancestorsMap = new TreeMap<String, TreeSet<String>>();
	
	static {
		ancestorsMap.put("root", new TreeSet<String>());
	}
	
	/**
	 * @param name name
	 */
	private OntologyTree(final String name) {
		this.name = name;
		this.parents = new TreeSet<OntologyTree>();
		this.sons = new TreeSet<OntologyTree>();
	}

	/**
	 * @param typeStartPar the typeStart to set
	 */
	public void setTypeStart(final String typeStartPar) {
		typeStart = typeStartPar;
	}

	/**
	 * 
	 * @param owlPath the path of .owl file
	 * @return the ontologyTree root
	 * @throws IOException IOException
	 */
	public static OntologyTree getTree(final String owlPath) throws IOException {
		 Map<String, HashSet<String>> subClassOfMap = getSubClassOf(owlPath);
		 return getTree(subClassOfMap);
	}

	/**
	 * @param subClassOfMap subClassOfMap
	 */
	public static void calTypes(final Map<String, HashSet<String>> subClassOfMap) {
		types = new TreeSet<String>();
		for (Entry<String, HashSet<String>> en : subClassOfMap.entrySet()) {
			types.add(en.getKey());
			for (String type:en.getValue()) {
				types.add(type);
			}
		}
	}
	
	// include name.
	public TreeSet<String> getPath(String name) {
		TreeSet<String> names = new TreeSet<String> (ancestorsMap.get(name));
		names.add(name);
		return names;
	}
	
	public boolean isAncestor(String name, String ancestor) {
		return ancestorsMap.get(name).contains(ancestor);
	}
	
	public Map<String, TreeSet<String>> getAncestorsMap () {
		return ancestorsMap;
	}

	/**

	 * @param subClassOfMap subClassOfMap
	 * @return ontologyTree
	 */
	public static OntologyTree getTree(final Map<String, HashSet<String>> subClassOfMap) {
		calTypes(subClassOfMap);
		OntologyTree root = new OntologyTree("root");
		Map<String, HashSet<String>> parents = new HashMap<String, HashSet<String>>();
		for (String type:types) {
			parents.put(type, new HashSet<String>());
			parents.get(type).add("root");
		}
		for (Entry<String, HashSet<String>> en : subClassOfMap.entrySet()) {
			parents.get(en.getKey()).addAll(en.getValue());
		}
		HashSet<String> oldAdded = new HashSet<String>();
		oldAdded.add("root");
		HashSet<String> newAdded = new HashSet<String>();
		newAdded.add("root");		
		HashSet<String> oldEdge = new HashSet<String>();
		oldEdge.add("root");
		HashSet<String> newEdge = new HashSet<String>();
		Map<String, OntologyTree> htMap = new HashMap<String, OntologyTree>();
		htMap.put("root", root);
		
		while (true) {
			int cnt = 0;
			for (String type:types) {
				if ((!oldAdded.contains(type)) && oldAdded.containsAll(parents.get(type))) {
					cnt++;
					OntologyTree son = new OntologyTree(type);
					htMap.put(type, son);
					ArrayList<String> dirs = new ArrayList<String>(oldEdge);
					dirs.retainAll(parents.get(type));
					for (String dir:dirs) {
						htMap.get(dir).addSon(son);
					}
					newAdded.add(type);
					newEdge.add(type);
				}
			}
			if (cnt == 0) {
				break;
			}
			oldEdge = newEdge;
			newEdge = new HashSet<String>();
			oldAdded = newAdded;
			newAdded = new HashSet<String>(oldAdded);
		}
		
		return root;
	}

	
	/**
	 * Read subclasof relationship from DBpedia ontology owl file.
	 * @param owlPath
	 *            ontology owl file path.
	 * @return the subclassof relationship map.
	 * @throws IOException IOException
	 */
	public static Map<String, HashSet<String>> getSubClassOf(final String owlPath)
			throws IOException {
		final Model dbpedia = ModelFactory.createOntologyModel();
		dbpedia.read(new FileInputStream(owlPath), "");
		final StmtIterator stmts = dbpedia.listStatements(null,
				RDFS.subClassOf, (RDFNode) null);
		Map<String, HashSet<String>> subClassMap = new HashMap<String, HashSet<String>>();

		while (stmts.hasNext()) {
			final Statement stmt = stmts.next();
			String sub = stmt.getSubject().toString().trim();
			String obj = stmt.getObject().toString().trim();
			if (sub.startsWith(typeStart) && obj.equals("http://www.w3.org/2002/07/owl#Thing")) {
				sub = sub.substring(typeStart.length());
				if (sub.length() != 0) {
					if (!subClassMap.containsKey(sub)) {
						subClassMap.put(sub, new HashSet<String>());
					}
				}				
			}
			if (!sub.equals(obj) && sub.startsWith(typeStart) && obj.startsWith(typeStart)) {
				sub = sub.substring(typeStart.length());
				obj = obj.substring(typeStart.length());
				if (sub.length() != 0 && obj.length() != 0) {
					if (!subClassMap.containsKey(sub)) {
						subClassMap.put(sub, new HashSet<String>());
					}
					subClassMap.get(sub).add(obj);
				}
			}
		}
		return subClassMap;
	}
	
	/**
	 * @param son the son
	 */
	public void addSon(final OntologyTree son) {
		sons.add(son);
		son.parents.add(this);
		
		TreeSet<String> ancestors = ancestorsMap.get(son.getTypeName());
		if (ancestors == null) {
			ancestors = new TreeSet<String>();
			ancestorsMap.put(son.getTypeName(), ancestors);
		}
		ancestors.add(this.getTypeName());
		ancestors.addAll(ancestorsMap.get(this.getTypeName()));
	}

	/**
	 * @return the name
	 */
	public String getTypeName() {
		return name;
	}
	
	/**
	 * 
	 * @return sons
	 */
	public TreeSet<OntologyTree> getSons() {
		return sons;
	}
		
	public OntologyTree toFlatTree() {
		OntologyTree tree = new OntologyTree("root");
		for (String type:types) {
			OntologyTree subTree = new OntologyTree(type);
			tree.addSon(subTree);
		}
		return tree;
	}
	
	public Collection<String> getSubTreeNames() {
		HashSet<String> nameSet = new HashSet<String>();
		nameSet.add(name);
		for (OntologyTree subtree:sons) {
			nameSet.addAll(subtree.getSubTreeNames());
		}
		return nameSet;
	}
	
	/**
	 * 
	 * @param level level
	 * @return String
	 */
	private String toString(final int level) {
		String str = "";
		for (int i = 0; i < level; i++) {
			str += "    ";
		}
		str += name + "\n";
		for (OntologyTree son:sons) {
			str += son.toString(level + 1);
		}
		return str;
	}
	
	public static TreeSet<String> getTypes() {
		return types;
	}
	
	@Override
	public String toString() {
		return toString(0);
	} 
	
	@Override
	public int compareTo(final OntologyTree o) {
		return this.name.compareTo(o.name);
	}
}
