/*******************************************************************************
 * This Java class contains the code written by Matthijs Brouwer for MTAS - an open source Lucene-based
 * search engine for querying on text with multilevel annotations. 
 * 
 * https://textexploration.github.io/mtas/index.html
 * 
 * For the ZuMult-purpose, MTAS-Parser was expanded to support 
 * new filters (html, ceil) and to treat time anchors as separate elements rather than words/tokens
 * 
 *******************************************************************************/
package org.zumult.query.searchEngine.parser;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import mtas.analysis.token.MtasToken;
import mtas.analysis.token.MtasTokenCollection;
import mtas.analysis.token.MtasTokenIdFactory;
import mtas.analysis.token.MtasTokenString;
import mtas.analysis.util.MtasConfigException;
import mtas.analysis.util.MtasConfiguration;
import mtas.analysis.util.MtasParserException;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.util.BytesRef;
import org.zumult.io.IOHelper;

/**
 *
 * @author Elena
 */
final public class ZuMultParser extends MtasTEIParser {
    
    int notIndexedTokens = 0;
    private static final Logger logger = Logger.getLogger(ZuMultParser.class.getName());
    private static final Log log = LogFactory.getLog(ZuMultParser.class);
    
    protected static final String MAPPING_TYPE_ANCHOR = "anchor";
    protected static final String MAPPING_TYPE_PRECEDED_WORD = "precededWord";
    protected static final String MAPPING_FILTER_CEIL = "ceil";
    protected static final String MAPPING_FILTER_HTML = "html";
    
    public ZuMultParser(MtasConfiguration config) {
        super(config);
    }

    @Override
    protected void initParser() throws MtasConfigException {
        super.initParser();
        //System.out.print(printConfig());
    }
   
    @Override
    protected Map<String, List<MtasParserObject>> createCurrentList() {
        Map<String, List<MtasParserObject>> currentList = super.createCurrentList();
        currentList.put(MAPPING_TYPE_PRECEDED_WORD, new ArrayList<MtasParserObject>());
        currentList.put(MAPPING_TYPE_ANCHOR, new ArrayList<MtasParserObject>());
        return currentList;
  }
    
