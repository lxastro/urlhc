package xlong.nlp.tokenizer;

import java.io.Serializable;
import java.util.List;

public abstract class Tokenizer implements Serializable {

	private static final long serialVersionUID = 5550979689329232540L;
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
