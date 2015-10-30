package it.factbook.extraction;

import it.factbook.dictionary.Golem;
import it.factbook.dictionary.WordForm;
import it.factbook.dictionary.repository.StemAdapter;
import it.factbook.dictionary.tree.Tree;
import it.factbook.dictionary.tree.TreeParser;
import it.factbook.search.SearchProfileUpdater;
import it.factbook.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Implementation of TreeBuilder using TreeParser and StemAdapter from factbook-dictionary
 */
@Component
public class TreeBuilderImpl implements TreeBuilder{
    private static final Logger log = LoggerFactory.getLogger(ProfileUpdaterMsgHandler.class);

    @Autowired
    private SearchProfileUpdater profileUpdater;

    @Autowired
    private TreeParser treeParser;

    @Autowired
    private StemAdapter stemAdapter;

    /**
     * Parses the provided text returns printable version of syntax tree with selected representative idioms.
     *
     * @param golem dictionary to parse the text
     * @param text the text to parse
     * @return string contains syntax trees formatted to print
     */
    public String getTreeAsString(Golem golem, String text){
        String treeStr = "";
        long startParsing = System.nanoTime();
        log.debug("Start parsing phrase {}", text);
        for (Tree<WordForm> tree: treeParser.parse(golem, text)){
            treeStr += tree.toString();
            for (WordForm[] pair: profileUpdater.findRepresentativePairs(tree)){
                int[] mergedMem = stemAdapter.mergeMems(golem, pair[0].getMem(), pair[1].getMem());
                treeStr += "{" + pair[0].getWord() + ", " + pair[1].getWord() + "} -> " +
                        Arrays.toString(mergedMem) + "\n";
            }
            treeStr += "\n";
        }
        log.debug("Parsing time {} msec, phrase {}", Utils.durationFormatted(startParsing),text);
        return treeStr;
    }
}