    @Override
    public MtasTokenCollection createTokenCollection(Reader reader) throws MtasParserException, MtasConfigException {
    Boolean hasRoot = rootTag == null ? true : false;
    Boolean parsingContent = contentTag == null ? true : false;
    String textContent = null;
    Integer unknownAncestors = 0;
    Integer lastOffset = 0;

    AtomicInteger position = new AtomicInteger(0);
    Map<String, Set<Integer>> idPositions = new HashMap<>();
    Map<String, Integer[]> idOffsets = new HashMap<>();

    Map<String, Map<Integer, Set<String>>> updateList = createUpdateList();
    Map<String, List<MtasParserObject>> currentList = createCurrentList();
    Map<String, LinkedHashMap<String, String>> variables = new HashMap<>(); //EF

    tokenCollection = new MtasTokenCollection();
    MtasTokenIdFactory mtasTokenIdFactory = new MtasTokenIdFactory();
    XMLInputFactory factory = XMLInputFactory.newInstance();
    try {
      XMLStreamReader streamReader = factory.createXMLStreamReader(reader);
      QName qname;
      try {
        int event = streamReader.getEventType();
        MtasParserType<?> currentType;
        MtasParserType<?> tmpCurrentType;
        MtasParserType<?> tmpVariableType;
        MtasParserObject currentObject = null;
        MtasParserObject variableObject = null;
        while (true) {
          switch (event) {
            case XMLStreamConstants.START_DOCUMENT:
              log.debug("start of document");
              String encodingScheme = streamReader.getCharacterEncodingScheme();
              if (encodingScheme == null) {
                // ignore for now
                log.info("No encodingScheme found, assume utf-8");
                // throw new MtasParserException("No encodingScheme found");
              } else if (!encodingScheme.equalsIgnoreCase("utf-8")) {
                throw new MtasParserException("XML not UTF-8 encoded but '" + encodingScheme + "'");
              }
              break;
            case XMLStreamConstants.END_DOCUMENT:
              log.debug("end of document");
              break;
            case XMLStreamConstants.SPACE:
              // set offset (end of start-element)
              lastOffset = streamReader.getLocation().getCharacterOffset();
              break;
            case XMLStreamConstants.START_ELEMENT:
              // get data
              qname = streamReader.getName();
              // check for rootTag
              if (!hasRoot) {
                if (qname.equals(getQName(rootTag))) {
                  hasRoot = true;
                } else {
                  throw new MtasParserException("No " + rootTag);
                }
                // parse content
              } else {
                if ((tmpVariableType = variableTypes.get(qname)) != null) {
                  variableObject = new MtasParserObject(tmpVariableType);
                  collectAttributes(variableObject, streamReader);
                  computeVariablesFromObj(variableObject, currentList, variables); //EF

                }
                if (parsingContent) {
                  // check for relation : not within word, not within
                  // groupAnnotation
                  if ((currentList.get(MAPPING_TYPE_WORD).isEmpty())
                      && (currentList.get(MAPPING_TYPE_GROUP_ANNOTATION).isEmpty())
                      && (tmpCurrentType = relationTypes.get(qname)) != null) {
                    currentObject = new MtasParserObject(tmpCurrentType);
                    collectAttributes(currentObject, streamReader);
                    currentObject.setUnknownAncestorNumber(unknownAncestors);
                    currentObject.setRealOffsetStart(lastOffset);
                    if (!prevalidateObject(currentObject, currentList)) {
                      unknownAncestors++;
                    } else {
                      currentType = tmpCurrentType;
                      currentList.get(MAPPING_TYPE_RELATION).add(currentObject);
                      unknownAncestors = 0;
                    }
                    // check for relation annotation: not within word, but within
                    // relation
                  } else if ((currentList.get(MAPPING_TYPE_WORD).isEmpty())
                      && (!currentList.get(MAPPING_TYPE_RELATION).isEmpty())
                      && (tmpCurrentType = relationAnnotationTypes.get(qname)) != null) {
                    currentObject = new MtasParserObject(tmpCurrentType);
                    collectAttributes(currentObject, streamReader);
                    currentObject.setUnknownAncestorNumber(unknownAncestors);
                    currentObject.setRealOffsetStart(lastOffset);
                    if (!prevalidateObject(currentObject, currentList)) {
                      unknownAncestors++;
                    } else {
                      currentType = tmpCurrentType;
                      currentList.get(MAPPING_TYPE_RELATION_ANNOTATION).add(currentObject);
                      unknownAncestors = 0;
                    }
                    // check for group: not within word, not within relation, not
                    // within groupAnnotation
                  } else if ((currentList.get(MAPPING_TYPE_WORD).isEmpty())
                      && (currentList.get(MAPPING_TYPE_RELATION).isEmpty())
                      && (currentList.get(MAPPING_TYPE_GROUP_ANNOTATION).isEmpty())
                      && (tmpCurrentType = groupTypes.get(qname)) != null) {
                    currentObject = new MtasParserObject(tmpCurrentType);
                    collectAttributes(currentObject, streamReader);
                    currentObject.setUnknownAncestorNumber(unknownAncestors);
                    currentObject.setRealOffsetStart(lastOffset);
                    if (!prevalidateObject(currentObject, currentList)) {
                      unknownAncestors++;
                    } else {
                      currentType = tmpCurrentType;
                      currentList.get(MAPPING_TYPE_GROUP).add(currentObject);
                      unknownAncestors = 0;
                    }
                    // check for group annotation: not within word, not within
                    // relation, but within group
                  } else if ((currentList.get(MAPPING_TYPE_WORD).isEmpty())
                      && (currentList.get(MAPPING_TYPE_RELATION).isEmpty())
                      && (!currentList.get(MAPPING_TYPE_GROUP).isEmpty())
                      && (tmpCurrentType = groupAnnotationTypes.get(qname)) != null) {
                    currentObject = new MtasParserObject(tmpCurrentType);
                    collectAttributes(currentObject, streamReader);
                    currentObject.setUnknownAncestorNumber(unknownAncestors);
                    currentObject.setRealOffsetStart(lastOffset);
                    if (!prevalidateObject(currentObject, currentList)) {
                      unknownAncestors++;
                    } else {
                      currentType = tmpCurrentType;
                      currentList.get(MAPPING_TYPE_GROUP_ANNOTATION).add(currentObject);
                      unknownAncestors = 0;
                    }
                    // check for word: not within relation, not within
                    // groupAnnotation, not within word, not within wordAnnotation
                  } else if ((currentList.get(MAPPING_TYPE_RELATION).isEmpty())
                      && (currentList.get(MAPPING_TYPE_GROUP_ANNOTATION).isEmpty())
                      && (currentList.get(MAPPING_TYPE_WORD).isEmpty())
                      && (currentList.get(MAPPING_TYPE_WORD_ANNOTATION).isEmpty())
                      && (tmpCurrentType = wordTypes.get(qname)) != null) {
                    currentObject = new MtasParserObject(tmpCurrentType);
                    collectAttributes(currentObject, streamReader);
                    currentObject.setUnknownAncestorNumber(unknownAncestors);
                    currentObject.setOffsetStart(lastOffset);
                    currentObject.setRealOffsetStart(lastOffset);
                    if (!prevalidateObject(currentObject, currentList)) {
                      unknownAncestors++;
                    } else {
                      currentType = tmpCurrentType;
                      currentObject.addPosition(position.getAndIncrement());
                      currentList.get(MAPPING_TYPE_WORD).add(currentObject);
                      unknownAncestors = 0;
                    }

                                        // EF: check for start anchor

                                        if (!currentList.get(MAPPING_TYPE_ANCHOR).isEmpty()){

                                            MtasParserObject currentAnchor = currentList.get(MAPPING_TYPE_ANCHOR)
                                            .remove(currentList.get(MAPPING_TYPE_ANCHOR).size() - 1);
                                            
                                            if (currentList.get(MAPPING_TYPE_ANCHOR).size()>0){
                                                currentList.get(MAPPING_TYPE_ANCHOR).clear(); // it can be only one start anchor (s. c84 in FOLK_E_00287_SE_01_T_03_DF_01)
                                            }

                                            MtasParserObject parentGroup = currentList.get(MAPPING_TYPE_GROUP)
                                                    .get(currentList.get(MAPPING_TYPE_GROUP).size() - 1);

                                            parentGroup.anchorStartPosition.put(currentAnchor.getType().getName(), currentObject.getId());


                                        }else if (tmpCurrentType.getName().equals("pause")
                                                || tmpCurrentType.getName().equals("incident")
                                                || tmpCurrentType.getName().equals("vocal")){

                                            if (!currentList.get(MAPPING_TYPE_WORD).isEmpty() && 
                                                  (currentList.get(MAPPING_TYPE_GROUP).size() == 1)){ // only body (pause within annotationBlock is handled as word)

                                                String anchor_start = null;
                                                String anchor_end = null;
                                                for (int i = 0; i < streamReader.getAttributeCount(); i++) {
                                                    if (streamReader.getAttributeLocalName(i).equals("start")){
                                                        anchor_start = streamReader.getAttributeValue(i);

                                                    }else if (streamReader.getAttributeLocalName(i).equals("end")){
                                                        anchor_end = streamReader.getAttributeValue(i);
                                                    }
                                                }

                                                MtasParserObject parentGroup = currentList.get(MAPPING_TYPE_GROUP)
                                                      .get(currentList.get(MAPPING_TYPE_GROUP).size() - 1);

                                                MtasParserObject parentWord = currentList.get(MAPPING_TYPE_WORD)
                                                  .get(currentList.get(MAPPING_TYPE_WORD).size() - 1);

                                                parentGroup.anchorStartPosition.put(anchor_start, parentWord.getId());
                                                parentGroup.anchorEndPosition.put(anchor_end, parentWord.getId());

                                                ArrayList<String> vars = getTimelineVarsBetween(variables, anchor_start, anchor_end);
                                                for (String var: vars){
                                                    parentGroup.anchorStartPosition.put(var, parentWord.getId());
                                                    parentGroup.anchorEndPosition.put(var, parentWord.getId());
                                                }
                                            }
                                        } else{
                                            //No start anchor
                                        }



                    // check for word annotation: not within relation, not within
                    // groupAnnotation, but within word
                  } else if ((currentList.get(MAPPING_TYPE_RELATION).isEmpty())
                      && (currentList.get(MAPPING_TYPE_GROUP_ANNOTATION).isEmpty())
                      && (!currentList.get(MAPPING_TYPE_WORD).isEmpty())
                      && (tmpCurrentType = wordAnnotationTypes.get(qname)) != null) {
                    currentObject = new MtasParserObject(tmpCurrentType);
                    collectAttributes(currentObject, streamReader);
                    currentObject.addPositions(currentList.get(MAPPING_TYPE_WORD)
                        .get((currentList.get(MAPPING_TYPE_WORD).size() - 1)).getPositions());
                    currentObject.setUnknownAncestorNumber(unknownAncestors);
                    currentObject.setRealOffsetStart(lastOffset);
                    if (!prevalidateObject(currentObject, currentList)) {
                      unknownAncestors++;
                    } else {
                      currentType = tmpCurrentType;
                      currentList.get(MAPPING_TYPE_WORD_ANNOTATION).add(currentObject);
                      unknownAncestors = 0;
                    }
                    // check for references: within relation
                  } else if (!currentList.get(MAPPING_TYPE_RELATION).isEmpty()
                      && (tmpCurrentType = refTypes.get(qname)) != null) {
                    currentObject = new MtasParserObject(tmpCurrentType);
                    collectAttributes(currentObject, streamReader);
                    currentObject.setUnknownAncestorNumber(unknownAncestors);
                    currentObject.setRealOffsetStart(lastOffset);
                    if (!prevalidateObject(currentObject, currentList)) {
                      unknownAncestors++;
                    } else {
                      currentType = tmpCurrentType;
                      currentList.get(MAPPING_TYPE_REF).add(currentObject);
                      unknownAncestors = 0;
                      // add reference to ancestor relations
                      for (MtasParserObject currentRelation : currentList.get(MAPPING_TYPE_RELATION)) {
                        currentRelation.addRefId(currentObject.getAttribute(currentType.getRefAttributeName()));
                        // register mapping for relation (for recursive relations)
                        SortedSet<String> keyMapList;
                        if (currentRelation.getId() != null) {
                          if (relationKeyMap.containsKey(currentRelation.getId())) {
                            keyMapList = relationKeyMap.get(currentRelation.getId());
                          } else {
                            keyMapList = new TreeSet<>();
                            relationKeyMap.put(currentRelation.getId(), keyMapList);
                          }
                          keyMapList.add(currentObject.getAttribute(currentType.getRefAttributeName()));
                        }
                      }
                    }
                                        //EF: check for anchors
                                    } else if (qname.getLocalPart().equals(MAPPING_TYPE_ANCHOR)) {

                                        String anchor = null;
                                        for (int i = 0; i < streamReader.getAttributeCount(); i++) {
                                            if (streamReader.getAttributeLocalName(i).equals("synch")){
                                                anchor = streamReader.getAttributeValue(i);
                                            }
                                        }

                                        MtasParserObject parentGroup = currentList.get(MAPPING_TYPE_GROUP)
                                                  .get(currentList.get(MAPPING_TYPE_GROUP).size() - 1);

                                        if (!currentList.get(MAPPING_TYPE_WORD).isEmpty()){

                                            MtasParserObject parentWord = currentList.get(MAPPING_TYPE_WORD)
                                              .get(currentList.get(MAPPING_TYPE_WORD).size() - 1);

                                            parentGroup.anchorStartPosition.put(anchor, parentWord.getId());
                                            parentGroup.anchorEndPosition.put(anchor, parentWord.getId());

                                        } else if (currentList.get(MAPPING_TYPE_WORD).isEmpty() 
                                                && !currentList.get(MAPPING_TYPE_GROUP).isEmpty()){

                                            MtasParserType anchorType = new MtasParserType(MAPPING_TYPE_ANCHOR, anchor, false);
                                            MtasParserObject anchorObject = new MtasParserObject(anchorType);
                                            currentList.get(MAPPING_TYPE_ANCHOR).add(anchorObject);

                                            if (!currentList.get(MAPPING_TYPE_PRECEDED_WORD).isEmpty()){
                                                MtasParserObject precededWord = currentList.get(MAPPING_TYPE_PRECEDED_WORD)
                                                  .remove(currentList.get(MAPPING_TYPE_PRECEDED_WORD).size() - 1);
                                                parentGroup.anchorEndPosition.put(anchor, precededWord.getId());
                                            }
                                        }

                    unknownAncestors++;
                  }

                                    else {
                                      unknownAncestors++;
                                    }

                  // check for start content
                } else if (qname.equals(getQName(contentTag))) {
                  parsingContent = true;
                  // unexpected
                } else if (!allowNonContent) {
                  throw new MtasParserException("Unexpected " + qname.getLocalPart() + " in document");
                }
              }
              // set offset (end of start-element)
              lastOffset = streamReader.getLocation().getCharacterOffset();
              break;
            case XMLStreamConstants.END_ELEMENT:
              // set offset (end of end-element)
              lastOffset = streamReader.getLocation().getCharacterOffset();
              // get data
              qname = streamReader.getName();
              // parse content
              if (parsingContent) {
                if (unknownAncestors > 0) {
                  unknownAncestors--;
                  // check for reference: because otherwise currentList should
                  // contain no references
                } else if (!currentList.get(MAPPING_TYPE_REF).isEmpty()) {
                  if ((currentType = refTypes.get(qname)) != null) {
                    currentObject = currentList.get(MAPPING_TYPE_REF)
                        .remove(currentList.get(MAPPING_TYPE_REF).size() - 1);
                    assert currentObject.getType().equals(currentType) : "object expected to be "
                        + currentObject.getType().getName() + ", not " + currentType.getName();
                    assert unknownAncestors == 0 : "error in administration " + currentObject.getType().getName();
                    // ignore text and realOffset: not relevant
                    idPositions.put(currentObject.getId(), currentObject.getPositions());
                    idOffsets.put(currentObject.getId(), currentObject.getOffset());
                    currentObject.updateMappings(idPositions, idOffsets);
                    unknownAncestors = currentObject.getUnknownAncestorNumber();
                    computeMappingsFromObject(mtasTokenIdFactory, currentObject, currentList, updateList);
                  } else {
                    // this shouldn't happen
                  }
                  // check for wordAnnotation: because otherwise currentList
                  // should contain no wordAnnotations
                } else if (!currentList.get(MAPPING_TYPE_WORD_ANNOTATION).isEmpty()) {
                  if ((currentType = wordAnnotationTypes.get(qname)) != null) {
                    currentObject = currentList.get(MAPPING_TYPE_WORD_ANNOTATION)
                        .remove(currentList.get(MAPPING_TYPE_WORD_ANNOTATION).size() - 1);
                    assert currentObject.getType().equals(currentType) : "object expected to be "
                        + currentObject.getType().getName() + ", not " + currentType.getName();
                    assert unknownAncestors == 0 : "error in administration " + currentObject.getType().getName();
                    currentObject.setRealOffsetEnd(lastOffset);
                    idPositions.put(currentObject.getId(), currentObject.getPositions());
                    idOffsets.put(currentObject.getId(), currentObject.getOffset());
                    // offset always null, so update later with word (should be
                    // possible)
                    if ((currentObject.getId() != null) && (!currentList.get(MAPPING_TYPE_WORD).isEmpty())) {
                      currentList.get(MAPPING_TYPE_WORD).get((currentList.get(MAPPING_TYPE_WORD).size() - 1))
                          .addUpdateableIdWithOffset(currentObject.getId());
                    }
                    currentObject.updateMappings(idPositions, idOffsets);
                    unknownAncestors = currentObject.getUnknownAncestorNumber();
                    computeMappingsFromObject(mtasTokenIdFactory, currentObject, currentList, updateList);
                  } else {
                    // this shouldn't happen
                  }
                  // check for word: because otherwise currentList should contain
                  // no words
                } else if (!currentList.get(MAPPING_TYPE_WORD).isEmpty()) {
                  if ((currentType = wordTypes.get(qname)) != null) {
                    currentObject = currentList.get(MAPPING_TYPE_WORD)
                        .remove(currentList.get(MAPPING_TYPE_WORD).size() - 1);
                    assert currentObject.getType().equals(currentType) : "object expected to be "
                        + currentObject.getType().getName() + ", not " + currentType.getName();
                    assert unknownAncestors == 0 : "error in administration " + currentObject.getType().getName();
                    currentObject.setOffsetEnd(lastOffset);
                    currentObject.setRealOffsetEnd(lastOffset);
                    // update ancestor groups with position and offset
                    for (MtasParserObject currentGroup : currentList.get(MAPPING_TYPE_GROUP)) {
                      currentGroup.addPositions(currentObject.getPositions());
                      currentGroup.addOffsetStart(currentObject.getOffsetStart());
                      currentGroup.addOffsetEnd(currentObject.getOffsetEnd());
                    }
                    idPositions.put(currentObject.getId(), currentObject.getPositions());
                    idOffsets.put(currentObject.getId(), currentObject.getOffset());
                    currentObject.updateMappings(idPositions, idOffsets);
                    unknownAncestors = currentObject.getUnknownAncestorNumber();
                    computeMappingsFromObject(mtasTokenIdFactory, currentObject, currentList, updateList);
                    //EF
                            if (!currentList.get(MAPPING_TYPE_PRECEDED_WORD).isEmpty()){
                                currentList.get(MAPPING_TYPE_PRECEDED_WORD).clear();
                            }
                            currentList.get(MAPPING_TYPE_PRECEDED_WORD).add(currentObject);

                  } else {
                    // this shouldn't happen
                  }
                  // check for group annotation: because otherwise currentList
                  // should contain no groupAnnotations
                } else if (!currentList.get(MAPPING_TYPE_GROUP_ANNOTATION).isEmpty()) {
                  if ((currentType = groupAnnotationTypes.get(qname)) != null) {
                    currentObject = currentList.get(MAPPING_TYPE_GROUP_ANNOTATION)
                        .remove(currentList.get(MAPPING_TYPE_GROUP_ANNOTATION).size() - 1);
                    assert currentObject.getType().equals(currentType) : "object expected to be "
                        + currentObject.getType().getName() + ", not " + currentType.getName();
                    assert unknownAncestors == 0 : "error in administration " + currentObject.getType().getName();
                    currentObject.setRealOffsetEnd(lastOffset);
                    idPositions.put(currentObject.getId(), currentObject.getPositions());
                    idOffsets.put(currentObject.getId(), currentObject.getOffset());
                    currentObject.updateMappings(idPositions, idOffsets);
                    unknownAncestors = currentObject.getUnknownAncestorNumber();
                    computeMappingsFromObject(mtasTokenIdFactory, currentObject, currentList, updateList);
                  } else {
                    // this shouldn't happen
                  }
                  // check for relation annotation
                } else if (!currentList.get(MAPPING_TYPE_RELATION_ANNOTATION).isEmpty()) {
                  if ((currentType = relationAnnotationTypes.get(qname)) != null) {
                    currentObject = currentList.get(MAPPING_TYPE_RELATION_ANNOTATION)
                        .remove(currentList.get(MAPPING_TYPE_RELATION_ANNOTATION).size() - 1);
                    assert currentObject.getType().equals(currentType) : "object expected to be "
                        + currentObject.getType().getName() + ", not " + currentType.getName();
                    assert unknownAncestors == 0 : "error in administration " + currentObject.getType().getName();
                    currentObject.setRealOffsetEnd(lastOffset);
                    idPositions.put(currentObject.getId(), currentObject.getPositions());
                    idOffsets.put(currentObject.getId(), currentObject.getOffset());
                    currentObject.updateMappings(idPositions, idOffsets);
                    unknownAncestors = currentObject.getUnknownAncestorNumber();
                    computeMappingsFromObject(mtasTokenIdFactory, currentObject, currentList, updateList);
                  } else {
                    // this shouldn't happen
                  }
                  // check for relation
                } else if (!currentList.get(MAPPING_TYPE_RELATION).isEmpty()) {
                  if ((currentType = relationTypes.get(qname)) != null) {
                    currentObject = currentList.get(MAPPING_TYPE_RELATION)
                        .remove(currentList.get(MAPPING_TYPE_RELATION).size() - 1);
                    assert currentObject.getType().equals(currentType) : "object expected to be "
                        + currentObject.getType().getName() + ", not " + currentType.getName();
                    assert unknownAncestors == 0 : "error in administration " + currentObject.getType().getName();
                    // ignore text: should not occur
                    currentObject.setRealOffsetEnd(lastOffset);
                    idPositions.put(currentObject.getId(), currentObject.getPositions());
                    idOffsets.put(currentObject.getId(), currentObject.getOffset());
                    currentObject.updateMappings(idPositions, idOffsets);
                    unknownAncestors = currentObject.getUnknownAncestorNumber();
                    computeMappingsFromObject(mtasTokenIdFactory, currentObject, currentList, updateList);
                  } else {
                    // this shouldn't happen
                  }
                  // check for group
                } else if (!currentList.get(MAPPING_TYPE_GROUP).isEmpty()) {
                  if ((currentType = groupTypes.get(qname)) != null) {
                    currentObject = currentList.get(MAPPING_TYPE_GROUP)
                        .remove(currentList.get(MAPPING_TYPE_GROUP).size() - 1);
                    assert currentObject.getType().equals(currentType) : "object expected to be "
                        + currentObject.getType().getName() + ", not " + currentType.getName();
                    assert unknownAncestors == 0 : "error in administration " + currentObject.getType().getName();
                    // ignore text: should not occur
                    currentObject.setRealOffsetEnd(lastOffset);
                    idPositions.put(currentObject.getId(), currentObject.getPositions());
                    idOffsets.put(currentObject.getId(), currentObject.getOffset());
                    currentObject.updateMappings(idPositions, idOffsets);
                    unknownAncestors = currentObject.getUnknownAncestorNumber();
                    computeMappingsFromObject(mtasTokenIdFactory, currentObject, currentList, updateList);
                     
                            // EF
                            updateList.get(UPDATE_TYPE_LOCAL_REF_POSITION_START).clear();
                            updateList.get(UPDATE_TYPE_LOCAL_REF_POSITION_END).clear();
                            updateList.get(UPDATE_TYPE_LOCAL_REF_OFFSET_START).clear();
                            updateList.get(UPDATE_TYPE_LOCAL_REF_OFFSET_END).clear();


                            if (!currentList.get(MAPPING_TYPE_ANCHOR).isEmpty()){
                                currentList.get(MAPPING_TYPE_ANCHOR).clear();
                            }
                  } else {
                    unknownAncestors--;
                  }
                } else if (qname.equals(getQName("text"))) {
                  parsingContent = false;
                  assert unknownAncestors == 0 : "error in administration unknownAncestors";
                  assert currentList.get(MAPPING_TYPE_REF).isEmpty() : "error in administration references";
                  assert currentList.get(MAPPING_TYPE_GROUP).isEmpty() : "error in administration groups";
                  assert currentList.get(MAPPING_TYPE_GROUP_ANNOTATION)
                      .isEmpty() : "error in administration groupAnnotations";
                  assert currentList.get(MAPPING_TYPE_WORD).isEmpty() : "error in administration words";
                  assert currentList.get(MAPPING_TYPE_WORD_ANNOTATION)
                      .isEmpty() : "error in administration wordAnnotations";
                  assert currentList.get(MAPPING_TYPE_RELATION).isEmpty() : "error in administration relations";
                  assert currentList.get(MAPPING_TYPE_RELATION_ANNOTATION)
                      .isEmpty() : "error in administration relationAnnotations";
                }
              }
              // forget text
              textContent = null;
              break;
            case XMLStreamConstants.CHARACTERS:
              // set offset (end of start-element)
              lastOffset = streamReader.getLocation().getCharacterOffset();
              // check for text
              if (streamReader.hasText()) {
                textContent = streamReader.getText();
              }
              if (currentObject != null && unknownAncestors.equals(0)) {
                currentObject.addText(textContent);
              }
              break;
            default:
              break;
          }
          if (!streamReader.hasNext()) {
            break;
          }
          event = streamReader.next();
        }
      } finally {
        streamReader.close();
      }
      // final checks
      assert unknownAncestors == 0 : "error in administration unknownAncestors";
      assert hasRoot : "no " + rootTag;
    } catch (XMLStreamException e) {
      log.debug(e);
      throw new MtasParserException("No valid XML: " + e.getMessage());
    }

    // update tokens with variable
    for (Map.Entry<Integer, Set<String>> updateItem : updateList.get(UPDATE_TYPE_VARIABLE).entrySet()) {
      MtasToken token = tokenCollection.get(updateItem.getKey());
      String encodedPrefix = token.getPrefix();
      String encodedPostfix = token.getPostfix();
                token.setValue(decodeAndUpdateWithVar(encodedPrefix, encodedPostfix, variables)); //EF

    }
    // update tokens with offset
    for (Map.Entry<Integer, Set<String>> updateItem : updateList.get(UPDATE_TYPE_OFFSET).entrySet()) {
      Set<String> refIdList = new HashSet<>();
      for (String refId : updateItem.getValue()) {
        if (idPositions.containsKey(refId)) {
          refIdList.add(refId);
        }
        if (relationKeyMap.containsKey(refId)) {
          refIdList.addAll(recursiveCollect(refId, relationKeyMap, 10));
        }
      }
      for (String refId : refIdList) {
        Integer[] refOffset = idOffsets.get(refId);
        Integer tokenId = updateItem.getKey();
        if (tokenId != null && refOffset != null) {
          MtasToken token = tokenCollection.get(tokenId);
          token.addOffset(refOffset[0], refOffset[1]);
        }
      }
    }
    // update tokens with position
    for (Map.Entry<Integer, Set<String>> updateItem : updateList.get(UPDATE_TYPE_POSITION).entrySet()) {
      HashSet<String> refIdList = new HashSet<>();
      for (String refId : updateItem.getValue()) {
        if (idPositions.containsKey(refId)) {
          refIdList.add(refId);
        }
        if (relationKeyMap.containsKey(refId)) {
          refIdList.addAll(recursiveCollect(refId, relationKeyMap, 10));
        }
      }
      for (String refId : refIdList) {
        Set<Integer> refPositions = idPositions.get(refId);
        Integer tokenId = updateItem.getKey();
        if (tokenId != null && refPositions != null) {
          MtasToken token = tokenCollection.get(tokenId);
          token.addPositions(refPositions);
        }
      }
    }

    // final check
    tokenCollection.check(autorepair, makeunique);


  //  System.out.println("**********START TOKEN COLLECTION***************** ");
   // tokenCollection.print();
   // System.out.println("**********END TOKEN COLLECTION (" + notIndexedTokens + " tokens were not indexed!) ***************** ");
    
    notIndexedTokens = 0;
    
    
    return tokenCollection;
  }

