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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author josip.batinic
 */
public class OutputThemenListAsJson implements Indexer {
    String[] CORPORA = {"FOLK", "GWSS"};
    String DATA_PATH = "src\\java\\data\\";
    ObjectMapper mapper = new ObjectMapper();
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new OutputThemenListAsJson().index();
        } catch (IOException ex) {
            Logger.getLogger(OutputThemenListAsJson.class.getName()).log(Level.SEVERE, null, ex);
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

                // iterate per speechevent
                corpusJsonObject.forEach(speechEvent -> {
                    JSONObject speechEventObject = (JSONObject) speechEvent;
                    System.out.println(speechEventObject.get("id") + " *******************************");
                    JSONArray themes = (JSONArray) speechEventObject.get("themen");
                    
                    for (Object themeObj : themes) {
                        String theme = ((String) themeObj).trim();
                        System.out.println("theme: " + theme);
                        JSONObject newThemeObj = new JSONObject();
                        newThemeObj.put("id", theme);
                        newThemeObj.put("label", theme);
                        if (!jsonArray.contains(newThemeObj)) {
                            jsonArray.add(newThemeObj);
                        }
                    }                    
                });
                // nice print
                System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonArray));

//              write to json
                FileWriter file = new FileWriter(DATA_PATH + "prototypeJson/themen" + corpusID + ".json");
                file.write(jsonArray.toString());
                file.flush();
            }

            long end = System.currentTimeMillis();
            DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss:sss");
            Date timeNeeded = new Date(end - start);
            System.out.println("time needed: " + dateFormat.format(timeNeeded));
        } catch (ParseException ex) {
            Logger.getLogger(OutputThemenListAsJson.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(OutputThemenListAsJson.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
