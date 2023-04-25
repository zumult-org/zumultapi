/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts;

import org.zumult.io.*;
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
public class TestGermaNet {
    private static final String DATA_PATH = Configuration.getGermanetPath();
    private static final String TOKEN = "Baum";
    
    public static void main(String[] args) {
        new TestGermaNet().doit();
    }
    
    private void doit() {
        try {
            GermaNet germanet = new GermaNet(DATA_PATH);
            Set<String> synonyms = getSynonymsFromGermaNet(germanet, TOKEN, SimilarityTypeEnum.GERMANET);
            System.out.println("Synonyme für " + TOKEN + ": " + synonyms.toString());
        } catch (XMLStreamException |IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    
    private Set<String> getSynonymsFromGermaNet(GermaNet germanet, String str, SimilarityTypeEnum mode){
        Set<String> result = new HashSet();
        List <Synset> synsets = germanet.getSynsets(str);
            
        for (Synset synset : synsets){
            //add orth forms, compounds info and synonyms
            switch(mode){
               case GERMANET:
               case GERMANET_PLUS:
                    result.addAll(getOrthFormsAndCompoundsForSynset(synset, str));
                    
                    for (Synset otherSynset: synset.getRelatedSynsets(ConRel.has_hypernym)){
                        result.addAll(getOrthFormsForSynset(otherSynset));    
                    }
                   
                    for (Synset otherSynset: synset.getRelatedSynsets(ConRel.has_hyponym)){
                        result.addAll(getOrthFormsForSynset(otherSynset));    
                    }

                   break;
               case GERMANET_ORTH:
                   result.addAll(getOrthFormsForSynset(synset));
                   break;
               case GERMANET_COMPOUNDS:
                    result.addAll(getCompoundsForSynset(synset, str));
                    break;
               case GERMANET_HYPERNYM:
                   for (Synset otherSynset: synset.getRelatedSynsets(ConRel.has_hypernym)){
                        result.addAll(getOrthFormsForSynset(otherSynset));    
                    }
                   break;
               case GERMANET_HYPONYM:
                   for (Synset otherSynset: synset.getRelatedSynsets(ConRel.has_hyponym)){
                        result.addAll(getOrthFormsForSynset(otherSynset));    
                    }
                   break;
               default:
            }
            
            if (result.contains(str)){
                result.remove(str);
            }
        }
            
        return result;
    }
    
    private static Set<String> getOrthFormsForSynset(Synset synset){
        Set<String> result = new HashSet();
        List<LexUnit> lexical_units = synset.getLexUnits();

        for (LexUnit lexUnit: lexical_units){
            result.addAll(lexUnit.getOrthForms());
        }
        
        return result;
    }
    
    private static Set<String> getCompoundsForSynset(Synset synset, String word){
        Set<String> result = new HashSet();
        List<LexUnit> lexical_units = synset.getLexUnits();

        for (LexUnit lexUnit: lexical_units){
            List<String> orth_forms = lexUnit.getOrthForms();
            
            if(orth_forms.size()==1 && orth_forms.get(0).equals(word)){
                // add head of compounds
                if(lexUnit.getCompoundInfo()!=null){
                    result.add(lexUnit.getCompoundInfo().getHead());
                }
            }
        }
        
        return result;
    }
        
    private static Set<String> getOrthFormsAndCompoundsForSynset(Synset synset, String word){
        Set<String> result = new HashSet();
        List<LexUnit> lexical_units = synset.getLexUnits();

        for (LexUnit lexUnit: lexical_units){
            List<String> orth_forms = lexUnit.getOrthForms();
            
            result.addAll(orth_forms);
            
            if(orth_forms.size()==1 && orth_forms.get(0).equals(word)){
                // add head of compounds
                if(lexUnit.getCompoundInfo()!=null){
                    result.add(lexUnit.getCompoundInfo().getHead());
                }
            }
        }
        
        return result;
    }
    

}