    @Override
    protected void computeMappingsFromObject(
      MtasTokenIdFactory mtasTokenIdFactory, MtasParserObject object,
      Map<String, List<MtasParserObject>> currentList,
      Map<String, Map<Integer, Set<String>>> updateList)
      throws MtasParserException, MtasConfigException {
    MtasParserType<MtasParserMapping<?>> objectType = object.getType();
    List<MtasParserMapping<?>> mappings = objectType.getItems();
    if (!object.updateableMappingsWithPosition.isEmpty()) {
      for (int tokenId : object.updateableMappingsWithPosition) {
        updateList.get(UPDATE_TYPE_POSITION).put(tokenId, object.getRefIds());
      }
    }
    if (!object.updateableMappingsWithOffset.isEmpty()) {
      for (int tokenId : object.updateableMappingsWithOffset) {
        updateList.get(UPDATE_TYPE_OFFSET).put(tokenId, object.getRefIds());
      }
    }
    for (MtasParserMapping<?> mapping : mappings) {
      try {
        if (mapping.getTokens().isEmpty()) {
          // empty exception
        } else {
          for (int i = 0; i < mapping.getTokens().size(); i++) {
            MtasParserMappingToken mappingToken = mapping.getTokens().get(i);
            // empty exception
            if (mappingToken.preValues.isEmpty()) {
              // continue, but no token
            } else {
              // check conditions
              postcheckMappingConditions(object, mapping.getConditions(),
                  currentList);
              boolean containsVariables = checkForVariables(
                  mappingToken.preValues);
              containsVariables = !containsVariables
                  ? checkForVariables(mappingToken.postValues)
                  : containsVariables;
              // construct preValue
              String[] preValue = computeValueFromMappingValues(object,
                  mappingToken.preValues, currentList, containsVariables);
              // at least preValue
              if (preValue == null || preValue.length == 0) {
                throw new MtasParserException("no preValues");
              } else {
                // no delimiter in preValue
                for (int k = 0; k < preValue.length; k++) {
                  if ((preValue[k] = preValue[k].replace(MtasToken.DELIMITER,
                      "")).isEmpty()) {
                    throw new MtasParserException("empty preValue");
                  }
                }
              }
              // construct postValue
              String[] postValue = computeValueFromMappingValues(object,
                  mappingToken.postValues, currentList, containsVariables);
              // construct value
              String[] value;
              if (postValue == null || postValue.length == 0) {
                if (postValue == null){ //EF: null only if trim filter used and value is empty, otherwise always empty postValue
                    System.out.print("postValue is null. The following MTASTokenString will not be created: ");
                    notIndexedTokens++;
                value = preValue.clone();
                for (int k = 0; k < value.length; k++) {
                  value[k] = value[k] + MtasToken.DELIMITER;
                }

                    value=null;
                }else{
                    value = preValue.clone();
                    for (int k = 0; k < value.length; k++) {
                       value[k] = value[k] + MtasToken.DELIMITER;
                    }
                }
              } else if (postValue.length == 1) {
                value = preValue.clone();
                for (int k = 0; k < value.length; k++) {
                  value[k] = value[k] + MtasToken.DELIMITER + postValue[0];
                }
              } else if (preValue.length == 1) {
                value = postValue.clone();
                for (int k = 0; k < value.length; k++) {
                  value[k] = preValue[0] + MtasToken.DELIMITER + value[k];
                }
              } else {
                value = new String[preValue.length * postValue.length];
                int number = 0;
                for (int k1 = 0; k1 < preValue.length; k1++) {
                  for (int k2 = 0; k2 < postValue.length; k2++) {
                    value[number] = preValue[k1] + MtasToken.DELIMITER
                        + postValue[k2];
                    number++;
                  }
                }
              }
              
              if (value!=null){ //EF: create MTASToken only if postValue is not null (empty is allowed)
              // construct payload
              BytesRef payload = computePayloadFromMappingPayload(object,
                  mappingToken.payload, currentList);
              // create token and get id: from now on, we must continue, no
              // exceptions allowed...
              for (int k = 0; k < value.length; k++) {
                MtasTokenString token = new MtasTokenString(
                    mtasTokenIdFactory.createTokenId(), value[k]);
                // store settings offset, realoffset and parent
                token.setProvideOffset(mappingToken.offset);
                token.setProvideRealOffset(mappingToken.realoffset);
                token.setProvideParentId(mappingToken.parent);
                String checkType = object.objectType.getType();
                // register token if it contains variables
                if (containsVariables) {
                  updateList.get(UPDATE_TYPE_VARIABLE).put(token.getId(), null);
                }
                // register id for update when parent is created
                if (!currentList.get(checkType).isEmpty()) {
                  if (currentList.get(checkType).contains(object)) {
                    int listPosition = currentList.get(checkType)
                        .indexOf(object);
                    if (listPosition > 0) {
                      currentList.get(checkType).get(listPosition - 1)
                          .registerUpdateableMappingAtParent(token.getId());
                    }
                  } else {
                    currentList.get(checkType)
                        .get(currentList.get(checkType).size() - 1)
                        .registerUpdateableMappingAtParent(token.getId());
                  }
                  // if no real ancestor, register id update when group
                  // ancestor is created
                } else if (!currentList.get(MAPPING_TYPE_GROUP).isEmpty()) {
                  currentList.get(MAPPING_TYPE_GROUP)
                      .get(currentList.get(MAPPING_TYPE_GROUP).size() - 1)
                      .registerUpdateableMappingAtParent(token.getId());
                } else if (!currentList.get(MAPPING_TYPE_RELATION).isEmpty()) {
                  currentList.get(MAPPING_TYPE_RELATION)
                      .get(currentList.get(MAPPING_TYPE_RELATION).size() - 1)
                      .registerUpdateableMappingAtParent(token.getId());
                }
                // update children
                for (Integer tmpId : object.getUpdateableMappingsAsParent()) {
                  if (tokenCollection.get(tmpId) != null) {
                    tokenCollection.get(tmpId).setParentId(token.getId());
                  }
                }
                object.resetUpdateableMappingsAsParent();
                // use own position
                if (mapping.position.equals(MtasParserMapping.SOURCE_OWN)) {
                  token.addPositions(object.getPositions());
                  // use position from ancestorGroup
                } else if (mapping.position
                    .equals(MtasParserMapping.SOURCE_ANCESTOR_GROUP)
                    && (!currentList.get(MAPPING_TYPE_GROUP).isEmpty())) {
                  currentList.get(MAPPING_TYPE_GROUP)
                      .get(currentList.get(MAPPING_TYPE_GROUP).size() - 1)
                      .addUpdateableMappingWithPosition(token.getId());
                  // use position from ancestorWord
                } else if (mapping.position
                    .equals(MtasParserMapping.SOURCE_ANCESTOR_WORD)
                    && (!currentList.get(MAPPING_TYPE_WORD).isEmpty())) {
                  currentList.get(MAPPING_TYPE_WORD)
                      .get(currentList.get(MAPPING_TYPE_WORD).size() - 1)
                      .addUpdateableMappingWithPosition(token.getId());
                  // use position from ancestorRelation
                } else if (mapping.position
                    .equals(MtasParserMapping.SOURCE_ANCESTOR_RELATION)
                    && (!currentList.get(MAPPING_TYPE_RELATION).isEmpty())) {
                  currentList.get(MAPPING_TYPE_RELATION)
                      .get(currentList.get(MAPPING_TYPE_RELATION).size() - 1)
                      .addUpdateableMappingWithPosition(token.getId());
                  // register id to get positions later from references
                } else if (mapping.position
                    .equals(MtasParserMapping.SOURCE_REFS)) {
                  if (mapping.type.equals(MAPPING_TYPE_GROUP_ANNOTATION)) {
                    if (mapping.start != null && mapping.end != null) {
                      String start = object.getAttribute(mapping.start);
                      String end = object.getAttribute(mapping.end);
                      if (start != null && !start.isEmpty() && end != null
                          && !end.isEmpty()) {
                        if (start.startsWith("#")) {
                          start = start.substring(1);
                        }
                        if (end.startsWith("#")) {
                          end = end.substring(1);
                        }
                        updateList.get(UPDATE_TYPE_LOCAL_REF_POSITION_START)
                            .put(token.getId(),
                                new HashSet<String>(Arrays.asList(start)));
                        updateList.get(UPDATE_TYPE_LOCAL_REF_POSITION_END).put(
                            token.getId(),
                            new HashSet<String>(Arrays.asList(end)));
                        updateList.get(UPDATE_TYPE_LOCAL_REF_OFFSET_START).put(
                            token.getId(),
                            new HashSet<String>(Arrays.asList(start)));
                        updateList.get(UPDATE_TYPE_LOCAL_REF_OFFSET_END).put(
                            token.getId(),
                            new HashSet<String>(Arrays.asList(end)));
                      }
                    }
                  } else {
                    updateList.get(UPDATE_TYPE_POSITION).put(token.getId(),
                        object.getRefIds());
                  }
                } else {
                  // should not happen
                }
                // use own offset
                if (mapping.offset.equals(MtasParserMapping.SOURCE_OWN)) {
                  token.setOffset(object.getOffsetStart(),
                      object.getOffsetEnd());
                  // use offset from ancestorGroup
                } else if (mapping.offset
                    .equals(MtasParserMapping.SOURCE_ANCESTOR_GROUP)
                    && (!currentList.get(MAPPING_TYPE_GROUP).isEmpty())) {
                  currentList.get(MAPPING_TYPE_GROUP)
                      .get(currentList.get(MAPPING_TYPE_GROUP).size() - 1)
                      .addUpdateableMappingWithOffset(token.getId());
                  // use offset from ancestorWord
                } else if (mapping.offset
                    .equals(MtasParserMapping.SOURCE_ANCESTOR_WORD)
                    && !currentList.get(MAPPING_TYPE_WORD).isEmpty()) {
                  currentList.get(MAPPING_TYPE_WORD)
                      .get(currentList.get(MAPPING_TYPE_WORD).size() - 1)
                      .addUpdateableMappingWithOffset(token.getId());
                  // use offset from ancestorRelation
                } else if (mapping.offset
                    .equals(MtasParserMapping.SOURCE_ANCESTOR_RELATION)
                    && !currentList.get(MAPPING_TYPE_RELATION).isEmpty()) {
                  currentList.get(MAPPING_TYPE_RELATION)
                      .get(currentList.get(MAPPING_TYPE_RELATION).size() - 1)
                      .addUpdateableMappingWithOffset(token.getId());
                  // register id to get offset later from refs
                } else if (mapping.offset
                    .equals(MtasParserMapping.SOURCE_REFS)) {
                  updateList.get(UPDATE_TYPE_OFFSET).put(token.getId(),
                      object.getRefIds());
                }
                // always use own realOffset
                token.setRealOffset(object.getRealOffsetStart(),
                    object.getRealOffsetEnd());
                // set payload
                token.setPayload(payload);
                // add token to collection
                tokenCollection.add(token);
              }
            }
          }
        }
        }
        // register start and end
        if (mapping.start != null && mapping.end != null) {
          String startAttribute = null;
          String endAttribute = null;
          if (mapping.start.equals("#")) {
            startAttribute = object.getId();
          } else {
            startAttribute = object.getAttribute(mapping.start);
            if (startAttribute != null && startAttribute.startsWith("#")) {
              startAttribute = startAttribute.substring(1);
            }
          }
          if (mapping.end.equals("#")) {
            endAttribute = object.getId();
          } else {
            endAttribute = object.getAttribute(mapping.end);
            if (endAttribute != null && endAttribute.startsWith("#")) {
              endAttribute = endAttribute.substring(1);
            }
          }
          if (startAttribute != null && endAttribute != null
              && !object.getPositions().isEmpty()) {
            object.setReferredStartPosition(startAttribute,
                object.getPositions().first());
            object.setReferredEndPosition(endAttribute,
                object.getPositions().last());
            object.setReferredStartOffset(startAttribute,
                object.getOffsetStart());
            object.setReferredEndOffset(endAttribute, object.getOffsetEnd());
          }
        }
      } catch (MtasParserException e) {
        log.debug("Rejected mapping " + object.getType().getName(), e);
        // ignore, no new token is created
      }
    }
    // copy remaining updateableMappings to new parent
    if (!currentList.get(objectType.getType()).isEmpty()) {
      if (currentList.get(objectType.getType()).contains(object)) {
        int listPosition = currentList.get(objectType.getType())
            .indexOf(object);
        if (listPosition > 0) {
          currentList.get(objectType.getType()).get(listPosition - 1)
              .registerUpdateableMappingsAtParent(
                  object.getUpdateableMappingsAsParent());
        }
      } else {
        currentList.get(objectType.getType())
            .get(currentList.get(objectType.getType()).size() - 1)
            .registerUpdateableMappingsAtParent(
                object.getUpdateableMappingsAsParent());
      }
    } else if (!currentList.get(MAPPING_TYPE_GROUP).isEmpty()) {
      currentList.get(MAPPING_TYPE_GROUP)
          .get(currentList.get(MAPPING_TYPE_GROUP).size() - 1)
          .registerUpdateableMappingsAtParent(
              object.getUpdateableMappingsAsParent());
    } else if (!currentList.get(MAPPING_TYPE_RELATION).isEmpty()) {
      currentList.get(MAPPING_TYPE_RELATION)
          .get(currentList.get(MAPPING_TYPE_RELATION).size() - 1)
          .registerUpdateableMappingsAtParent(
              object.getUpdateableMappingsAsParent());
    }
    updateMappingsWithLocalReferences(object, currentList, updateList);
  }

