/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.searchEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.w3c.dom.Element;
import org.zumult.io.Constants;
import org.zumult.query.SearchServiceException;

/**
 *
 * @author Frick
 */
public class Repetition {
    
    private final Distance distanceToPreviousElement = new Distance();
    private final Position position = new Position();
    private final Speaker speaker = new Speaker();
    private RepetitionTypeEnum type = RepetitionTypeEnum.WORD;
    private SimilarityTypeEnum similarity = SimilarityTypeEnum.EQUAL;
    private final Context context = new Context();
    private boolean ignoreTokenOrder = true;

    public Repetition(Element el) throws SearchServiceException {
 
        setRepetitionType(el.getElementsByTagName(Constants.REPETITION_XML_ELEMENT_NAME_REPETITON_TYPE).item(0).getTextContent());
        setSimilarityType(el.getElementsByTagName(Constants.REPETITION_XML_ELEMENT_NAME_REPETITON_SIMILARITY_TYPE).item(0).getTextContent());
        setSpeaker(getBooleanFromString(Constants.REPETITION_XML_ELEMENT_NAME_SPEAKER, el.getElementsByTagName(Constants.REPETITION_XML_ELEMENT_NAME_SPEAKER).item(0).getTextContent()));   
        setSpeakerMetadata(el.getElementsByTagName(Constants.REPETITION_XML_ELEMENT_NAME_SPEAKER_METADATA).item(0).getTextContent());
        setSpeakerChange(getBooleanFromString(Constants.REPETITION_XML_ELEMENT_NAME_SPEAKER_CHANGE, el.getElementsByTagName(Constants.REPETITION_XML_ELEMENT_NAME_SPEAKER_CHANGE).item(0).getTextContent()));
        setIgnoredCustomPOS(new HashSet<>(Arrays.asList(el.getElementsByTagName(Constants.REPETITION_XML_ELEMENT_NAME_IGNORED_CUSTOM_POS).item(0).getTextContent().split(Constants.REPETITION_XML_ELEMENT_NAME_IGNORED_CUSTOM_POS_SEPARATOR))));    
        setOptionIgnoreTokenOrder(getBooleanFromString(Constants.REPETITION_XML_ELEMENT_NAME_IGNORE_TOKEN_ORDER, el.getElementsByTagName(Constants.REPETITION_XML_ELEMENT_NAME_IGNORE_TOKEN_ORDER).item(0).getTextContent()));
        setMinMaxDistance(getIntegerFromString(Constants.REPETITION_XML_ELEMENT_NAME_MIN_DISTANCE, el.getElementsByTagName(Constants.REPETITION_XML_ELEMENT_NAME_MIN_DISTANCE).item(0).getTextContent()), 
                getIntegerFromString(Constants.REPETITION_XML_ELEMENT_NAME_MAX_DISTANCE, el.getElementsByTagName(Constants.REPETITION_XML_ELEMENT_NAME_MAX_DISTANCE).item(0).getTextContent()));
        setPositionOverlap(el.getElementsByTagName(Constants.REPETITION_XML_ELEMENT_NAME_POSITION_TO_OVERLAP).item(0).getTextContent());
        setPositionSpeakerChange(el.getElementsByTagName(Constants.REPETITION_XML_ELEMENT_NAME_POSITION_TO_SPEAKER_CHANGE_TYPE).item(0).getTextContent(), 
                getIntegerFromString(Constants.REPETITION_XML_ELEMENT_NAME_POSITION_TO_SPEAKER_CHANGE_MIN_DISTANCE, el.getElementsByTagName(Constants.REPETITION_XML_ELEMENT_NAME_POSITION_TO_SPEAKER_CHANGE_MIN_DISTANCE).item(0).getTextContent()), 
                getIntegerFromString(Constants.REPETITION_XML_ELEMENT_NAME_POSITION_TO_SPEAKER_CHANGE_MAX_DISTANCE, el.getElementsByTagName(Constants.REPETITION_XML_ELEMENT_NAME_POSITION_TO_SPEAKER_CHANGE_MAX_DISTANCE).item(0).getTextContent()));
        setPrecededby(el.getElementsByTagName(Constants.REPETITION_XML_ELEMENT_NAME_CONTEXT_LEFT).item(0).getTextContent());
        setFollowedby(el.getElementsByTagName(Constants.REPETITION_XML_ELEMENT_NAME_CONTEXT_RIGHT).item(0).getTextContent());
        setOptionWithinSpeakerContributionLeft(getBooleanFromString(Constants.REPETITION_XML_ELEMENT_NAME_CONTEXT_WITHIN_SPEAKER_CONTRIBUTION_LEFT, el.getElementsByTagName(Constants.REPETITION_XML_ELEMENT_NAME_CONTEXT_WITHIN_SPEAKER_CONTRIBUTION_LEFT).item(0).getTextContent()));
        setOptionWithinSpeakerContributionRight(getBooleanFromString(Constants.REPETITION_XML_ELEMENT_NAME_CONTEXT_WITHIN_SPEAKER_CONTRIBUTION_RIGHT, el.getElementsByTagName(Constants.REPETITION_XML_ELEMENT_NAME_CONTEXT_WITHIN_SPEAKER_CONTRIBUTION_RIGHT).item(0).getTextContent()));
    }
    

    
    public Repetition(String type, String similarity, Boolean sameSpeakerAsSource, 
            Boolean speakerChange, Set<String> ignoredCustomPOS, Boolean ignoreTokenOrder, String positionOverlap, 
            Integer minDistanceToSource, Integer maxDistanceToSource, String metadataQueryString,
            String positionSpeakerChangeType, Integer positionSpeakerChangeMin, 
            Integer positionSpeakerChangeMax, String precededby, String followedby, 
            Boolean withinSpeakerContributionLeft, Boolean withinSpeakerContributionRight) throws SearchServiceException{
        
        setRepetitionType(type);
        setSimilarityType(similarity);
        setSpeaker(sameSpeakerAsSource); 
        setSpeakerMetadata(metadataQueryString);
        setSpeakerChange(speakerChange);
        setIgnoredCustomPOS(ignoredCustomPOS);
        setOptionIgnoreTokenOrder(ignoreTokenOrder);
        setMinMaxDistance(minDistanceToSource, maxDistanceToSource);
        setPositionOverlap(positionOverlap);
        setPositionSpeakerChange(positionSpeakerChangeType, positionSpeakerChangeMin, positionSpeakerChangeMax);
        setOptionWithinSpeakerContributionLeft(withinSpeakerContributionLeft);
        setOptionWithinSpeakerContributionRight(withinSpeakerContributionRight);
        setPrecededby(precededby);
        setFollowedby(followedby);
        
    }
    
