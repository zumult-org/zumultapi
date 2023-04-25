/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.indexing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Elena
 */
public class OutputLanguageListAsJson implements Indexer {
    
    String[] CORPORA = {"GWSS"};
    String DATA_PATH = "src\\java\\data\\";
    ObjectMapper mapper = new ObjectMapper();
    

    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new OutputLanguageListAsJson().index();
        } catch (IOException ex) {
            Logger.getLogger(OutputLanguageListAsJson.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
   @Override
    public void index() throws IOException {
        try {
            long start = System.currentTimeMillis();
            
            for (String corpusID : CORPORA) {
                String corpusJsonPath = DATA_PATH + "prototypeJson/" + corpusID + ".json";

                JSONArray jsonArray = new JSONArray();

                JSONParser jsonParser = new JSONParser();
                FileReader reader = new FileReader(corpusJsonPath);
                JSONArray corpusJsonObject = (JSONArray) jsonParser.parse(reader);
                
                Map<String, Integer> langMap = new HashMap();

                // iterate per speechevent
                corpusJsonObject.forEach(speechEvent -> {
                    JSONObject speechEventObject = (JSONObject) speechEvent;
                    System.out.println(speechEventObject.get("id") + " *******************************");
                    String lang = (String) speechEventObject.get("sprachen");

                    if (langMap.containsKey(lang)){
                        int n = langMap.get(lang);
                        langMap.put(lang, n+1);
                    }else{
                        langMap.put(lang, 1);
                    }
         
                });
                
                System.out.println(langMap);
                
                for (String langObj : langMap.keySet()){
                    JSONObject newLangObj = new JSONObject();
                    newLangObj.put("id", langObj);
                    if (langObj.equals("Deutsch (L1)")){
                        newLangObj.put("label", "Deutsch als L1");
                    }else if(langObj.equals("Deutsch (L2)")){
                        newLangObj.put("label", "Deutsch als L2");
                    }else if(langObj.equals("Deutsch (L2) ; Deutsch (L1)")){
                        newLangObj.put("label", "Deutsch als L1 und L2");
                    }else if(langObj.startsWith("Polnisch")){
                        newLangObj.put("label", "Polnisch");
                    }else if(langObj.startsWith("Italienisch")){
                        newLangObj.put("label", "Italienisch");
                    }else if(langObj.equals("Englisch (L1)")){
                        newLangObj.put("label", "Englisch als L1");
                    }else if(langObj.equals("Englisch (L2)")){
                        newLangObj.put("label", "Englisch als L2");
                    }else if(langObj.equals("Englisch (L1) ; Englisch (L2)")){
                        newLangObj.put("label", "Englisch als L1 und L2");
                    }
                    newLangObj.put("count", langMap.get(langObj));
              
                    jsonArray.add(newLangObj);
                    
                }
                // nice print
                System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonArray));

//              write to json
                FileWriter file = new FileWriter(DATA_PATH + "prototypeJson/lang" + corpusID + ".json");
                file.write(jsonArray.toString());
                file.flush();
            }

            long end = System.currentTimeMillis();
            DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss:sss");
            Date timeNeeded = new Date(end - start);
            System.out.println("time needed: " + dateFormat.format(timeNeeded));
        } catch (ParseException | JsonProcessingException ex) {
            Logger.getLogger(OutputLanguageListAsJson.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
