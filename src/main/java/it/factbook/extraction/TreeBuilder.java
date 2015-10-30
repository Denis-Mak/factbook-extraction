package it.factbook.extraction;

import it.factbook.dictionary.Golem;

/**
 * Interface of the remote procedure call registered using Spring-AMQP.
 * Can be called externally like this:
 * <pre>
 * public class RPC client {
 *     {@literal @}Autowired
 *      private TreeBuilder treeBuilder;
 *
 *     public static void main(String[] args){
 *         System.out.println(treeBuilder.getTreeAsString(golem, text));
 *     }
 * }
 * </pre>
 */
public interface TreeBuilder {

    String getTreeAsString(Golem golem, String text);

}
