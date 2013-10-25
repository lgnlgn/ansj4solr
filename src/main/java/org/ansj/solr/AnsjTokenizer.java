package org.ansj.solr;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ansj.domain.Term;
import org.ansj.splitWord.Analysis;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;


public class AnsjTokenizer extends Tokenizer{

	private final static String PUNCTION = "。，！？；,!?;";
	public  static final String SPACES = " 　\t\r\n";

	private final int analysisType ; 
	//for sentences split
	private final StringBuilder buffer = new StringBuilder();
	private int tokenStart = 0, tokenEnd = 0;


	private CharTermAttribute termAtt;
	private OffsetAttribute offsetAtt;
	private TypeAttribute typeAtt;
	private PositionIncrementAttribute positionIncrementAtt;
	int lastOffset = 0;
	int endPosition =0; 
	private Iterator<Term> tokenIter;
	private List<Term> tokenBuffer;
	static
	{
		new ToAnalysis(new StringReader(""));
	}
	protected AnsjTokenizer(Reader input, int analysisType) {
		super(input);
		offsetAtt = addAttribute(OffsetAttribute.class);
		termAtt = addAttribute(CharTermAttribute.class);
		typeAtt = addAttribute(TypeAttribute.class);
		this.analysisType = analysisType;
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (tokenIter == null || !tokenIter.hasNext()){
			String currentSentence = checkSentences();
			if (currentSentence!= null){
				if (analysisType == 1){
					Analysis udf = new ToAnalysis(new StringReader(currentSentence));
					Term term = null ;
					tokenBuffer = new ArrayList<Term>();
					while((term=udf.next())!=null){
						tokenBuffer.add(term);
					}
				}else {
					tokenBuffer = IndexAnalysis.parse(currentSentence);
				}
				tokenIter = tokenBuffer.iterator();
				if (!tokenIter.hasNext()){
					return false;
				}
			} else {
				return false; // no more sentences, end of stream!
			}
		}
		clearAttributes();
		Term nextWord = tokenIter.next();
		termAtt.append(nextWord.getName());
		// ���ô�Ԫ����
		termAtt.setLength(nextWord.getOffe());
		
		int currentStart = tokenStart + nextWord.getOffe();
		int currentEnd = tokenStart + nextWord.getToValue();
		offsetAtt.setOffset(currentStart,currentEnd );
		typeAtt.setType("word");

		int pi = currentStart - lastOffset;
		if(nextWord.getOffe()  <= 0) {
			pi = 1;
		}
		positionIncrementAtt.setPositionIncrement( pi );
		lastOffset = currentStart;
		endPosition = currentEnd;
		return true;
	}



	private String checkSentences() throws IOException{
		buffer.setLength(0);
		int ci;
		char ch, pch;
		boolean atBegin = true;
		tokenStart = tokenEnd;
		ci = input.read();
		ch = (char) ci;

		while (true) {
			if (ci == -1) {
				break;
			} else if (PUNCTION.indexOf(ch) != -1) {
				// End of a sentence
				buffer.append(ch);
				tokenEnd++;
				break;
			} else if (atBegin && SPACES.indexOf(ch) != -1) {
				tokenStart++;
				tokenEnd++;
				ci = input.read();
				ch = (char) ci;
			} else {
				buffer.append(ch);
				atBegin = false;
				tokenEnd++;
				pch = ch;
				ci = input.read();
				ch = (char) ci;
				// Two spaces, such as CR, LF
				if (SPACES.indexOf(ch) != -1
						&& SPACES.indexOf(pch) != -1) {
					// buffer.append(ch);
					tokenEnd++;
					break;
				}
			}
		}
		if (buffer.length() == 0){
			//sentences finished~	
			return null; 
		}else {
			return buffer.toString();
		}

	}

	@Override
	public void reset() throws IOException {
		super.reset();
		tokenStart = tokenEnd = 0;
	}
	
	public final void end() {
		// set final offset
		int finalOffset = correctOffset(this.endPosition);
		offsetAtt.setOffset(finalOffset, finalOffset);
	}

}
