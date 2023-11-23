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

    /**
     * Object of the {@link Distance} class. It specifies the minimum and
     * maximum of the distance between repetitions,
     * POS values that should be ignored when measuring distance
     * and if speaker change should occur in between or not.
     */
    private final Distance distanceToPreviousElement = new Distance();

    /**
     * Object of the {@link Position} class. It specifies positions
     * of the repetition relative to speaker overlap and speaker change.
     */
    private final Position position = new Position();

    /**
     * Object of the {@link Speaker} class. It specifies the speaker
     * producing repetition and his metadata.
     */
    private final Speaker speaker = new Speaker();

    /**
     * Object of the {@link Context} class. It specifies the left and right
     * context of the repetion, the distance to it and if the context should
     * occur in the same speaker contribution as the repetition itself.
     */
    private final Context context = new Context();

    /**
     * The default value for the annotation level
     * where repetitions should be searched.
     */
    private RepetitionTypeEnum type = RepetitionTypeEnum.WORD;

    /**
     * The default value for the search mode.
     */
    private final SimilarityObject similarity = new SimilarityObject();

    /**
     * The default value for searching multi word repetitions
     * specify that the token order may vary and should not exaclty match
     * that in the source element.
     */
    private boolean ignoreTokenOrder = true;

    /**
    * Class constructor.
    *
    * @param el repetition xml element, e.g.
    *
    * @throws org.zumult.query.SearchServiceException if method parameters
    *         are illegal or not supported
    */
    public Repetition(final Element el) throws SearchServiceException {
        setRepetitionType(el);
        setSimilarity(el);
        setSpeakerOptions(el);
        setSpeakerChange(el);
        setIgnoredCustomPOS(el);
        setOptionIgnoreTokenOrder(el);
        setDistance(el); // set min and max distance between repetitions
        setRepetitionContext(el); // set left and right context
        setPositionOverlap(el);
        setPositionSpeakerChange(el);
    }

    /**
     * Returns the value of the speaker option when searching repetitions.
     *
     * Returns <i>true</i> if the element we are looking for
     * should be repeated by the same speaker.
     *
     * Returns <i>false</i> if the element we are looking for
     * should be repeated by another speaker.
     *
     * Returns <i>null</i> if the speaker producing the repetition
     * is not important.
     *
     * @return the Boolean object
     */
    public Boolean isSameSpeaker() {
        return this.speaker.sameSpeakerAsSource;
    }

    /**
     * Returns the speaker metadata query string, for example
     * &lt;s_geschlecht=&quot;Männlich&quot;/&gt; {@literal &}
     *  (&lt;ses_rolle_s=&quot;Tutor/in&quot;/&gt; |
     *  &lt;ses_rolle_s=&quot;Lehrer/in&quot;/&gt;).
     *
     * @return the String object
     */
    public String getSpeakerMetadata() {
        return this.speaker.speakerMetadata;
    }

    /**
     * Returns the value of the speaker change option
     * when searching repetitions.
     *
     * Returns <i>true</i> if speaker change between repetitions is required.
     *
     * Returns <i>false</i> if speaker change between repetitions
     * is not allowed.
     *
     * Returns <i>null</i> if speaker change between repetitions may occur,
     * but is not required.
     *
     * @return the Boolean object
     */
    public Boolean isSpeakerChangedDesired() {
        return this.distanceToPreviousElement.speakerChange;
    }

    /**
     * Returns the set of POS values that should be irgnored
     * when measuring distance between repetitions.
     *
     * @return the set of strings
     */
    public Set<String> getIgnoredCustomPOS() {
        return this.distanceToPreviousElement.ignoredCustomPOS;
    }

    /**
     * Returns the token order of multi word repetitions.
     *
     * Returns <i>true</i> if the token order of the multi word repetition
     * may vary.
     *
     * Returns <i>false</i> if the token order of the multi word repetition
     * should be the same as in the source element.
     *
     * @return the Boolean object
     */
    public Boolean ignoreTokenOrder() {
        return this.ignoreTokenOrder;
    }

    /**
     *
     * @return the {@code PositionOverlapEnum} object
     */
    public PositionOverlapEnum getPositionOverlap() {
        return this.position.positionOverlap;
    }

    /**
     *
     * @return the {@code PositionSpeakerChangeEnum} object
     */
    public PositionSpeakerChangeEnum getPositionSpeakerChangeType() {
        return this.position.positionSpeakerChange.type;
    }

    /**
     *
     * @return the Integer object
     */
    public Integer getMinPositionSpeakerChange() {
        return this.position.positionSpeakerChange.minWordToken;
    }

    /**
     *
     * @return the Integer object
     */
    public Integer getMaxPositionSpeakerChange() {
        return this.position.positionSpeakerChange.maxWordToken;
    }

    /**
     *
     * @return the Integer object
     */
    public Integer getMinDistance() {
        return this.distanceToPreviousElement.minDistance;
    }

    /**
     *
     * @return the Integer object
     */
    public Integer getMinDistanceToLeftContext() {
        return this.context.distanceLeft.minDistance;
    }

    /**
     *
     * @return the Integer object
     */
    public Integer getMinDistanceToRightContext() {
        return this.context.distanceRight.minDistance;
    }

    /**
     *
     * @return the String object
     */
    public String getPrecededby() {
        return this.context.precededby;
    }

    /**
     *
     * @return the String object
     */
    public String getFollowedby() {
        return this.context.followedby;
    }

    /**
     *
     * @return the Boolean object
     */
    public Boolean isWithinSpeakerContributionLeft() {
        return this.context.withinSpeakerContributionLeft;
    }

    /**
     *
     * @return the Boolean object
     */
    public Boolean isWithinSpeakerContributionRight() {
        return this.context.withinSpeakerContributionRight;
    }

    /**
     *
     * @return the Integer object
     */
    public Integer getMaxDistance() {
        return this.distanceToPreviousElement.maxDistance;
    }

    /**
     *
     * @return the Integer object
     */
    public Integer getMaxDistanceToLeftContext() {
        return this.context.distanceLeft.maxDistance;
    }

    /**
     *
     * @return the Integer object
     */
    public Integer getMaxDistanceToRightContext() {
        return this.context.distanceRight.maxDistance;
    }

    /**
     *
     * @return the {@code RepetitionTypeEnum} object
     */
    public RepetitionTypeEnum getType() {
        return this.type;
    }

    /**
     *
     * @return the {@code SimilarityTypeEnum} object
     */
    public SimilarityTypeEnum getSimilarityType() {
        return this.similarity.type;
    }
    
    public double getMinSimilarity(){
        return this.similarity.min;
    }
    
    public double getMaxSimilarity(){
        return this.similarity.max;
    }
    
    public Set<GermaNetModeEnum> getGermaNetMode(){
        return this.similarity.germaNetMode;
    }

    /**
     * Sets if speaker change is required or not when searching repetitions.
     * (true - requiered, false - not allowed, null - may occur,
     * but should not)
     *
     * @param el repetition as xml object
     *
     * @throws org.zumult.query.SearchServiceException if the
     *           speaker change parameter is illegal or not supported
     */
    private void setSpeakerChange(final Element el)
            throws SearchServiceException {

        String speakerChangeValue = el
                .getElementsByTagName(Constants.REPETITION_SPEAKER_CHANGE)
                .item(0)
                .getTextContent();

        Boolean speakerChangeBooleanValue = getBooleanFromString(
                Constants.REPETITION_SPEAKER_CHANGE,
                speakerChangeValue);

        this.distanceToPreviousElement.speakerChange =
                speakerChangeBooleanValue;
    }

    /**
     * Sets the mode for repetition search (SimilarityTypeEnum) and GermaNet
     * items (GermaNetModeEnum) if GermaNet is selected as a seach mode.
     * 
     * @param el repetition as xml object
     *
     * @throws org.zumult.query.SearchServiceException if the
     *           search mode parameter is illegal or not supported
     */
    private void setSimilarity(final Element el)
            throws SearchServiceException {
        
        // set similarity  type
        String similarityType = el
                .getElementsByTagName(Constants.REPETITION_SIMILARITY_TYPE)
                .item(0)
                .getTextContent();

        if (similarityType != null && !similarityType.isEmpty()) {
            try {
                this.similarity.type = SimilarityTypeEnum
                                                .valueOf(similarityType);
            } catch (IllegalArgumentException ex) {
                
                String message = ". Please specify the similarity type."
                    + " Supported similarity types are: "
                    + getEnumValuesAsString(SimilarityTypeEnum.values());
                
                throw new SearchServiceException(message);
            }
        } else {
            throw new SearchServiceException("You have not specified "
                    + "the type of repetition similarity!");
        }
        
        if(this.similarity.type.equals(SimilarityTypeEnum.GERMANET_PLUS)
            || this.similarity.type.equals(SimilarityTypeEnum.GERMANET)) {
            setGermaNetItems(el);
        }
    }
    
    private void setGermaNetItems(final Element el)
            throws SearchServiceException{

        String germaNetItems = el
                .getElementsByTagName(Constants.REPETITION_GERMANET_ITEMS)
                .item(0)
                .getTextContent();
        
        if (germaNetItems != null && !germaNetItems.isEmpty()) {
            
            String[] germaNetItemsArray = germaNetItems.split(
                Constants.PIPE_SYMBOL);

            Set<GermaNetModeEnum> germaNetItemsSet = new HashSet<>();
            
            for (String str : germaNetItemsArray){
                try {
                    GermaNetModeEnum mode = GermaNetModeEnum
                                                .valueOf(str);
                    germaNetItemsSet.add(mode);
                } catch (IllegalArgumentException ex) {
                    
                    String message = str + " is not correct."
                      + " Supported GermaNet items are: "
                      + getEnumValuesAsString(GermaNetModeEnum.values());
                    
                    throw new SearchServiceException(message);
                }
            } 
            
            this.similarity.germaNetMode = germaNetItemsSet;
        } else {            
            throw new SearchServiceException("You have not specified "
                + "which items (synonyms, hyponyms, hypernyms or compounds)"
                + " should be included when using GermaNet");
        }
    }

    /**
    * Sets the annotation level where repetition should be searched,
    * namely by comparing transcribed, normalized or lemmatized forms.
    *
    * Possible values are defined in RepetitionTypeEnum.
    *
    * @param el repetition as xml object
    *
    * @throws org.zumult.query.SearchServiceException if the
    *           repetition type parameter is illegal or not supported
    */
    private void setRepetitionType(final Element el)
                                   throws SearchServiceException {

        String repetitionType = el
                .getElementsByTagName(Constants.REPETITION_TYPE)
                .item(0)
                .getTextContent();

        if (repetitionType != null && !repetitionType.isEmpty()) {
            try {
                this.type = RepetitionTypeEnum.valueOf(repetitionType);
            } catch (IllegalArgumentException ex) {
                
                String message = ". Please specify the repetition type."
                    + " Supported repetition types are: "
                    + getEnumValuesAsString(RepetitionTypeEnum.values());
                
                throw new SearchServiceException(message);
            }
        } else {
            throw new SearchServiceException("You have not specified "
                    + "the type of repetition desired!");
        }
    }

    private void setIgnoredCustomPOS(final Element el)
            throws SearchServiceException {

        String posToBeIgnored = el
                .getElementsByTagName(Constants.REPETITION_IGNORED_POS)
                .item(0)
                .getTextContent();

        if (posToBeIgnored != null && !posToBeIgnored.isEmpty()) {

            String[] posToBeIgnoredArray = posToBeIgnored.split(
                Constants.PIPE_SYMBOL);

            Set posToBeIgnoredSet =
                new HashSet<>(Arrays.asList(posToBeIgnoredArray));

            this.distanceToPreviousElement.ignoredCustomPOS =
                    posToBeIgnoredSet;
        }
    }
    
    private void setOptionIgnoreTokenOrder(final Element el)
            throws SearchServiceException {

        String tokenOrderValue = el
              .getElementsByTagName(Constants.REPETITION_IGNORE_TOKEN_ORDER)
              .item(0)
              .getTextContent();

        Boolean tokenOrderBooleanValue = getBooleanFromString(
                Constants.REPETITION_IGNORE_TOKEN_ORDER,
              tokenOrderValue);

        if (tokenOrderBooleanValue != null) {
            this.ignoreTokenOrder = tokenOrderBooleanValue;
        }
    }

    /**
    *  Sets the option if repetition should occur
    *     within or outside of speaker overlap.
    *
    * @param el repetition as xml object
    *
    * @throws org.zumult.query.SearchServiceException if the
    *           position overlap type parameter is illegal or not supported
    */
    private void setPositionOverlap(final Element el)
            throws SearchServiceException {

        String positionOverlap = el
            .getElementsByTagName(Constants.REPETITION_POSITION_TO_OVERLAP)
            .item(0)
            .getTextContent();

        if (positionOverlap != null
                            && !positionOverlap.isEmpty()
                            && !positionOverlap.equals("null")) {
            try {
                this.position.positionOverlap =
                    PositionOverlapEnum.valueOf(positionOverlap);
            } catch (IllegalArgumentException ex) {
                
                String message = ". Please check the position "
                    + "to speaker overlap. Supported positions are: "
                    + getEnumValuesAsString(PositionOverlapEnum.values());
                
               throw new SearchServiceException(message);
            }
        }
    }

    /**
    * Sets the reptition position according to the speaker change.
    *
    * The repetition can occur before or after speaker change
    * (PositionSpeakerChangeEnum) and have a certain distance
    * to the speaker change (min, max).
    *
    * @param el repetition as xml object
    *
    * @throws org.zumult.query.SearchServiceException if the
    *           speaker change parameters are illegal or not supported
    */
    private void setPositionSpeakerChange(final Element el)
                    throws SearchServiceException {

        String positionSpeakerChangeType = el
            .getElementsByTagName(
                    Constants.REPETITION_POSITION_TO_SPEAKER_CHANGE_TYPE)
            .item(0)
            .getTextContent();

        String minDistance = el
            .getElementsByTagName(
              Constants.REPETITION_POSITION_TO_SPEAKER_CHANGE_MIN_DISTANCE)
            .item(0)
            .getTextContent();

        Integer minDistanceInt = toInteger(
          Constants.REPETITION_POSITION_TO_SPEAKER_CHANGE_MIN_DISTANCE,
          minDistance);

        String maxDistance = el
                .getElementsByTagName(
               Constants.REPETITION_POSITION_TO_SPEAKER_CHANGE_MAX_DISTANCE)
                .item(0)
                .getTextContent();

        Integer maxDistanceInt = toInteger(
            Constants.REPETITION_POSITION_TO_SPEAKER_CHANGE_MAX_DISTANCE,
          maxDistance);

        if (positionSpeakerChangeType != null
            && !positionSpeakerChangeType.isEmpty()
            && !positionSpeakerChangeType.equals("null")) {
            try {

                this.position.positionSpeakerChange.type =
                            PositionSpeakerChangeEnum
                            .valueOf(positionSpeakerChangeType);

                this.position.positionSpeakerChange.maxWordToken =
                        maxDistanceInt;

                this.position.positionSpeakerChange.minWordToken =
                        minDistanceInt;

            } catch (IllegalArgumentException ex) {
                
               String message =". Please check"
                    + " the specified position to speaker change."
                    + " Supported positions are: "
                    + getEnumValuesAsString(
                            PositionSpeakerChangeEnum.values());
                
                throw new SearchServiceException(message);
            }
        }
   }

    /**
     * Sets speaker options (same or another speaker + speaker metadata).
     *
     * @param el repetition as xml object
     *
     * @throws org.zumult.query.SearchServiceException if the
     *           speaker option parameters are illegal or not supported
     */
    private void setSpeakerOptions(final Element el)
            throws SearchServiceException {

        // set speaker value (true, false, null)
        String speakerValue = el
                .getElementsByTagName(Constants.REPETITION_SPEAKER)
                .item(0)
                .getTextContent();

        Boolean speakerBooleanValue = getBooleanFromString(
                Constants.REPETITION_SPEAKER,
                speakerValue);

        this.speaker.sameSpeakerAsSource = speakerBooleanValue;

        // set speaker metadata
        String metadataQueryString = el
                .getElementsByTagName(Constants.REPETITION_SPEAKER_METADATA)
                .item(0)
                .getTextContent();

        this.speaker.speakerMetadata = metadataQueryString
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&", "&&")
                .replace("|", "||");
    }

     /**
     * Sets the left and right repetition context, the distance to it and
     * specifies if the context should occur within the same speaker
     * contribution as the repetition itself or not.
     *
     * @param el repetition as xml object
     *
     * @throws org.zumult.query.SearchServiceException if the
     *           context parameters are illegal or not supported
     */
    private void setRepetitionContext(final Element el)
            throws SearchServiceException {

        // set the left repetition context
        String precededby = el
                .getElementsByTagName(Constants.REPETITION_CONTEXT_LEFT)
                .item(0).getTextContent();

        this.context.precededby = precededby.trim()
                .replace("&lt;", "<")
                .replace("&gt;", ">");

        // set the right repetition context
        String followedby = el
                .getElementsByTagName(Constants.REPETITION_CONTEXT_RIGHT)
                .item(0).getTextContent();

        this.context.followedby = followedby.trim()
                .replace("&lt;", "<")
                .replace("&gt;", ">");

        // set left repetition context distance
        String leftMinDist = el
                .getElementsByTagName(
                        Constants.REPETITION_CONTEXT_LEFT_MIN_DISTANCE)
                .item(0)
                .getTextContent();

        Integer leftMinDistInt = toInteger(
                Constants.REPETITION_CONTEXT_LEFT_MIN_DISTANCE,
                leftMinDist);

        String leftMaxDist = el
                .getElementsByTagName(
                        Constants.REPETITION_CONTEXT_LEFT_MAX_DISTANCE)
                .item(0)
                .getTextContent();

        Integer leftMaxDistInt = toInteger(
                Constants.REPETITION_CONTEXT_LEFT_MAX_DISTANCE,
                   leftMaxDist);

        if (leftMinDistInt > leftMaxDistInt) {
            throw new SearchServiceException("Please check "
                    + "the minDistance to the left context. "
                    + "It can not be greater than maxDistance");
        }

        this.context.distanceLeft.minDistance = leftMinDistInt;
        this.context.distanceLeft.maxDistance = leftMaxDistInt;

        // set right repetition context distance
        String rightMinDist = el
            .getElementsByTagName(
                    Constants.REPETITION_CONTEXT_RIGHT_MIN_DISTANCE)
            .item(0)
            .getTextContent();

        Integer rightMinDistInt = toInteger(
                Constants.REPETITION_CONTEXT_RIGHT_MIN_DISTANCE,
               rightMinDist);

        String rightMaxDist = el
            .getElementsByTagName(
                    Constants.REPETITION_CONTEXT_RIGHT_MAX_DISTANCE)
            .item(0)
            .getTextContent();

        Integer rightMaxDistInt = toInteger(
                Constants.REPETITION_CONTEXT_RIGHT_MAX_DISTANCE,
              rightMaxDist);

        if (rightMinDistInt > rightMaxDistInt) {
            throw new SearchServiceException("Please check "
                    + "the minDistance to the right context. "
                    + "It can not be greater than maxDistance");
        }

        this.context.distanceRight.minDistance = rightMinDistInt;
        this.context.distanceRight.maxDistance = rightMaxDistInt;

        // should the left context occur within the same contribution?
        String withinLeft = el
            .getElementsByTagName(
              Constants.REPETITION_CONTEXT_WITHIN_SPEAKER_CONTRIBUTION_LEFT)
            .item(0)
            .getTextContent();

        Boolean withinLeftBooleanValue = getBooleanFromString(
                Constants.REPETITION_CONTEXT_WITHIN_SPEAKER_CONTRIBUTION_LEFT,
                withinLeft);

        this.context.withinSpeakerContributionLeft = withinLeftBooleanValue;

        // should the right context occur within the same contribution?
        String withinRight = el
                .getElementsByTagName(
             Constants.REPETITION_CONTEXT_WITHIN_SPEAKER_CONTRIBUTION_RIGHT)
                .item(0)
                .getTextContent();

        Boolean withinRightBooleanValue = getBooleanFromString(
             Constants.REPETITION_CONTEXT_WITHIN_SPEAKER_CONTRIBUTION_RIGHT,
             withinRight);

        this.context.withinSpeakerContributionRight = withinRightBooleanValue;
    }

    /**
     * Sets the min and max distance between repetitions.
     *
     * @param el repetition as xml object
     *
     * @throws org.zumult.query.SearchServiceException if the
     *           min and max distance parameters are illegal
     */
    private void setDistance(final Element el) throws SearchServiceException {
        String min = el
                .getElementsByTagName(Constants.REPETITION_MIN_DISTANCE)
                .item(0)
                .getTextContent();

        Integer minInteger = toInteger(
                Constants.REPETITION_MIN_DISTANCE,
                min);

        String max = el
                .getElementsByTagName(Constants.REPETITION_MAX_DISTANCE)
                .item(0)
                .getTextContent();

        Integer maxInteger = toInteger(
                Constants.REPETITION_MAX_DISTANCE,
                max);

        if (minInteger > maxInteger) {
            throw new SearchServiceException("Please check "
                    + "the minDistance. "
                    + "It can not be greater than maxDistance");
        }

        this.distanceToPreviousElement.minDistance = minInteger;
        this.distanceToPreviousElement.maxDistance = maxInteger;
    }

    /**
     * Converts the string value to Boolean taking zero value
     * into account and generates an option-sensitive error message.
     *
     * @param key option name
     * @param value string to be converted to integer
     * @return Boolean
     */
    private Boolean getBooleanFromString(final String key, final String value)
            throws SearchServiceException {

        if (value == null || value.isEmpty()) {
            return null;
        } else {
            switch (value) {
                case "false" -> {
                    return false;
                }
                case "true" -> {
                    return true;
                }
                case "null" -> {
                    return null;
                }
                default -> throw new SearchServiceException(
                                        "Please check the value of "
                                        + key
                                        + "! It can be true, false or null");
            }
        }
    }

    /**
     * Converts the string value to integer taking zero value
     * into account and generates a option-sensitive error message.
     *
     * @param key option name
     * @param value string to be converted to integer
     * @return Integer
     */
    private Integer toInteger(final String key, final String value)
            throws SearchServiceException {

        if (value == null || value.isEmpty()) {
            return null;
        } else {
            if (value.equals("null")) {
                return null;
            } else {
                try {
                    return Integer.valueOf(value);
                } catch (NumberFormatException ex) {
                    throw new SearchServiceException(value
                            + " is not valid for " + key + "!");
                }
            }
        }
    }

    private class Speaker {
        /**
         * Value of the speaker option when searching repetitions.
         * Possible values are:
         * <p>
         * <i>true</i> if the element we are looking for should
         * be repeated by the same speaker.
         * <p>
         * <i>false</i> if the element we are looking for
         * should be repeated by another speaker.
         * <p>
         * <i>null</i> if the speaker producing the repetition is not important.
         */
        private Boolean sameSpeakerAsSource;

        /**
         * Metadata query string, for example
         * &lt;s_geschlecht=&quot;Männlich&quot;/&gt; {@literal &}
         *  (&lt;ses_rolle_s=&quot;Tutor/in&quot;/&gt; |
         *  &lt;ses_rolle_s=&quot;Lehrer/in&quot;/&gt;).
         */
        private String speakerMetadata;
    }

    private class Position {
        /**
        * Object of the {@link PositionOverlapEnum} class. It specifies
        * the repetition position relative to speaker overlap.
        */
        private PositionOverlapEnum positionOverlap;
        /**
        * Object of the {@link PositionSpeakerChange} class. It specifies
        * the repetition position relative to speaker change.
        */
        private PositionSpeakerChange positionSpeakerChange =
                new PositionSpeakerChange();
    }

    private class Context {
        /**
         * Query string matching the left context.
         */
        private String precededby;

        /**
         * Query string matching the right context.
         */
        private String followedby;

        /**
         * Specifies if the left context should be within the same
         * speaker contribution as the repetition.
         */
        private Boolean withinSpeakerContributionLeft = null;

        /**
         * Specifies if the right context should be within the same
         * speaker contribution as the repetition.
         */
        private Boolean withinSpeakerContributionRight = null;

        /**
         * Object of the {@link Distance} class. Specifies the left distance.
         */
        private Distance distanceLeft = new Distance();

        /**
         * Object of the {@link Distance} class. Specifies the right distance.
         */
        private Distance distanceRight = new Distance();

    }

    private class Distance {

        /**
         * The minimum number of word tokens.
         */
        private Integer minDistance; //if null, then irrelevant

        /**
         * The maximum number of word tokens.
         */
        private Integer maxDistance; //if null, then irrelevant

        /**
         * Set of custom defined POS values to be ignored
         * when measuring distance.
         */
        private Set<String> ignoredCustomPOS = new HashSet();

        /**
         * Specifies if speaker change is reqiured.
         * <p>
         * <i>true</i> - speaker change is required
         * <p>
         * <i>false</i> - speaker change is not allowed
         * <p>
         * <i>null</i> - speaker change is irrelevant
         *
         */
        private Boolean speakerChange;
    }

    private class PositionSpeakerChange {
        /**
        * Object of the {@link PositionSpeakerChangeEnum} class. It specifies
        * if the repetition should occur before or after a speaker overlap.
        */
        private PositionSpeakerChangeEnum type;

        /**
         * The maximum number of word tokens up to speaker change.
         */
        private int maxWordToken;

        /**
         * The minimum number of word tokens up to speaker change.
         */
        private int minWordToken;
    }

    /**
     * Specifies the position of the repetition relative to the speaker overlap.
     */
    public enum PositionOverlapEnum {
        /**
         * The repetition should be located within speaker overlaps.
         */
        WITHIN,
        /**
         * The repetition should be located outside
         * of speaker overlaps.
         */
        NOT_WITHIN,
        /**
         * The repetition should intersect a speaker overlap.
         */
        INTERSECTING,
        /**
         * The repetition should be preceed a speaker overlap.
         */
        FOLLOWEDBY,
        /**
         * The repetition should follow a speaker overlap.
         *
         */
        PRECEDEDBY
    }

    /**
     * Specifies the position of the repetition relative to the speaker change.
     * 
     * Positions that can be used
     * {@link #FOLLOWEDBY}
     * {@link #PRECEDEDBY}
     */
    public enum PositionSpeakerChangeEnum {
        /**
         * The position of the repetition is located before the speaker change.
         */
        FOLLOWEDBY,
        /**
         * The position of the repetition is located after the speaker change.
         */
        PRECEDEDBY
    }

    /**
    * Specifies the items that should be included
    * from GermaNet when used in the repetition search.
    *
    *  GermaNet items that can be used
    *  {@link #SYN}
    *  {@link #HYPO}
    *  {@link #HYPER}
    *  {@link #COMP}
    */
    public enum GermaNetModeEnum {
        /**
         * Synonyms.
         */
        SYN {
            @Override
            public String toString(){
                return "Synonyms";
            }
        },
        /** 
         * Hyponyms.
         */
        HYPO {
            @Override
            public String toString(){
                return "Hyponyms";
            }
        },
        /**
         * Hypernyms.
         */
        HYPER {
            @Override
            public String toString(){
                return "Hypernyms";
            }
        },
        /**
         * Compounds.
         */
        COMP {
            @Override
            public String toString(){
                return "Compounds";
            }
        };
        

    }
    
    /**
    *  Specifies the annotation level for searching repetitions.
    *
    *  Repetition types that can be used
    *  {@link #LEMMA}
    *  {@link #WORD}
    *  {@link #NORM}
    */
    public enum RepetitionTypeEnum {
        /**
        * Repetitions will be searched by comparing lemmas.
        */
        LEMMA,
        /**
        * Repetitions will be searched by comparing transcripbed forms.
        */
        WORD,
        /**
        * Repetitions will be searched by comparing normalized forms.
        */
        NORM
    }

    private class SimilarityObject {
        SimilarityTypeEnum type = SimilarityTypeEnum.EQUAL;
        Set<GermaNetModeEnum> germaNetMode = new HashSet();
        double min = 75.0;
        double max = 100.0;
    }
    
    /**
    *  Repetition search mode.
    *
    * There are different possibilities to identify repetitions,
    * for example by comparing
    *
    *  Sililarity types that can be used
    *  {@link #EQUAL}
    *  {@link #FUZZY}
    *  {@link #FUZZY_PLUS}
    *  {@link #DIFF_PRON}
    *  {@link #DIFF_NORM}
    *  {@link #OWN_LEMMA_LIST}
    *  {@link #GERMANET}
    *  {@link #GERMANET_PLUS}
    */
    public enum SimilarityTypeEnum {
        /**
         * The word token sequence is recognized as a repetition
         * when all token forms exactly match those in the source element.
         */
        EQUAL,
        /**
         * Similarity measure are used to identify repetitions and
         * repetitions are not allowed to match exactly the source element.
         */
        FUZZY,
        /**
         * Similarity measure are used to identify repetitions.
         */
        FUZZY_PLUS,
        /**
         * The word token sequence is recognized as a repetition
         * when all token forms exactly match those in the source element,
         * but have different transcribed forms.
         */
        JACCARD_DISTANCE,
        JACCARD_DISTANCE_COLOGNE_PHONETIC,
        JARO_WINKLER_DISTANCE,
        LEVENSHTEIN_DISTANCE,
        ZUMULT_MIX,
        DIFF_PRON,
        /**
         * The word token sequence is recognized as a repetition
         * when all token forms exactly match those in the source element,
         * but have different normalized forms.
         */
        DIFF_NORM,
        /**
         * The word token sequence is recognized as a repetition
         * when all token forms exactly match those in the source element.
         * Additionally, the custom list of synonyms will be used.
         */
        OWN_LEMMA_LIST,
        /**
         * The word token sequence is recognized as a repetition
         * when all token forms exactly match those in the source element.
         * Additionally, GermaNet will be used.
         */
        GERMANET_PLUS,
        /**
         * GermaNet is used to identify repetitions.
         */
        GERMANET
    }

    public class Synonyms extends ArrayList<ArrayList<String>> {
    }

    private String deleteLastComma(final String str) {
        return str.replaceFirst(",$", "");
    }
    
    private <T extends Enum<T>> String getEnumValuesAsString(T[] values) {
        StringBuilder sb = new StringBuilder();
        for (T ob: values) {
            sb.append(ob.name());
            sb.append(", ");
        }
        return deleteLastComma(sb.toString().trim());
    }
}
