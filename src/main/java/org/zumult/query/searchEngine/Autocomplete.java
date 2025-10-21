/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.zumult.query.searchEngine;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mtas.codec.util.CodecInfo;
import mtas.codec.util.CodecSearchTree;
import mtas.codec.util.CodecUtil;
import mtas.search.spans.util.MtasSpanQuery;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.spans.SpanWeight;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.zumult.query.SearchServiceException;


/**
 * The {@code Autocomplete} class depends on the MTAS/Lucene extension
 * and provides functionality to perform token based search
 * for autocompletion purposes.
 *
 * Example:
 * [lemma="h.*"] will return a list of strings
 * containing all lemmas starting with 'h'
 *
 * @author Frick
 */
public class Autocomplete extends QueryCreater {

  /**
   * Retrieves a sorted list of autocompletion suggestions
   * for a given query string from the provided Lucene/Mtas index paths.
   *
   * @param indexPaths              A list of index directories to search in.
   * @param queryString             A query string
   *                                of the form {@code [key="value"]}.
   * @param from                    Not used (reserved for range filtering).
   * @param to                      Not used (reserved for range filtering).
   * @return                        A sorted list of unique strings
   *                                matching the query.
   * @throws SearchServiceException if the query string is malformed or
   *                                does not match the expected pattern.
   * @throws IOException            if index access fails.
     */
  public List<String> getSuggestions(final ArrayList<String> indexPaths,
                                     final String queryString,
                                     final Integer from, final Integer to)
                                throws SearchServiceException, IOException {

    // create prefix list
    List<String> prefixList =  getPrefixList(queryString);


    // create search query
    MtasSpanQuery mtasQuery
         = createQuery(SearchIndexFieldEnum.TRANSCRIPT_CONTENT.toString(),
                            queryString,
                            null,
                            null,
                            null);

    // collect unique suggestions from all indexes
    Set<String> set = new HashSet<>();
    for (String indexPath : indexPaths) {
      set.addAll(search(indexPath, mtasQuery, prefixList));
    }

    // sort the results alphabetically
    ArrayList arrayList = new ArrayList(set);
    Collections.sort(arrayList, String.CASE_INSENSITIVE_ORDER);

    //System.out.println(arrayList.toString());
    return arrayList;
  }

  /**
  * Extracts the annotation key from a single token query and
  * creates a prefix list needed for the MTAS method
  * 'getPositionedTermsByPrefixesAndPositionRange'.
  *
  * @param queryString                A query string of the form
  *                                   {@code [key="value"]}.
  * @return                           A singletonList with the key extracted
  *                                   from the query string.
  * @throws SearchServiceException    If query does not match a token query.
  */
  private List<String> getPrefixList(final String queryString)
                                    throws SearchServiceException {

    Pattern pattern = Pattern.compile(
            "^\\[([a-zA-Z0-9_]+)=\"((?:[^\"\\\\]|\\\\.)*)\"\\]$");

    Matcher matcher = pattern.matcher(queryString);
    if (!matcher.matches()) {
      throw new SearchServiceException("Please check your query string! "
            + "It should be a key-value pair enclosed in square brackets,"
            + " e.g. [lemma=\"gehen\"]");
    }

    String key = matcher.group(1);
    String caseSensitiveEnding = "_lc";

    if (key.endsWith(caseSensitiveEnding)) {
     key = key.substring(0,
                         key.length() - caseSensitiveEnding.length());
    }

    return Collections.singletonList(key);
  }

 /**
  * Executes the given {@link MtasSpanQuery} on a single index path and
  * collects matching metadata values.
  *
  * @param indexPath            The file system path to the index directory.
  * @param query                The compiled MtasSpanQuery object.
  * @param prefixList   The list of metadata prefixes to extract.
  * @return                     A set of extracted term values.
  * @throws IOException         If index reading or query execution fails.
  */
  private Set<String> search(final String indexPath,
                             final MtasSpanQuery query,
                             final List<String> prefixList)
                               throws IOException {

    IndexReader reader = null;
    Set<String> result = new HashSet();

    Directory directory = FSDirectory.open(Paths.get(indexPath));

    if (directory == null) {
      return null;
    }

    try {
      reader = DirectoryReader.open(directory);
      ListIterator<LeafReaderContext> iterator
                            = reader.leaves().listIterator();
      IndexSearcher searcher = new IndexSearcher(reader);
      SpanWeight spanweight = ((MtasSpanQuery) query.rewrite(reader))
                                    .createWeight(searcher,
                                                  ScoreMode.COMPLETE,
                                                   0);

      while (iterator.hasNext()) {
        LeafReaderContext lrc = iterator.next();
        Spans spans = spanweight.getSpans(lrc,
                                    SpanWeight.Postings.POSITIONS);
        SegmentReader segmentReader = (SegmentReader) lrc.reader();
        Terms t = segmentReader.terms(
                     SearchIndexFieldEnum.TRANSCRIPT_CONTENT.toString());
        CodecInfo mtasCodecInfo = CodecInfo.getCodecInfoFromTerms(t);

        if (spans == null) {
          continue;
        }

        // iterate over all spans in a segment and extracts values
        while (spans.nextDoc() != Spans.NO_MORE_DOCS) {
          if (segmentReader.numDocs() == segmentReader.maxDoc()
                || segmentReader.getLiveDocs().get(spans.docID())) {

            while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS) {
              List<CodecSearchTree.MtasTreeHit<String>> terms = mtasCodecInfo
                        .getPositionedTermsByPrefixesAndPositionRange(
                       SearchIndexFieldEnum.TRANSCRIPT_CONTENT.toString(),
                            spans.docID(),
                            prefixList,
                            spans.startPosition(),
                            (spans.endPosition() - 1));

              String hit = CodecUtil.termValue(terms.get(0).data);
              result.add(hit);
            }
          }
        }
      }

      reader.close();

    } catch (IndexNotFoundException ex) {
      throw new IOException("Search index could not be found under "
                                   + indexPath, ex);
    } finally {
      if (reader != null) {
        reader.close();
      }
    }

    return result;
  }
}