    @Override
    protected void updateMappingsWithLocalReferences(MtasParserObject currentObject,
      Map<String, List<MtasParserObject>> currentList,
      Map<String, Map<Integer, Set<String>>> updateList) {
    if (currentObject.getType().getType().equals(MAPPING_TYPE_GROUP)) { //EF: type -> getType()
      for (Integer tokenId : updateList
          .get(UPDATE_TYPE_LOCAL_REF_POSITION_START).keySet()) {
        if (updateList.get(UPDATE_TYPE_LOCAL_REF_POSITION_END)
            .containsKey(tokenId)
            && updateList.get(UPDATE_TYPE_LOCAL_REF_OFFSET_START)
                .containsKey(tokenId)
            && updateList.get(UPDATE_TYPE_LOCAL_REF_OFFSET_END)
                .containsKey(tokenId)) {
          Iterator<String> startPositionIt = updateList
              .get(UPDATE_TYPE_LOCAL_REF_POSITION_START).get(tokenId)
              .iterator();
          Iterator<String> endPositionIt = updateList
              .get(UPDATE_TYPE_LOCAL_REF_POSITION_END).get(tokenId).iterator();
          Iterator<String> startOffsetIt = updateList
              .get(UPDATE_TYPE_LOCAL_REF_OFFSET_START).get(tokenId).iterator();
          Iterator<String> endOffsetIt = updateList
              .get(UPDATE_TYPE_LOCAL_REF_OFFSET_END).get(tokenId).iterator();
          Integer startPosition = null;
          Integer endPosition = null;
          Integer startOffset = null;
          Integer endOffset = null;
          Integer newValue = null;
          while (startPositionIt.hasNext()) {
            String localKey = startPositionIt.next();
            
            // EF
            if (currentObject.anchorStartPosition.containsKey(localKey)){ //only for time references
            //if (!currentObject.referredStartPosition.containsKey(localKey)){ //only for time references
                localKey = currentObject.anchorStartPosition.get(localKey); // use anchorStartPositions to replace localKey (the time reference by token reference)  
            }
            if (currentObject.referredStartPosition.containsKey(localKey)) {
              newValue = currentObject.referredStartPosition.get(localKey);
              startPosition = (startPosition == null) ? newValue
                  : Math.min(startPosition, newValue);
            }
          }
          while (endPositionIt.hasNext()) {
            String localKey = endPositionIt.next();
            
            // EF
            if (currentObject.anchorEndPosition.containsKey(localKey)){ //only for time references
            //if (!currentObject.referredEndPosition.containsKey(localKey)){ //only for time references
                localKey = currentObject.anchorEndPosition.get(localKey); // use anchorEndPositions to replace the time reference by token reference  
            }
            if (currentObject.referredEndPosition.containsKey(localKey)) {
              newValue = currentObject.referredEndPosition.get(localKey);
              endPosition = (endPosition == null) ? newValue
                  : Math.max(endPosition, newValue);
            }
          }
          while (startOffsetIt.hasNext()) {
            String localKey = startOffsetIt.next();
            
            // EF
            if (currentObject.anchorStartPosition.containsKey(localKey)){ //only for time references
            //if (!currentObject.referredStartOffset.containsKey(localKey)){ //only for time references
               localKey = currentObject.anchorStartPosition.get(localKey); // use anchorEndPositions to replace the time reference by token reference  
            }
                        
            if (currentObject.referredStartOffset.containsKey(localKey)) {
              newValue = currentObject.referredStartOffset.get(localKey);
              startOffset = (startOffset == null) ? newValue
                  : Math.min(startOffset, newValue);
            }
          }
          while (endOffsetIt.hasNext()) {
            String localKey = endOffsetIt.next();
            
            // EF
            if (currentObject.anchorEndPosition.containsKey(localKey)){ //only for time references
            //if (!currentObject.referredEndOffset.containsKey(localKey)){ //only for time references
                localKey = currentObject.anchorEndPosition.get(localKey); // use anchorEndPositions to replace the time reference by token reference  
             }
                        
            if (currentObject.referredEndOffset.containsKey(localKey)) {
              newValue = currentObject.referredEndOffset.get(localKey);
              endOffset = (endOffset == null) ? newValue
                  : Math.max(endOffset, newValue);
            }
          }
          if (startPosition != null && endPosition != null
              && startOffset != null && endOffset != null) {
            MtasToken token = tokenCollection.get(tokenId);
            token.addPositionRange(startPosition, endPosition);
            token.addOffset(startOffset, endOffset);
            
          }else{
          }
        }
      }

    }
    if (!currentList.get(MAPPING_TYPE_GROUP).isEmpty()) {
      MtasParserObject parentGroup = currentList.get(MAPPING_TYPE_GROUP)
          .get(currentList.get(MAPPING_TYPE_GROUP).size() - 1);

      if (currentList.get(MAPPING_TYPE_GROUP).size()==1){ //EF: this is for body element: don't change start positions
                      
        for (Map.Entry<String, Integer> entry : currentObject.referredStartPosition.entrySet()) {
            if (!parentGroup.referredStartPosition.containsKey(entry.getKey())){
                parentGroup.referredStartPosition.put(entry.getKey(), entry.getValue());
            }
        }
        
        for (Map.Entry<String, Integer> entry : currentObject.referredStartOffset.entrySet()) {
            if (!parentGroup.referredStartOffset.containsKey(entry.getKey())){
                parentGroup.referredStartOffset.put(entry.getKey(), entry.getValue());
            }
        }
      }else{
        parentGroup.referredStartPosition.putAll(currentObject.referredStartPosition);
        parentGroup.referredStartOffset.putAll(currentObject.referredStartOffset);
      }
      
      parentGroup.referredEndPosition.putAll(currentObject.referredEndPosition);
      //parentGroup.referredStartOffset.putAll(currentObject.referredStartOffset);
      parentGroup.referredEndOffset.putAll(currentObject.referredEndOffset);
            
      if (currentObject.getType().getType().equals(MAPPING_TYPE_GROUP)) {
          
          if (currentList.get(MAPPING_TYPE_GROUP).size()==1){ //EF: this is for body element: don't change start positions
            for (Map.Entry<String, String> entry : currentObject.anchorStartPosition.entrySet()) {
                if (!parentGroup.anchorStartPosition.containsKey(entry.getKey())){
                    parentGroup.anchorStartPosition.put(entry.getKey(), entry.getValue());
    }
            }
          }else{
            parentGroup.anchorStartPosition.putAll(currentObject.anchorStartPosition);
          }
          parentGroup.anchorEndPosition.putAll(currentObject.anchorEndPosition);

      }
      


    }
    
    currentObject.referredStartPosition.clear();
    currentObject.referredEndPosition.clear();
    currentObject.referredStartOffset.clear();
    currentObject.referredEndOffset.clear();
    currentObject.anchorEndPosition.clear();
    currentObject.anchorStartPosition.clear();
    
  }
    
