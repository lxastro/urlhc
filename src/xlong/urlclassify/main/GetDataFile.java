package xlong.urlclassify.main;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;

import xlong.urlclassify.data.Entity;
import xlong.urlclassify.data.IO.NTripleReader;
import xlong.urlclassify.data.IO.UrlEntityMapIO;
import xlong.urlclassify.data.IO.UrlMapIO;
import xlong.urlclassify.data.filter.ExistUrlFilter;
import xlong.urlclassify.data.filter.UrlMapFilter;
import xlong.urlclassify.data.processer.SimplifyProcesser;
import xlong.urlclassify.data.processer.Triple2PairProcesser;
import xlong.urlclassify.data.processer.UrlNormalizeProcesser;
import xlong.wm.ontology.OntologyTree;

public class GetDataFile {
	
	public static void main(String[] args) throws Exception{
		
		if (args.length != 4) {
			throw new Exception("Error! Args should be: typeFile urlFile ontologyFile resultDir");
		}
	
		String typeFile = args[0]; //"E:\\longx\\data\\instance_types_en.nt";
		String urlFile = args[1]; //"E:\\longx\\data\\external_links_en.nt";
		String ontologyFile = args[2]; //"E:\\longx\\data\\dbpedia_2014.owl";
		String resultDir = args[3]; //"result";
		
		Files.createDirectories(FileSystems.getDefault().getPath(resultDir));
		
		String typePairFile = resultDir + "/typePair.txt";
		String urlPairFile = resultDir + "/urlPair.txt";
		String entityFile = resultDir + "/entities.txt";
		String overlapFile = resultDir + "/overlap.txt";
		String UrlMapFile = resultDir + "/UrlMap.txt";
		
		Collection<Entity> entities;
		HashMap<String, TreeSet<String>> urlMap;
		HashMap<String, TreeSet<Entity>> urlEntityMap;
		OntologyTree tree = OntologyTree.getTree(ontologyFile);
		
		// Read data file.
		NTripleReader typeReader = new NTripleReader(typeFile
				, new SimplifyProcesser(new UrlNormalizeProcesser(new Triple2PairProcesser())));
		NTripleReader urlReader = new NTripleReader(urlFile
				, new SimplifyProcesser(new UrlNormalizeProcesser(new Triple2PairProcesser())));
		typeReader.readAll(typePairFile);
		urlReader.readAll(urlPairFile);

		// Generate Entities.
		System.out.println("Generate Entities");
		// entities = Entity.generateEntities(typePairFile, urlPairFile);
		entities = Entity.generateEntities(typePairFile, urlPairFile, tree);
		System.out.println(entities.size());
		entities = Entity.filtEntities(entities, new ExistUrlFilter(new xlong.urlclassify.data.filter.SingleTypeFilter()));
		// entities = Entity.filtEntities(entities, new ExistUrlFilter(new xlong.data.filter.MultipleTypeFilter()));
		System.out.println(entities.size());
		Entity.write(entities, entityFile);	
		entities = null;
		entities = Entity.read(entityFile);
		System.out.println(entities.size());
		
		// Get UrlEntity map. For test.
		urlEntityMap = Entity.entities2UrlEntityMap(entities);
		UrlEntityMapIO.writeOverlapUrl(urlEntityMap, overlapFile);
		
		// Get url map.
		System.out.println("Get URL Map");
		urlMap =  Entity.entities2UrlMap(entities);
		entities = null;
		System.out.println(urlMap.size());
		urlMap = UrlMapFilter.filterUrlMap(urlMap);
		System.out.println(urlMap.size());
		UrlMapIO.write(urlMap, UrlMapFile);
	}
}