    public Boolean isSameSpeaker(){
        return this.speaker.sameSpeakerAsSource;
    }
    
    public String getSpeakerMetadata(){
        return this.speaker.speakerMetadata;
    }
    
    public Boolean isSpeakerChangedDesired(){
        return this.distanceToPreviousElement.speakerChange;
    }
    
    public Set<String> getIgnoredCustomPOS(){
        return this.distanceToPreviousElement.ignoredCustomPOS;
    }
    
    public Boolean ignoreTokenOrder(){
        return this.ignoreTokenOrder;
    }
    
    public PositionOverlapEnum getPositionOverlap(){
        return this.position.positionOverlap;
    }
    
    public PositionSpeakerChangeEnum getPositionSpeakerChangeType(){
        return this.position.positionSpeakerChange.type;       
    }
    
    public Integer getMinPositionSpeakerChange(){
        return this.position.positionSpeakerChange.minWordToken;
    }
    
    public Integer getMaxPositionSpeakerChange(){
        return this.position.positionSpeakerChange.maxWordToken;
    }
    
    public Integer getMinDistance(){
        return this.distanceToPreviousElement.minDistance;
    }
    
    public String getPrecededby(){
        return this.context.precededby;
    }
    
    public String getFollowedby(){
        return this.context.followedby;
    }
    
    public Boolean isWithinSpeakerContributionLeft(){
        return this.context.withinSpeakerContributionLeft;
    }
    
    public Boolean isWithinSpeakerContributionRight(){
        return this.context.withinSpeakerContributionRight;
    }
    
    public Integer getMaxDistance(){
        return this.distanceToPreviousElement.maxDistance;
    }
    
    public RepetitionTypeEnum getType(){
        return this.type;
    }
    
