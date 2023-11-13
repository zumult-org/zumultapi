/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts.GermaNet;

import de.tuebingen.uni.sfs.germanet.api.ConRel;
import de.tuebingen.uni.sfs.germanet.api.GermaNet;
import de.tuebingen.uni.sfs.germanet.api.LexUnit;
import de.tuebingen.uni.sfs.germanet.api.Synset;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import org.zumult.backend.Configuration;
import org.zumult.query.searchEngine.Repetition.SimilarityTypeEnum;

/**
 *
 * @author Frick
 */
public final class TestGermaNet {

    /** GermaNet location, e.g. /home/zumult/GN_V170_XML */
    private static final String DATA_PATH = Configuration.getGermanetPath();

    /**
     * Main method.
     * Calls doit() to retrieve and print synonyms
     *
     * @param args The command line arguments
     */
    public static void main(final String[] args) {
        doit("Baum");
        doit("parken");
    }

    private TestGermaNet() {
      //not called
    }

    /**
    * Prints synonyms for the specified word token.
    *
    * @param str word token for which synonyms should be printed
    */
    private static void doit(final String str) {
        try {
            // initialize GermaNet
            GermaNet germanet = new GermaNet(DATA_PATH);

            // get synonyms
            Set<String> synonyms =
                    getSynonymsFromGermaNet(germanet,
                            str,
                            SimilarityTypeEnum.GERMANET);

            // print synonyms
            System.out.println("Synonyme für "  + str + ": "
                    + synonyms.toString());

        // if GermaNet can't be read
        } catch (IOException | XMLStreamException ex) {
            Logger.getLogger(TestGermaNet.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /**
    * Retrieves synonyms from GermaNet for the specified word token.
    *
    * @param germanet
    * @param str word token for which synonyms should be printed
    * @param mode
    *
    * @return a set of synonyms
    */
    private static Set<String> getSynonymsFromGermaNet(final GermaNet germanet,
                                                final String str,
                                                final SimilarityTypeEnum mode) {
        Set<String> result = new HashSet();
        List<Synset> synsets = germanet.getSynsets(str);

        for (Synset synset : synsets) {
            List<LexUnit> lexicalUnits = synset.getLexUnits();
            List<Synset> hypernyms =
                            synset.getRelatedSynsets(ConRel.has_hypernym);
            List<Synset> hyponyms =
                            synset.getRelatedSynsets(ConRel.has_hyponym);

            //add orth forms, compounds info and synonyms
            switch (mode) {
               case GERMANET, GERMANET_PLUS -> {
                   result.addAll(getOrthFormsAndCompounds(lexicalUnits,
                           str));

                   for (Synset otherSynset: hypernyms) {
                       List<LexUnit> lexUnits = otherSynset.getLexUnits();
                       result.addAll(getOrthForms(lexUnits));
                   }

                   for (Synset otherSynset: hyponyms) {
                       List<LexUnit> lexUnits = otherSynset.getLexUnits();
                       result.addAll(getOrthForms(lexUnits));
                   }
                }
               
               default -> {
                   throw new IllegalStateException("Unexpected value: " + mode);
                }
            }

            if (result.contains(str)) {
                result.remove(str);
            }
        }

        return result;
    }

    /**
     * Returns a set of orthographic forms for all lexical units.
     *
     * @param lexUnits
     * @return a set of strings
     */
    private static Set<String> getOrthForms(final List<LexUnit> lexUnits) {
        Set<String> result = new HashSet();

        for (LexUnit lexUnit: lexUnits) {
            result.addAll(lexUnit.getOrthForms());
        }

        return result;
    }

    /**
     * Returns a set of orthographic forms for all lexical units
     * and a set of compounds for the specified word.
     *
     * @param lexUnits
     * @param word
     * @return a set of strings
     */
    private static Set<String> getOrthFormsAndCompounds(
                                            final List<LexUnit> lexUnits,
                                            final String word) {
        Set<String> result = new HashSet();

        for (LexUnit lexUnit: lexUnits) {
            List<String> orthForms = lexUnit.getOrthForms();
            result.addAll(orthForms);

            if (orthForms.size() == 1
                && orthForms.get(0).equals(word)) {

                // add head of compounds
                if (lexUnit.getCompoundInfo() != null) {
                    result.add(lexUnit.getCompoundInfo().getHead());
                }
            }
        }

        return result;
    }

}
