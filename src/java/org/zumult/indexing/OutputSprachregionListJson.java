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
public class OutputSprachregionListJson implements Indexer {
    String[] CORPORA = {"FOLK", "GWSS"};
//    String CORPUS_ID = "FOLK";
//    String CORPUS_ID = "GWSS";
    String DATA_PATH = "src\\java\\data\\";
//    String IDLISTS_PATH = DATA_PATH + "IDLists\\";
//    XPath xPath = XPathFactory.newInstance().newXPath();
    ObjectMapper mapper = new ObjectMapper();
    

    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new OutputSprachregionListJson().index();
        } catch (IOException ex) {
            Logger.getLogger(OutputSprachregionListJson.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void index() throws IOException {
        try {
            // get stratifikation stuff
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
                    // get the metadata
                    JSONObject geo = (JSONObject) speechEventObject.get("geo");
                    String land = (String) geo.get("land");
                    String regionLameli = (String) geo.get("dialektalregion_lameli");
                    String regionWiesinger = (String) geo.get("dialektalregion_wiesinger");
                    
                    System.out.println("land:\t" + land);
                    System.out.println("regionLameli:\t" + regionLameli);
                    System.out.println("regionWiesinger:\t" + regionWiesinger);

                    if (land != null) {
                        JSONObject wiesingerObject = new JSONObject();
                        wiesingerObject.put("id", land + "__" + regionLameli + "__" + regionWiesinger);
                        wiesingerObject.put("label", regionWiesinger);
                        wiesingerObject.put("count", "1");
                        JSONArray wiesingerArray = new JSONArray();
                        wiesingerArray.add(wiesingerObject);

                        JSONObject lameliObject = new JSONObject();
                        lameliObject.put("id", land + "__" + regionLameli);
                        lameliObject.put("label", regionLameli);
                        lameliObject.put("count", "1");
                        lameliObject.put("children", wiesingerArray);
                        JSONArray lameliArray = new JSONArray();
                        lameliArray.add(lameliObject);

                        JSONObject landObject = new JSONObject();
                        landObject.put("id", land);
                        landObject.put("label", land);
                        landObject.put("count", "1");
                        
                        if (regionLameli != null) landObject.put("children", lameliArray);

                        if (jsonArray.isEmpty()) {
                            System.out.println("--> jsonArray Empty");
                            jsonArray.add(landObject);
                        } else { // jsonArray is not empty
                            System.out.println("--> jsonArray not empty");
                            boolean containsLand = false; // check if domain already in the json array
                            int landFoundIndex = 0;
                            for (int j = 0; j < jsonArray.size(); j++) {
                                JSONObject landJsonObject = (JSONObject) jsonArray.get(j);
                                System.out.println("--" + landJsonObject.get("id"));
                                if (landJsonObject.containsValue(land)) {
                                    containsLand = true;
                                    landFoundIndex = j;
                                    break;
                                }
                            }

                            if (!containsLand) { // jsonArray is not empty and it does not contain lameli
                                System.out.println("--> jsonArray not empty, and does not contain land");
                                jsonArray.add(landObject);
                            } else { // contains lameli                                
                                System.out.println("--> contains land");
                                System.out.println("landFoundIndex: " + landFoundIndex);
                                JSONObject foundLandObject = (JSONObject) jsonArray.get(landFoundIndex);
                                System.out.println("foundLandObject count: " + Integer.parseInt((String) foundLandObject.get("count")));
                                foundLandObject.replace("count", Integer.toString(Integer.parseInt((String) foundLandObject.get("count")) + 1)); // increment the count

                                if (regionLameli != null) {
                                    JSONArray lameliList = (JSONArray) foundLandObject.get("children");
                                    boolean containsLameli = false;
                                    int lameliFoundIndex = 0;
                                    for (int k = 0; k < lameliList.size(); k++) {
                                        JSONObject lameliObj = (JSONObject) lameliList.get(k);
                                        System.out.println("------" + lameliObj.get("id"));
                                        if (lameliObj.containsValue(regionLameli)) {
                                            containsLameli = true;
                                            lameliFoundIndex = k;
                                            break;
                                        }
                                    }

                                    if (!containsLameli) { // contains lameli, does not contain wiesinger OBJECT
                                        System.out.println("--> contains land, does not contain lameli");
                                        lameliList.add(lameliObject);
                                    } else { // contains lameli, contains wiesinger
                                        System.out.println("--> contains land, contains lameli");

                                        JSONObject foundLameliObject = (JSONObject) lameliList.get(lameliFoundIndex);
                                        System.out.println("foundLameliObject count: " + Integer.parseInt((String) foundLameliObject.get("count")));
                                        foundLameliObject.replace("count", Integer.toString(Integer.parseInt((String) foundLameliObject.get("count")) + 1)); // increment the count

                                        JSONArray wiesingerList = (JSONArray) foundLameliObject.get("children");
                                        boolean containsWiesinger = false;
                                        int wiesingerFoundIndex = 0;
                                        for (int n = 0; n < wiesingerList.size(); n++) {
                                            JSONObject wiesingerObj = (JSONObject) wiesingerList.get(n);
                                            System.out.println("------" + wiesingerObj.get("id"));
                                            if (wiesingerObj.containsValue(regionWiesinger)) {
                                                containsWiesinger = true;
                                                wiesingerFoundIndex = n;
                                                break;
                                            }
                                        }

                                        if (!containsWiesinger) { // contains lameli, does not contain wiesinger OBJECT
                                            System.out.println("--> contains land, contains lameli, does not contain wiesinger");
                                            wiesingerList.add(wiesingerObject);
                                        } else { // contains lameli, contains wiesinger
                                            System.out.println("--> contains land, contains lameli, contains wiesinger");

                                            JSONObject foundWiesingerObject = (JSONObject) wiesingerList.get(wiesingerFoundIndex);
                                            System.out.println("foundWiesingerObject count: " + Integer.parseInt((String) foundWiesingerObject.get("count")));
                                            foundWiesingerObject.replace("count", Integer.toString(Integer.parseInt((String) foundWiesingerObject.get("count")) + 1)); // increment the count
                                        }
                                    }
                                }
                                ///usabhkab
                            }
                        }
                    }
                });
                // nice print
                System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonArray));

                // write to json
                if (!jsonArray.isEmpty()) {
                    FileWriter file = new FileWriter(DATA_PATH + "prototypeJson/sprachregionTreeselect" + corpusID + ".json");
                    file.write(jsonArray.toString());
                    file.flush();
                }
            }

            long end = System.currentTimeMillis();
            DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss:sss");
            Date timeNeeded = new Date(end - start);
            System.out.println("time needed: " + dateFormat.format(timeNeeded));
        } catch (ParseException ex) {
            Logger.getLogger(OutputSprachregionListJson.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JsonProcessingException ex) {
                Logger.getLogger(OutputSprachregionListJson.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