    @Override
    protected String computeFilteredPrefixedValue(String type, String value,
      String filter, String prefix) throws MtasConfigException {
    String localValue = value;
    // do magic with filter
    if (filter != null) {
      String[] filters = filter.split(",");
      for (String item : filters) {
        if (item.trim().equals(MAPPING_FILTER_HTML)) {
            localValue = IOHelper.IPA2HTML(localValue);
        } else if (item.trim().equals(MAPPING_FILTER_CEIL)) {
            if (localValue!=null){
                Pattern pattern = Pattern.compile("\\d+\\.?\\d*"); // only for doubles
                if (pattern.matcher(localValue).matches()){         
                    double durDouble = Double.valueOf(localValue);              
                    int durInt = (int)Math.ceil(durDouble);
                    localValue = String.valueOf(durInt);     
                }
            }
        } else if (item.trim().equals(MAPPING_FILTER_UPPERCASE)) {
          localValue = localValue == null ? null : localValue.toUpperCase();
        } else if (item.trim().equals(MAPPING_FILTER_LOWERCASE)) {
          localValue = localValue == null ? null : localValue.toLowerCase();
        } else if (item.trim().equals(MAPPING_FILTER_ASCII)) {
          if (localValue != null) {
            char[] old = localValue.toCharArray();
            char[] ascii = new char[4 * old.length];
            ASCIIFoldingFilter.foldToASCII(old, 0, ascii, 0,
                localValue.length());
            localValue = new String(ascii);
          }
        } else if (item.trim()
            .matches(Pattern.quote(MAPPING_FILTER_SPLIT) + "\\([0-9\\-]+\\)")) {
          if (!type.equals(MtasParserMapping.PARSER_TYPE_TEXT_SPLIT)) {
            throw new MtasConfigException(
                "split filter not allowed for " + type);
          }
        } else {
          throw new MtasConfigException(
              "unknown filter " + item + " for value " + localValue);
        }
      }
    }
    if (localValue != null && prefix != null) {
      localValue = prefix + localValue;
    }
    return localValue;
  }
    
