package xlong.nlp.parser;


public abstract class Parser {
	Parser father;
	public Parser(Parser father) {
		this.father = father;
	}
	
	public String parse(String text) {
		if (father == null) {
			return myParse(text);
		} else {
			return myParse(father.parse(text));
		}
	}
	
	abstract protected String myParse(String text);
}