    public SimilarityTypeEnum getSimilarityType(){
        return this.similarity;
    }

    private Boolean getBooleanFromString(String key, String value) throws SearchServiceException{

        switch(value){
            case "false":
                return false;
            case "true":
                return true;
            case "null":
                return null;
            default:
                throw new SearchServiceException ("Please check the value of "+ key + "! It can be true, false or null");
        }
    }
    
    private Integer getIntegerFromString(String key, String value) throws SearchServiceException{
        if(value.equals("null")){
            return null;
        }else{
            try{
                Integer i = Integer.parseInt(value);
                return i;
            }catch(NumberFormatException ex){
                throw new SearchServiceException(value + " is not valid for " + key + "!");
            }
        }
    }
    
    private void setPrecededby(String precededby){
        this.context.precededby = precededby.replace("&lt;", "<").replace("&gt;", ">");
    }
    
    private void setFollowedby(String followedby){
        this.context.followedby = followedby.replace("&lt;", "<").replace("&gt;", ">");
    }
    
    private void setSpeaker(Boolean speaker){
        this.speaker.sameSpeakerAsSource = speaker;
    }
    
    private void setSpeakerMetadata(String metadataQueryString){
        this.speaker.speakerMetadata = metadataQueryString.replace("&amp;", "&")
                .replace("&lt;", "<").replace("&gt;", ">")
                .replace("&", "&&").replace("|", "||");
    }
    
    private void setSpeakerChange(Boolean speakerChange){
        this.distanceToPreviousElement.speakerChange = speakerChange;
    }
    
    private void setSimilarityType(String similarity) throws SearchServiceException{
                if(similarity!=null && !similarity.isEmpty()){
            try{
                this.similarity = SimilarityTypeEnum.valueOf(similarity);
            }catch (IllegalArgumentException ex){
                StringBuilder sb = new StringBuilder();
                sb.append(". Similarity type ").append(similarity).append(" is not supported. Supported similarity types are: ");
                for (RepetitionTypeEnum ob : RepetitionTypeEnum.values()){
                    sb.append(ob.name());
                    sb.append(", ");
                }
                throw new SearchServiceException(sb.toString().trim().replaceFirst(",$",""));
            }
        }else {
            throw new SearchServiceException("You have not specified the type of repetition similarity!");
        }
    }
    
    private void setRepetitionType(String type) throws SearchServiceException{
        if(type!=null && !type.isEmpty()){
            try{
                this.type = RepetitionTypeEnum.valueOf(type);
            }catch (IllegalArgumentException ex){
                StringBuilder sb = new StringBuilder();
                sb.append(". Repetition type ").append(type).append(" is not supported. Supported repetition types are: ");
                for (RepetitionTypeEnum ob : RepetitionTypeEnum.values()){
                    sb.append(ob.name());
                    sb.append(", ");
                }
                throw new SearchServiceException(sb.toString().trim().replaceFirst(",$",""));
            }
        }else {
            throw new SearchServiceException("You have not specified the type of repetition desired!");
        }
                
    }
    
    private void setMinMaxDistance(Integer minDistanceToSource, Integer maxDistanceToSource) throws SearchServiceException{
        if(minDistanceToSource>maxDistanceToSource){
            throw new SearchServiceException("Please check the minDistance. It can not be greater than maxDistance");
        }
        
        this.distanceToPreviousElement.minDistance = minDistanceToSource;
        this.distanceToPreviousElement.maxDistance = maxDistanceToSource;
    }
    
    private void setOptionWithinSpeakerContributionLeft(Boolean withinSpeakerContributionLeft){
        this.context.withinSpeakerContributionLeft = withinSpeakerContributionLeft;
    }
    
    private void setOptionWithinSpeakerContributionRight(Boolean withinSpeakerContributionRight){
        this.context.withinSpeakerContributionRight = withinSpeakerContributionRight;
    }
    
    private void setIgnoredCustomPOS(Set<String> ignoredCustomPOS) throws SearchServiceException{
        if(ignoredCustomPOS!=null){
            this.distanceToPreviousElement.ignoredCustomPOS = ignoredCustomPOS;
        }
    }
    
    private void setOptionIgnoreTokenOrder(Boolean ignoreTokenOrder) throws SearchServiceException{
        if(ignoreTokenOrder!=null){
            this.ignoreTokenOrder = ignoreTokenOrder;
        }
    }
        