    //EF: method renamed: computeVariablesFromObject -> computeVariablesFromObj, because of Map -> LinkedHashMap
    protected void computeVariablesFromObj(MtasParserObject object,
        Map<String, List<MtasParserObject>> currentList,
        Map<String, LinkedHashMap<String, String>> variables) {
      
        MtasParserType<MtasParserVariable> parserType = object.getType();
        String id = object.getId();
        if (id != null) {
            for (MtasParserVariable variable : parserType.getItems()) {
                if (!variables.containsKey(variable.variable)) {
                    variables.put(variable.variable, new LinkedHashMap<String, String>());
                }
            StringBuilder builder = new StringBuilder();
            for (MtasParserVariableValue variableValue : variable.values) {
                if (variableValue.type.equals(ITEM_TYPE_ATTRIBUTE)) {
                    String subValue = object.getAttribute(variableValue.name);
                    if (subValue != null) {
                        builder.append(subValue);
                    }
                }
            }
            variables.get(variable.variable).put(id, builder.toString());
            }
        }
    }
    
    //EF: method renamed: decodeAndUpdateWithVariables -> decodeAndUpdateWithVar, because of Map -> LinkedHashMap
    protected String decodeAndUpdateWithVar(String encodedPrefix,
      String encodedPostfix, Map<String, LinkedHashMap<String, String>> variables) {
    String[] prefixSplit;
    String[] postfixSplit;
    if (encodedPrefix != null && !encodedPrefix.isEmpty()) {
      prefixSplit = encodedPrefix.split(" ");
    } else {
      prefixSplit = new String[0];
    }
    if (encodedPostfix != null && !encodedPostfix.isEmpty()) {
      postfixSplit = encodedPostfix.split(" ");
    } else {
      postfixSplit = new String[0];
    }
    try {
      String prefix = decodeAndUpdateWithVar(prefixSplit, variables);
      String postfix = decodeAndUpdateWithVar(postfixSplit, variables);
      return prefix + MtasToken.DELIMITER + postfix;
    } catch (MtasParserException e) {
      log.debug(e);
      return null;
    }
  }

