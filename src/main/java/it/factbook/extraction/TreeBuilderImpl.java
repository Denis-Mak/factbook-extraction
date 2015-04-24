package it.factbook.extraction;

import it.factbook.dictionary.Golem;
import it.factbook.dictionary.WordForm;
import it.factbook.dictionary.parse.TreeParser;
import it.factbook.dictionary.tree.PhraseTree;
import it.factbook.search.FactProcessor;
import it.factbook.search.SearchProfileUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 *
 */
@Component
public class TreeBuilderImpl implements TreeBuilder{
    private static final Logger log = LoggerFactory.getLogger(ProfileUpdaterMsgHandler.class);

    @Autowired
    private SearchProfileUpdater profileUpdater;

    @Autowired
    private TreeParser treeParser;

    public String getTreeAsString(Golem golem, String text){
        String treeStr = "";
        for (PhraseTree<WordForm> tree: treeParser.parse(golem, text)){
            treeStr += FactProcessor.printTree(tree);
            for (WordForm[] pair: profileUpdater.findRepresentativePairs(tree)){
                treeStr += "{" + pair[0].getWord() + ", " + pair[1].getWord() + "} -> " +
                        Arrays.toString(treeParser.mergeMems(golem, pair[0].getMem(), pair[1].getMem())) + "\n";
            }
            treeStr += "\n";
        }
        return treeStr;
    }
}
