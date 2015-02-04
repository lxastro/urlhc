package xlong.nlp.tokenizer;

import java.util.List;

public abstract class Tokenizer {
	Tokenizer father;
	public Tokenizer(Tokenizer father) {
		this.father = father;
	}
	abstract public List<String> myTokenize(String text);
	
	public List<String> tokenize(String text) {
		if (father == null) {
			return myTokenize(text);
		} else {
			List<String> words = myTokenize(text);
			words.addAll(father.tokenize(text));
			return words;
		}
	}
}
