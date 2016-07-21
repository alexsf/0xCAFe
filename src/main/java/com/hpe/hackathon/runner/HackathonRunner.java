package com.hpe.hackathon.runner;

import java.io.IOException;
import java.io.InputStream;

import com.hpe.hackathon.stanford.nlp.*;

import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.util.InvalidFormatException;

public class HackathonRunner {

    public static void main(String[] args) {
       try {
           //new HackathonRunner().parseOpenNlp();
           new HackathonRunner().parseStanfordNlp();
       }
       catch(Exception e) {
           e.printStackTrace();
       }

    }
    
    public  void parseOpenNlp() throws InvalidFormatException, IOException {
        InputStream is = getClass().getResourceAsStream("/en-parser-chunking.bin");
        ParserModel model = new ParserModel(is);
        Parser parser = ParserFactory.create(model);
        
        String sentence = "This camera has great zoom and resolution";// I never had such a smooth ice cream.";
        //String sentence = "The cow jumped over the moon";
        Parse topParses[] = ParserTool.parseLine(sentence, parser, 5);
        
        //StringBuffer sb = new StringBuffer();
        for (Parse p : topParses) {
            //p.showCodeTree();
            p.show();
            System.out.println(p.getProb());
        }
        
        is.close();
        // (TOP (S (NP (DT This) (NN ice) (NN cream)) (VP (VBZ is) (ADJP (JJ smooth) (CC and) (JJ delicious)))))
        
    }
    
    public void parseStanfordNlp() {
        Extract extract = new Extract();
        java.util.List<Pattern> patterns = extract.run("Cafe is conveniently located on Pier 39 in San Fransisco and offers a great view of the bay along with a good hearty breakfast. I had heard some mixed things about Cafe but I wanted to see for myself so I went Sunday morning before my flight home. The restaurant itself is very clean and as expected for being in the middle of a big tourist location it is pretty busy. My meal consisted of banana and pecan french toast, country potatoes, fruit and 2 eggs over easy. The food was good but not great, my biggest complaint was the syrup was very thin and not super flavorful and the french toast was a little dry. 3 stars for A ok (not bad)");
        //patterns = new Postprocess().run(patterns);
        for (Pattern pattern : patterns) {
            System.out.println(pattern.toAspect());
        }
    }

}