    private void setPositionOverlap(String positionOverlap) throws SearchServiceException{
            if(positionOverlap!=null && !positionOverlap.isEmpty() && !positionOverlap.equals("null")){
            try{
                this.position.positionOverlap = PositionOverlapEnum.valueOf(positionOverlap);
            }catch (IllegalArgumentException ex){
                StringBuilder sb = new StringBuilder();
                sb.append(". Plase check the specified position to speaker overlap.").append(" Supported positions are: ");
                for (PositionOverlapEnum ob : PositionOverlapEnum.values()){
                    sb.append(ob.name());
                    sb.append(", ");
                }
                throw new SearchServiceException(sb.toString().trim().replaceFirst(",$",""));
            }
        }
    }
    
    private void setPositionSpeakerChange(String positionSpeakerChangeType, Integer positionSpeakerChangeMin, Integer positionSpeakerChangeMax) throws SearchServiceException{
        if(positionSpeakerChangeType!=null && !positionSpeakerChangeType.isEmpty() && !positionSpeakerChangeType.equals("null")){
            try{
                this.position.positionSpeakerChange.type = PositionSpeakerChangeEnum.valueOf(positionSpeakerChangeType);
                this.position.positionSpeakerChange.maxWordToken = positionSpeakerChangeMax;
                this.position.positionSpeakerChange.minWordToken = positionSpeakerChangeMin;
            }catch (IllegalArgumentException ex){
                StringBuilder sb = new StringBuilder();
                sb.append(". Plase check the specified position to speaker change.").append(" Supported positions are: ");
                for (PositionSpeakerChangeEnum ob : PositionSpeakerChangeEnum.values()){
                    sb.append(ob.name());
                    sb.append(", ");
                }
                throw new SearchServiceException(sb.toString().trim().replaceFirst(",$",""));
            }
        }
   }
    
    private class Speaker{
        Boolean sameSpeakerAsSource; //if null, then irrelevant
        String speakerMetadata;
    }
        
    private class Position {
        PositionOverlapEnum positionOverlap;
        PositionSpeakerChange positionSpeakerChange = new PositionSpeakerChange();
        PositionToMatch positionToMatch;
    }
    
    private class Context {
        String precededby;
        String followedby;
        Boolean withinSpeakerContributionLeft = null;
        Boolean withinSpeakerContributionRight = null;

    }
    
    private class Distance {        
        Integer minDistance; //if null, then irrelevant
        Integer maxDistance; //if null, then irrelevant
        //Boolean ignoreFunctionalWords = false;
        Set<String> ignoredCustomPOS = new HashSet();
        Boolean speakerChange;   /* true - speaker change is required, 
                                false - speaker change is not allowed,
                                null - speaker change is irrelevant */
        
    }
    
    private class PositionSpeakerChange {
        PositionSpeakerChangeEnum type;
        int maxWordToken;
        int minWordToken;
    }
    
    public enum PositionOverlapEnum {
        WITHIN, NOT_WITHIN, INTERSECTING, FOLLOWEDBY, PRECEDEDBY
    }
    
    public enum PositionSpeakerChangeEnum {
        FOLLOWEDBY, PRECEDEDBY
    }
    
    public enum RepetitionTypeEnum {
        LEMMA, WORD, NORM
    }
    
    public enum SimilarityTypeEnum {
        EQUAL, 
        
        FUZZY, /* */
        FUZZY_PLUS, 
        
        /* In the case of a word sequence, 
        it is sufficient if the entire string differs, 
        not every single word */
        DIFF_PRON, /* e.g. selektive klopfreglung -> selektive klopfregelung */
        DIFF_NORM, /* mit allen drei würfeln dieselbe augenzahl würfeln */
        
        OWN_LEMMA_LIST,
        
        GERMANET_PLUS,
        GERMANET,
        GERMANET_ORTH,
        GERMANET_HYPERNYM,
        GERMANET_HYPONYM,
        GERMANET_COMPOUNDS;
    }
    
    public enum PositionToMatch{
        FOLLOWEDBY, PRECEDEDBY
    }
    
    public class Synonyms extends ArrayList<ArrayList<String>>{
        
    }

}
