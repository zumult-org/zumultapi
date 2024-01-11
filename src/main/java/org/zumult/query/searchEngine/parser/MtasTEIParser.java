/*******************************************************************************
 * This Java class was written by Matthijs Brouwer for MTAS - an open source Lucene-based
 * search engine for querying on text with multilevel annotations. 
 * 
 * https://textexploration.github.io/mtas/index.html
 * 
 * ******************************************************************************/
package org.zumult.query.searchEngine.parser;

import javax.xml.XMLConstants;
import mtas.analysis.util.MtasConfigException;
import mtas.analysis.util.MtasConfiguration;
import org.zumult.io.Constants;

/**
 * The Class MtasTEIParser.
 */
public class MtasTEIParser extends MtasXMLParser {

  /**
   * Instantiates a new mtas TEI parser.
   *
   * @param config the config
   */
  public MtasTEIParser(MtasConfiguration config) {
    super(config);
  }

  /*
   * (non-Javadoc)
   * 
   * @see mtas.analysis.parser.MtasXMLParser#initParser()
   */
  @Override
  protected void initParser() throws MtasConfigException {
    namespaceURI = Constants.TEI_NAMESPACE_URL;
    namespaceURI_id = XMLConstants.XML_NS_URI;
    rootTag = "TEI";
    contentTag = "text";
    allowNonContent = true;
    super.initParser();
  }

}

