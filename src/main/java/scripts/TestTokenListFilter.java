/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.io.XMLReader;
import org.zumult.objects.TokenFilter;
import org.zumult.objects.TokenList;
import org.zumult.objects.Transcript;
import org.zumult.objects.implementations.AndFilter;
import org.zumult.objects.implementations.NegatedFilter;
import org.zumult.objects.implementations.TokenListTokenFilter;

/**
 *
 * @author Thomas_Schmidt
 */
public class TestTokenListFilter {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new TestTokenListFilter().doit();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException | SAXException | ParserConfigurationException ex) {
            Logger.getLogger(TestTokenListFilter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(TestTokenListFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doit() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, SAXException, ParserConfigurationException, Exception {
        BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
        Transcript transcript1 = backendInterface.getTranscript("GWSS_E_00020_SE_01_T_01");
        System.out.println(transcript1.getNumberOfTokens());
        
        //Transcript transcript2 = backendInterface.getTranscript("FOLK_E_00001_SE_01_T_02");
        //System.out.println(transcript.toXML());
        /*TokenList posFilterTokenList = XMLReader.readTokenListFromInternalResource("/data/MEASURE_1_POS_FILTER.xml");
        TokenFilter posFilter = new NegatedFilter(new TokenListTokenFilter("lemma", posFilterTokenList));

        TokenList ngirrFilterTokenList = XMLReader.readTokenListFromInternalResource("/data/FOLK_NGIRR_OHNE_DEREWO.xml");
        TokenFilter ngirrFilter = new NegatedFilter(new TokenListTokenFilter("lemma", ngirrFilterTokenList));
        
        TokenFilter filter = new AndFilter(posFilter, ngirrFilter);*/

        /*TokenList tokenList1 = transcript1.getTokenList("lemma");
        TokenList tokenList2 = transcript2.getTokenList("lemma");
        TokenList tokenList = tokenList1.merge(tokenList2);
        IOUtilities.writeDocumentToLocalFile("F:\\Dropbox\\IDS\\ZuMult\\Wordlists\\FOLK_E_00001_SE_01_ZuMult_Wordlist.xml", IOUtilities.readDocumentFromString(tokenList.toXML()));
        
        TokenList tokenListDGD = XMLReader.readTokenListFromFile(new File("F:\\Dropbox\\IDS\\ZuMult\\Wordlists\\FOLK_E_00001_SE_01_DGD_Wordlist.xml"));
        TokenList difference = tokenList.remove(tokenListDGD);
        System.out.println(difference.toXML());*/
        
        TokenList unfilteredTokenList = XMLReader.readTokenListFromFile(new File("F:\\Dropbox\\IDS\\ZuMult\\Wordlists\\FOLK_E_00001_SE_01_ZuMult_Wordlist.xml"));
        
        // read the list with POS for filtering...
        TokenList posFilterTokenList = XMLReader.readTokenListFromInternalResource("/data/MEASURE_1_POS_FILTER.xml");
        // ... make a filter from it and negate it (since we want these POS to be excluded, not included)
        TokenFilter posFilter = new NegatedFilter(new TokenListTokenFilter("lemma", posFilterTokenList));
        
        // read the list with NGIRR tokens for filtering...
        TokenList ngirrFilterTokenList = XMLReader.readTokenListFromInternalResource("/data/FOLK_NGIRR_OHNE_DEREWO.xml");
        // ... make a filter from it and negate it (since we want these forms to be excluded, not included)
        TokenFilter ngirrFilter = new NegatedFilter(new TokenListTokenFilter("lemma", ngirrFilterTokenList));
        
        // combine the two filters into one
        TokenFilter filter = new AndFilter(posFilter, ngirrFilter);
        
        TokenList tokenList1 = transcript1.getTokenList("lemma", filter);
        /*TokenList tokenList2 = transcript2.getTokenList("lemma", filter);
        TokenList filteredTokenList = tokenList1.merge(tokenList2);
        
        TokenList manuallyFilteredTokenList = XMLReader.readTokenListFromFile(new File("F:\\Dropbox\\IDS\\ZuMult\\Wordlists\\FOLK_E_00001_WOLI.xml"));
        
        System.out.println("Unfiltered: " + unfilteredTokenList.getNumberOfTypes());
        System.out.println("Filtered: " + filteredTokenList.getNumberOfTypes());
        System.out.println("Manually filtered: " + manuallyFilteredTokenList.getNumberOfTypes());
        
        TokenList difference = filteredTokenList.remove(manuallyFilteredTokenList);
        System.out.println(difference.toXML());*/
        
        
        
        
    }
    
}
