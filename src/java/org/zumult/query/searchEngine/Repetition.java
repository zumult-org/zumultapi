/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.searchEngine;

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

    public Repetition(Element el) throws SearchServiceException {
 
        setRepetitionType(el.getElementsByTagName(Constants.REPETITON_XML_ELEMENT_NAME_REPETITON_TYPE).item(0).getTextContent());
        setSpeaker(getBooleanFromString(Constants.REPETITON_XML_ELEMENT_NAME_SPEAKER, el.getElementsByTagName(Constants.REPETITON_XML_ELEMENT_NAME_SPEAKER).item(0).getTextContent()));   
        setSpeakerMetadata(el.getElementsByTagName(Constants.REPETITON_XML_ELEMENT_NAME_SPEAKER_METADATA).item(0).getTextContent());
        setSpeakerChange(getBooleanFromString(Constants.REPETITON_XML_ELEMENT_NAME_SPEAKER_CHANGE, el.getElementsByTagName(Constants.REPETITON_XML_ELEMENT_NAME_SPEAKER_CHANGE).item(0).getTextContent()));
        setOptionIgnoreFunctionalWords(getBooleanFromString(Constants.REPETITON_XML_ELEMENT_NAME_IGNORE_FUNCTIONAL_WORDS, el.getElementsByTagName(Constants.REPETITON_XML_ELEMENT_NAME_IGNORE_FUNCTIONAL_WORDS).item(0).getTextContent()));
        setMinMaxDistance(getIntegerFromString(Constants.REPETITON_XML_ELEMENT_NAME_MIN_DISTANCE, el.getElementsByTagName(Constants.REPETITON_XML_ELEMENT_NAME_MIN_DISTANCE).item(0).getTextContent()), 
                getIntegerFromString(Constants.REPETITON_XML_ELEMENT_NAME_MAX_DISTANCE, el.getElementsByTagName(Constants.REPETITON_XML_ELEMENT_NAME_MAX_DISTANCE).item(0).getTextContent()));
        setPositionOverlap(el.getElementsByTagName(Constants.REPETITON_XML_ELEMENT_NAME_POSITION_TO_OVERLAP).item(0).getTextContent());
        setPositionSpeakerChange(el.getElementsByTagName(Constants.REPETITON_XML_ELEMENT_NAME_POSITION_TO_SPEAKER_CHANGE_TYPE).item(0).getTextContent(), 
                getIntegerFromString(Constants.REPETITON_XML_ELEMENT_NAME_POSITION_TO_SPEAKER_CHANGE_MIN_DISTANCE, el.getElementsByTagName(Constants.REPETITON_XML_ELEMENT_NAME_POSITION_TO_SPEAKER_CHANGE_MIN_DISTANCE).item(0).getTextContent()), 
                getIntegerFromString(Constants.REPETITON_XML_ELEMENT_NAME_POSITION_TO_SPEAKER_CHANGE_MAX_DISTANCE, el.getElementsByTagName(Constants.REPETITON_XML_ELEMENT_NAME_POSITION_TO_SPEAKER_CHANGE_MAX_DISTANCE).item(0).getTextContent()));
        setPrecededby(el.getElementsByTagName(Constants.REPETITON_XML_ELEMENT_NAME_CONTEXT_PRECEDEDBY).item(0).getTextContent());
        setOptionWithinSpeakerContribution(getBooleanFromString(Constants.REPETITON_XML_ELEMENT_NAME_CONTEXT_WITHIN_SPEAKER_CONTRIBUTION, el.getElementsByTagName(Constants.REPETITON_XML_ELEMENT_NAME_CONTEXT_WITHIN_SPEAKER_CONTRIBUTION).item(0).getTextContent()));
    }
    

    
    public Repetition(String type, Boolean sameSpeakerAsSource, 
            Boolean speakerChange, Boolean ignoreFunctionalWords, String positionOverlap, 
            Integer minDistanceToSource, Integer maxDistanceToSource, String metadataQueryString,
            String positionSpeakerChangeType, Integer positionSpeakerChangeMin, 
            Integer positionSpeakerChangeMax, String precededby, Boolean withinSpeakerContribution) throws SearchServiceException{
        
        setRepetitionType(type);
        setSpeaker(sameSpeakerAsSource); 
        setSpeakerMetadata(metadataQueryString);
        setSpeakerChange(speakerChange);
        setOptionIgnoreFunctionalWords(ignoreFunctionalWords);
        setMinMaxDistance(minDistanceToSource, maxDistanceToSource);
        setPositionOverlap(positionOverlap);
        setPositionSpeakerChange(positionSpeakerChangeType, positionSpeakerChangeMin, positionSpeakerChangeMax);
        setOptionWithinSpeakerContribution(withinSpeakerContribution);
        setPrecededby(precededby);
        
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
    
    public Boolean ignoreFunctionalWords(){
        return this.distanceToPreviousElement.ignoreFunctionalWords;
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
    
    public Boolean isWithinSpeakerContribution(){
        return this.context.withinSpeakerContribution;
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
    
    private void setOptionWithinSpeakerContribution(Boolean withinSpeakerContribution){
        if(withinSpeakerContribution!=null){
            this.context.withinSpeakerContribution = withinSpeakerContribution;
        }
    }
    
    private void setOptionIgnoreFunctionalWords(Boolean ignoreFunctionalWords) throws SearchServiceException{
        if(ignoreFunctionalWords!=null){
            this.distanceToPreviousElement.ignoreFunctionalWords = ignoreFunctionalWords;
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
        Boolean withinSpeakerContribution = false; 
        String precededby;
    }
    
    private class Distance {        
        Integer minDistance; //if null, then irrelevant
        Integer maxDistance; //if null, then irrelevant
        Boolean ignoreFunctionalWords = false;
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
        EQUAL, FUZZY, FUZZY_PLUS, DIFF_PRON, DIFF_NORM
    }
    
    public enum PositionToMatch{
        FOLLOWEDBY, PRECEDEDBY
    }

}