    //EF: method renamed: decodeAndUpdateWithVariables -> decodeAndUpdateWithVar, because of Map -> LinkedHashMap
    private String decodeAndUpdateWithVar(String[] splitList,
      Map<String, LinkedHashMap<String, String>> variables) throws MtasParserException {
    StringBuilder builder = new StringBuilder();
    for (String split : splitList) {
      if (split.contains(":")) {
        String[] subSplit = split.split(":");
        if (subSplit.length == 2) {
          String decodedVariableName = new String(dec.decode(subSplit[0]),
              StandardCharsets.UTF_8);
          String decodedVariableValue = new String(dec.decode(subSplit[1]),
              StandardCharsets.UTF_8);
          if (variables.containsKey(decodedVariableName)) {
            if (variables.get(decodedVariableName)
                .containsKey(decodedVariableValue)) {
              String valueFromVariable = variables.get(decodedVariableName)
                  .get(decodedVariableValue);
              builder.append(valueFromVariable);
            } else {
              throw new MtasParserException("id " + decodedVariableValue
                  + " not found in " + decodedVariableName);
            }
          } else {
            throw new MtasParserException(
                "variable " + decodedVariableName + " unknown");
          }
        }
      } else {
        try {
          builder.append(new String(dec.decode(split), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
          log.info(e);
        }
      }
    }
    return builder.toString();
  }
  
    public void printCurrentObject(MtasParserObject currentObject){
        
        logger.setLevel(Level.WARNING);
        logger.info("***");
        logger.info("currentObject id: "+ currentObject.getId());
        logger.info("currentObject referredStartPosition: "+ currentObject.referredStartPosition);
        logger.info("currentObject referredEndPosition: "+ currentObject.referredEndPosition);
        logger.info("currentObject referredStartOffset: "+ currentObject.referredStartOffset);
        logger.info("currentObject referredEndOffset: "+ currentObject.referredEndOffset);
        logger.info("currentObject anchorEndPosition: "+ currentObject.anchorEndPosition);
        logger.info("currentObject anchorStartPosition: "+ currentObject.anchorStartPosition);
        logger.info("currentObject updateableMappingsAsParent: "+ currentObject.getUpdateableMappingsAsParent());
        logger.info("currentObject updateableIdsWithOffset: "+ currentObject.getUpdateableIdsWithOffset());
        logger.info("currentObject updateableIdsWithPosition: "+ currentObject.getUpdateableIdsWithPosition());
        logger.info("currentObject updateableMappingsWithOffset: "+ currentObject.getUpdateableMappingsWithOffset());
        logger.info("currentObject updateableMappingsWithPosition: "+ currentObject.getUpdateableMappingsWithPosition());
        logger.info("currentObject positions: "+ currentObject.getPositions());
        logger.info("currentObject text: "+ currentObject.getText());
        logger.info("currentObject offsetStart: "+ currentObject.getOffsetStart());
        logger.info("currentObject offsetEnd: "+ currentObject.getOffsetEnd());
        logger.info("currentObject realOffsetStart: "+ currentObject.getRealOffsetStart());
        logger.info("currentObject realOffsetEnd: "+ currentObject.getRealOffsetEnd());
        logger.info("currentObject type (name): "+ currentObject.getType().getName());
        logger.info("currentObject type (type): "+ currentObject.getType().getType());
        logger.info("currentObject refId: "+ currentObject.getRefIds());
        logger.info("***");             
    }
    
    ArrayList<String> getTimelineVarsBetween(Map<String, LinkedHashMap<String,String>> variables, String start, String end){
        ArrayList<String> arrayList = new ArrayList<String>();
        LinkedHashMap<String,String> map = variables.get("interval");
        boolean x = false;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            if (key.equals(end)){
                x = false;
            }
            if (x){
                arrayList.add(key);
            }
            if (key.equals(start)){
                x = true;
            }
        }
      
        return arrayList;
    }

}
