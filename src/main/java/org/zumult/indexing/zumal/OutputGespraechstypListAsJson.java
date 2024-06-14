/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.indexing.zumal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.zumult.indexing.Indexer;

/**
 *
 * @author josip.batinic
 */
public class OutputGespraechstypListAsJson implements Indexer {
    String[] CORPORA = {"FOLK", "GWSS"};
//    String CORPUS_ID = "FOLK";
//    String CORPUS_ID = "GWSS";
    String DATA_PATH = "src\\main\\java\\data\\";
//    String IDLISTS_PATH = DATA_PATH + "IDLists\\";
//    XPath xPath = XPathFactory.newInstance().newXPath();
    ObjectMapper mapper = new ObjectMapper();
    

    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new OutputGespraechstypListAsJson().index();
        } catch (IOException ex) {
            Logger.getLogger(OutputGespraechstypListAsJson.class.getName()).log(Level.SEVERE, null, ex);
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
                //FileReader reader = new FileReader(corpusJsonPath, "UTF_8");
                FileInputStream fis = new FileInputStream(corpusJsonPath);
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);

                JSONArray corpusJsonObject = (JSONArray) jsonParser.parse(isr);

                // iterate per speechevent
                corpusJsonObject.forEach(speechEvent -> {
                    JSONObject speechEventObject = (JSONObject) speechEvent;
                    System.out.println(speechEventObject.get("id") + " *******************************");
                    // get the three stooges
                    /*String domainOfInteraction = (String) speechEventObject.get("domain_of_interaction");
                    List areaOfLifeStringArray = (ArrayList) speechEventObject.get("area_of_life");
                    String activity = (String) speechEventObject.get("activity");*/


                    List topics = (List) speechEventObject.get("themen");
                    System.out.println(topics);
                    String domainOfInteraction = (String) speechEventObject.get("interaktionsdomäne");
                    List areaOfLifeStringArray = (ArrayList) speechEventObject.get("lebensbereich");
                    String activity = (String) speechEventObject.get("aktivität");
                    

                    System.out.println(areaOfLifeStringArray);
                    System.out.println("domainOfInteraction:\t" + domainOfInteraction);
                    System.out.println("activity:\t\t" + activity);

                    if (areaOfLifeStringArray != null) {
                        for (int i = 0; i < areaOfLifeStringArray.size(); i++) {
                            String areaOfLife = (String) areaOfLifeStringArray.get(i);
                            System.out.println("areaOfLife:\t\t" + areaOfLife);

                            JSONObject activityObject = new JSONObject();
                            activityObject.put("id", domainOfInteraction + "__" + areaOfLife + "__" + activity);
                            activityObject.put("label", activity);
                            activityObject.put("count", "1");
                            JSONArray activityArray = new JSONArray();
                            activityArray.add(activityObject);

                            JSONObject areaOfLifeObject = new JSONObject();
                            areaOfLifeObject.put("id", domainOfInteraction + "__" + areaOfLife);
                            areaOfLifeObject.put("label", areaOfLife);
                            areaOfLifeObject.put("count", "1");
                            areaOfLifeObject.put("children", activityArray);
                            JSONArray areaOfLifeArray = new JSONArray();
                            areaOfLifeArray.add(areaOfLifeObject);

                            JSONObject domainOfInteractionObject = new JSONObject();
                            domainOfInteractionObject.put("id", domainOfInteraction);
                            domainOfInteractionObject.put("label", domainOfInteraction);
                            domainOfInteractionObject.put("count", "1");
                            domainOfInteractionObject.put("children", areaOfLifeArray);

                            if (jsonArray.isEmpty()) {
                                System.out.println("--> jsonArray Empty");
                                jsonArray.add(domainOfInteractionObject);
                            } else { // jsonArray is not empty
                                System.out.println("--> jsonArray not empty");
                                boolean containsDomain = false; // check if domain already in the json array
                                int domainFoundIndex = 0;
                                for (int j = 0; j < jsonArray.size(); j++) {
                                    JSONObject domainObject = (JSONObject) jsonArray.get(j);
                                    System.out.println("--" + domainObject.get("value"));
                                    if (domainObject.containsValue(domainOfInteraction)) {
                                        containsDomain = true;
                                        domainFoundIndex = j;
                                        break;
                                    }
                                }

                                if (!containsDomain) { // jsonArray is not empty and it does not contain domain
                                    System.out.println("--> jsonArray not empty, and does not contain domain");
                                    jsonArray.add(domainOfInteractionObject);
                                } else { // contains domain
                                    System.out.println("--> contains domain");

                                    JSONObject foundDomainObject = (JSONObject) jsonArray.get(domainFoundIndex);
                                    System.out.println("foundDomainObject count: " + Integer.parseInt((String) foundDomainObject.get("count")));
                                    foundDomainObject.replace("count", Integer.toString(Integer.parseInt((String) foundDomainObject.get("count")) + 1)); // increment the count

                                    JSONArray areaOfLifeList = (JSONArray) foundDomainObject.get("children");
                                    boolean containsAreaOfLife = false; // check if area of life OBJECT already in the domain array
                                    int areaOfLifeFoundIndex = 0;
                                    for (int k = 0; k < areaOfLifeList.size(); k++) {
                                        JSONObject areaOfLifeObj = (JSONObject) areaOfLifeList.get(k);
                                        System.out.println("------" + areaOfLifeObj.get("value"));
                                        if (areaOfLifeObj.containsValue(areaOfLife)) {
                                            containsAreaOfLife = true;
                                            areaOfLifeFoundIndex = k;
                                            break;
                                        }
                                    }

                                    if (!containsAreaOfLife) { // contains domain, does not contain area of life OBJECT
                                        System.out.println("--> contains domain, does not contain area of life");
                                        areaOfLifeList.add(areaOfLifeObject);
                                    } else { // contains domain, contain area of life
                                        System.out.println("--> contains domain, contains area of life");

                                        JSONObject foundAreaOfLifeObject = (JSONObject) areaOfLifeList.get(areaOfLifeFoundIndex);
                                        System.out.println("foundAreaOfLifeObject count: " + Integer.parseInt((String) foundAreaOfLifeObject.get("count")));
                                        foundAreaOfLifeObject.replace("count", Integer.toString(Integer.parseInt((String) foundAreaOfLifeObject.get("count")) + 1)); // increment the count

                                        JSONArray activityList = (JSONArray) foundAreaOfLifeObject.get("children");
                                        boolean containsActivity = false; // check if activity is already in the area of life array
                                        int activityFoundIndex = 0;
                                        for (int l = 0; l < activityList.size(); l++) {
                                            JSONObject activityObj = (JSONObject) activityList.get(l);
                                            System.out.println("----------" + activityObj.get("value"));
                                            if (activityObj.containsValue(activity)) {
                                                containsActivity = true;
                                                activityFoundIndex = l;
                                                break;
                                            }
                                        }

                                        if (!containsActivity) { // contains domain, contains area of life, does not contain activity
                                            System.out.println("--> contains domain, contains area of life, does not contain activity");
                                            activityList.add(activityObject);
                                        } else { // contains domain, contains area of life, contains activity
                                            System.out.println("--> contains domain, contains area of life, contains activity");

                                            JSONObject foundActivityObject = (JSONObject) activityList.get(activityFoundIndex);
                                            System.out.println("foundActivityObject count: " + Integer.parseInt((String) foundActivityObject.get("count")));
                                            foundActivityObject.replace("count", Integer.toString(Integer.parseInt((String) foundActivityObject.get("count")) + 1)); // increment the count
                                        }

                                    }

                                }
                            }
                        }
                    }

                });
                
                isr.close();
                
                // nice print
                System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonArray));

                // write to json
                if (!jsonArray.isEmpty()) {
                    
                    FileOutputStream fos = new FileOutputStream(DATA_PATH + "prototypeJson/gesprachstypTreeselect" + corpusID + ".json");
                    OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                    BufferedWriter bw = new BufferedWriter(osw);
                    bw.append(jsonArray.toString());
                    bw.flush();
                    
                /*  FileWriter file = new FileWriter(DATA_PATH + "prototypeJson/gesprachstypTreeselect" + corpusID + ".json");
                    file.write(jsonArray.toString());
                    file.flush();*/
                }
            }

            long end = System.currentTimeMillis();
            DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss:sss");
            Date timeNeeded = new Date(end - start);
            System.out.println("time needed: " + dateFormat.format(timeNeeded));
        } catch (ParseException ex) {
            Logger.getLogger(OutputGespraechstypListAsJson.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JsonProcessingException ex) {
                Logger.getLogger(OutputGespraechstypListAsJson.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}