// Getters derive and peform calculation on the data in state.js
// This is where most of the data filtering and refining is done.

// The absolute extents of each criterion is calculated here. They
// determine the extreme bounds of the slider used for filtering.

// Each criterion has its own filter. The filter here is always a
// list of speech events that comply to the user-selected criteria.
// For example, normalisierungFilter is a list of speech events, whose
// normalisierungsrate falls within the user-selected min and max (slider range)

import { parseHHMMSS, minutesToTimeString, timeStringToMin, getExtent } from '@/helper';
import _ from 'lodash'
import * as d3 from 'd3'

export default {
  // returns the raw {CORPUS} data of the selected corpus, as obtained from the server
  rawData: state => {
    return state.corpusData[state.selectedCorpus].data;
  },
  // returns gesprachstypTreeselect{CORPUS} data, as obtained from the server
  gesprachstypData: state => {
    return state.corpusData[state.selectedCorpus].gesprachstypData;
  },
  // returns themen{CORPUS} data, as obtained from the server
  themenData: state => {
    return state.corpusData[state.selectedCorpus].themenData;
  },
  // returns art{CORPUS} data, as obtained from the server
  artData: state => {
    return state.corpusData[state.selectedCorpus].artData;
  },
  // returns sprachregionTreeselect{CORPUS} data, as obtained from the server
  sprachregionData: state => {
    return state.corpusData[state.selectedCorpus].sprachregionData;
  },
  // returns lang{CORPUS} data, as obtained from the server
  sprachenData: state => {
    return state.corpusData[state.selectedCorpus].sprachen;
  },
  // returns list of corpora (FOLK, GWSS)
  corporaOptions: state => {
    return Object.keys(state.corpusData);
  },
  // list of all wortarten
  getWortarten: (state, getters) => {
    return getters.rawData.length == 0 ? [] : Object.keys(getters.rawData[0].maße.wortarten);
  },
  // list of all mündlichkeitsphänomene
  getMündlichkeitsphänomene: (state, getters) => {
    return getters.rawData.length == 0 ? [] : Object.keys(getters.rawData[0].maße.mündlichkeitsphänomene);
  },







  // returns the absolute extent (min and max) for speech event durations as a set of strings, ex.: ["00:00","05:38"]
  getDauerExtent: (state, getters) => {
    const dauerInMin = getters.rawData.map(se => parseHHMMSS(se.maße.dauer).getMinutes() + parseHHMMSS(se.maße.dauer).getHours() * 60 );
     // if the data has not loaded, set extent to ["00:00","06:00"]
    const min = getters.rawData.length == 0 ? "00:00" : Math.floor(Math.min(...dauerInMin));
    const max = getters.rawData.length == 0 ? "06:00" : Math.ceil(Math.max(...dauerInMin) + 1);

    const minTime = minutesToTimeString(min);
    const maxTime = minutesToTimeString(max);

    return [minTime, maxTime];
  },
  // returns the absolute extent (min and max) for the token coverage of the selected Wortschatzliste as a set integers, ex.: [55,97]
  getFrequenzlisteExtent: (state, getters) => {
    let extent = [0, 100];
    if (state.frequenzlistenOptions.includes(state.selectedFrequenzliste)) {
      const tokensRatioList = getters.rawData.map(se => { 
        return isNaN(se.maße.wortschatz[state.selectedFrequenzliste].tokens_ratio)
        ? 0
        : +se.maße.wortschatz[state.selectedFrequenzliste].tokens_ratio.replace(",", ".") * 100;
      });
      extent = getExtent(getters, tokensRatioList);
    }
    return extent;
  },
  // returns the absolute extent (min and max) for the proportion of normalised tokens in the corpus as a set integers, ex.: [2,57]
  getNormalisierungExtent: (state, getters) => {
    const normalisierungsrateList = getters.rawData.map(se => {
      console.log()
      return isNaN(se.maße.normalisierungsrate.replace(",", "."))
      ? 0
      : +se.maße.normalisierungsrate.replace(",", ".");
    });
    return getExtent(getters, normalisierungsrateList);
  },
  // returns the absolute extent (min and max) for the number of articulated sillables per second in the corpus as a set integers, ex.: [2,7]
  getArtikulationsrateExtent: (state, getters) => {
    const artikulationsrateList = getters.rawData.map(se => +se.maße.artikulationsrate.replace(",", "."));
    return getExtent(getters, artikulationsrateList);
  },
  // returns the absolute extent (min and max) for the proportion of normalised tokens in the corpus as a set integers, ex.: [2,57]
  getUeberlappungenExtent: (state, getters) => {
    const ueberlappungenList = getters.rawData.map(se => +se.maße.anteil_Überlappungen_mit_mehr_als_2_wörtern_tokens.replace(",", "."));
    return getExtent(getters, ueberlappungenList);
  },
  // returns the absolute extents (min and max) for the proportion of token of each wortart in the corpus, ex.: {"NN":[5,19],"NE":[0,6],"V":[1,16],"ADJ":[1,12],"ADV":[0,19],"PTKVZ":[0,3]}
  getWortartenExtents: (state, getters) => {
    let wortartenExtents = {};
    getters.getWortarten.forEach(wortart => {
      const wortartRateList = getters.rawData.map(se => {
        return isNaN(se.maße.wortarten[wortart].token_ratio)
        ? 0
        : +se.maße.wortarten[wortart].token_ratio.replace(",", ".")
      });
      wortartenExtents[wortart] = getExtent(getters, wortartRateList);
    });
    return wortartenExtents;
  },
  // returns the absolute extents (min and max) for the proportion of token of each mündlichkeitsphänomen in the corpus, ex.: {"NGHES":[0,11],"NGIRR":[0,28],"PTKMA":[0,5],"SEDM":[0,5],"SEQU":[0,4],"CLITIC":[0,7]}
  getMündlichkeitsphänomeneExtents: (state, getters) => {
    let mündlichkeitsphänomeneExtents = {};
    getters.getMündlichkeitsphänomene.forEach(mündlichkeitsphänomen => {
      const mündlichkeitsphänomenRateList = getters.rawData.map(se => {
        return isNaN(se.maße.mündlichkeitsphänomene[mündlichkeitsphänomen].token_ratio)
        ? 0
        : +se.maße.mündlichkeitsphänomene[mündlichkeitsphänomen].token_ratio.replace(",", ".")});
      mündlichkeitsphänomeneExtents[mündlichkeitsphänomen] = getExtent(getters, mündlichkeitsphänomenRateList);
    });
    return mündlichkeitsphänomeneExtents;
  },







  // for each wortart, return the list of speech events, filtered by the user-selected range (slider)
  wortartenFilters: (state, getters) => {
    let filters = {};
    getters.getWortarten.forEach(wortart => {
      filters[wortart + "Filter"] = getters.rawData.filter(se => {
        const ratio = isNaN(se.maße.wortarten[wortart].token_ratio)
          ? 0 // if the value is not valid (= "nicht verfügbar"), set value to 0
          : +se.maße.wortarten[wortart].token_ratio.replace(",", "."); // otherwise read value and extract a numeric value from it

        const selectedRangeStart = state.wortartenRanges[wortart][0];
        const selectedRangeEnd = state.wortartenRanges[wortart][1];
        return ratio >= selectedRangeStart && ratio <= selectedRangeEnd;
        // return _.inRange(ratio, selectedRangeStart, selectedRangeEnd);
      }).map( se => se );
    });
    return filters;
  },
  // for each mündlichkeitsphänomen, return the list of speech events, filtered by the user-selected range (slider)
  mündlichkeitsphänomenFilters: (state, getters) => {
    let filters = {};
    getters.getMündlichkeitsphänomene.forEach(mündlichkeitsphänomen => {
      filters[mündlichkeitsphänomen + "Filter"] = getters.rawData.filter(se => {
        const ratio = isNaN(se.maße.mündlichkeitsphänomene[mündlichkeitsphänomen].token_ratio)
          ? 0 // if the value is not valid (= "nicht verfügbar"), set value to 0
          : +se.maße.mündlichkeitsphänomene[mündlichkeitsphänomen].token_ratio.replace(",", "."); // otherwise read value and extract a numeric value from it
        const selectedRangeStart = state.mündlichkeitsphänomeneRanges[mündlichkeitsphänomen][0];
        const selectedRangeEnd = state.mündlichkeitsphänomeneRanges[mündlichkeitsphänomen][1];
        return ratio >= selectedRangeStart && ratio <= selectedRangeEnd;
        // return _.inRange(ratio, selectedRangeStart, selectedRangeEnd);
      }).map( se => se );
    });
    return filters;
  },
  // ==> Used with the previous Dauer component (bars)
  // getDauerBrushSEs: ({brushRange}, {rawData}) => { // state, getters
  //   const newdauerBrushRangeSeList = rawData.filter(se => {
  //     const duration = parseHHMMSS(se.maße.dauer);
  //     return duration >= brushRange.min && duration <= brushRange.max ?
  //       se :
  //       null;
  //   });
  //   return newdauerBrushRangeSeList;
  // },
  // return the list of speech events, filtered by the user-selected range (slider)
  dauerFilter: (state, getters) => {
    // ==> with previous Dauer component (bars)
    // return getters.getDauerBrushSEs.filter(se => {
    //   return getters.rawData.includes(se) ? se : null;
    // }).map(se => se);

    return getters.rawData.filter(se => {
      const dauerInMin = parseHHMMSS(se.maße.dauer).getMinutes() + parseHHMMSS(se.maße.dauer).getHours() * 60;
      const dauerRangeStart = timeStringToMin(state.dauerRange[0]);
      const dauerRangeEnd = timeStringToMin(state.dauerRange[1]);
      return dauerInMin >= dauerRangeStart && dauerInMin <= dauerRangeEnd;
      // return _.inRange(ratio, selectedRangeStart, selectedRangeEnd);
    }).map( se => se );
  },
  // return the list of speech events, filtered by the user selection of gesprächstyp criteria (treeselect input area). Only applies to FOLK
  gespraechstypFilter: (state, getters) => {
    let filteredGespraechstypSEList = new Set(); // so as not to have duplicates

    if (state.selectedGespraechstyp.length == 0) {
      // if nothing is selected in the input area, return the list with all speech events
      filteredGespraechstypSEList = getters.rawData;
    } else {
      state.selectedGespraechstyp.forEach( item => {
        const split = item.split("__");
        // filter data by selected interaktionsdomäne, ex.: "Öffentlich"
        let filteredSEs = getters.rawData.filter( se => { // 1st level
          return se.interaktionsdomäne == split[0];
        }).map( se => se);
  
        // filter further by selected lebensbereich, ex.: "Politik"
        if (split.length > 1) { // 2nd level
          filteredSEs = filteredSEs.filter( se => {
            return se.lebensbereich.includes(split[1]);
          }).map( se => se);
          
          // filter further by selected aktivität, ex.: "Schlichtung"
          if (split.length > 2) { // 3rd level
            filteredSEs = filteredSEs.filter( se => {
              return se.aktivität == split[2];
            }).map( se => se);  
          }
        }
        filteredGespraechstypSEList = [...filteredGespraechstypSEList, ...filteredSEs];
      });
    }
    return filteredGespraechstypSEList;
  },
  // return the list of speech events, filtered by the user selection of Sprachregion criteria (treeselect input area) 
  sprachregionFilter: (state, getters) => {
    let filteredsSprachregionSEList = new Set(); // so as not to have duplicates

    if (state.selectedSprachregion.length == 0) {
      // if nothing is selected in the input area, return the list with all speech events
      filteredsSprachregionSEList = getters.rawData;
    } else {
      state.selectedSprachregion.forEach(item => {
        const split = item.split("__");
        // filter data by selected land, ex.: "Deutschland"
        let filteredSEs = getters.rawData.filter( se => { // 1st level
          return se.geo.land == split[0];
        }).map( se => se);
        
        // filter further by selected dialektalregion_lameli, ex.: "südwest"
        if (split.length > 1) { // 2nd level
          filteredSEs = filteredSEs.filter( se => {
            return se.geo.dialektalregion_lameli == split[1];
          }).map( se => se);
  
          // filter further by selected dialektalregion_wiesinger, ex.: "Schwäbische Sprachregion"
          if (split.length > 2) { // 3rd level
            filteredSEs = filteredSEs.filter( se => {
              return se.geo.dialektalregion_wiesinger.includes(split[2]);
            }).map( se => se);  
          }
        }
        filteredsSprachregionSEList = [...filteredsSprachregionSEList, ...filteredSEs];
      });
    }
    return filteredsSprachregionSEList;
  },
  // return the list of speech events, filtered by the user selection of Art criteria (treeselect input area) 
  artFilter: (state, getters) => {
    let filteredArtSEList = new Set();// so as not to have duplicates

    if (state.selectedArt.length == 0) {
      // if nothing is selected in the input area, return the list with all speech events
      filteredArtSEList = getters.rawData;
    } else {
      state.selectedArt.forEach( item => {
        // filter data by selected art, ex.: "Schlichtungsgespräch"
        let filteredSEs = getters.rawData.filter( se => {
          return se.art == item;
        }).map( se => se);
  
        filteredArtSEList = [...filteredArtSEList, ...filteredSEs];
      });
    }
    return filteredArtSEList;
  },
  // return the list of speech events, filtered by the user selection of Sprachen criteria (treeselect input area). Only applies to GWSS
  sprachenFilter: (state, getters) => {
    let filteredSprachenSEList = new Set();

    if (state.selectedSprachen.length == 0) {
      // if nothing is selected in the input area, return the list with all speech events
      filteredSprachenSEList = getters.rawData;
    } else {
      state.selectedSprachen.forEach( item => {
        // filter data by selected sprachen, ex.: "Deutsch (L2)" or "Deutsch (L2) ; Deutsch (L1)"
        let filteredSEs = getters.rawData.filter( se => {
          return se.sprachen == item;
        }).map( se => se);
  
        filteredSprachenSEList = [...filteredSprachenSEList, ...filteredSEs];
      });
    }
    return filteredSprachenSEList;
  },
  // return the list of speech events, filtered by the user selection of Themen criteria (treeselect input area)
  themenFilter: (state, getters) => {
    let filteredThemenSEList = new Set();// so as not to have duplicates

    if (state.selectedThemen.length == 0) {
      // if nothing is selected in the input area, return the list with all speech events
      filteredThemenSEList = getters.rawData;
    } else {
      state.selectedThemen.forEach( item => {
        // filter data by selected themen, ex.: ["Kinderbuch","türkische Wörter","Zähneputzen"]
        let filteredSEs = getters.rawData.filter( se => {
          return se.themen.includes(item);
        }).map( se => se );
  
        filteredThemenSEList = [...filteredThemenSEList, ...filteredSEs];
      });
    }
    return filteredThemenSEList;
  },
  // return the list of speech events, filtered by the user-selected range (slider) for Wortschatz (Niveaustufe)
  frequenzlistenFilter: (state, getters) => {
    let filtered = getters.rawData;
    if (state.frequenzlistenOptions.includes(state.selectedFrequenzliste)) {
        filtered = getters.rawData.filter(se => {
          const ratio = isNaN(se.maße.wortschatz[state.selectedFrequenzliste].tokens_ratio)
            ? 0 // if the value is not valid (= "nicht verfügbar"), set value to 0
            : +se.maße.wortschatz[state.selectedFrequenzliste].tokens_ratio.replace(",", ".") * 100; //  // otherwise read value and extract a numeric value from it; * 100 for %
          const selectedRangeStart = state.frequenzlistenRange[0];
          const selectedRangeEnd = state.frequenzlistenRange[1];
          return ratio >= selectedRangeStart && ratio <= selectedRangeEnd;
          // return _.inRange(ratio, selectedRangeStart, selectedRangeEnd);
        }).map( se => se );
    }
    return filtered;
  },
  // return the list of speech events, filtered by the user-selected range (slider) for Standardnähe (Normalisierungsrate)
  normalisierungFilter: (state, getters) => {
    return getters.rawData.filter(se => {
      const ratio = isNaN(se.maße.normalisierungsrate)
      ? 0 // if the value is not valid (= "nicht verfügbar"), set value to 0
      : +se.maße.normalisierungsrate.replace(",", "."); // otherwise read value and extract a numeric value from it
      const selectedRangeStart = state.normalisierungRange[0];
      const selectedRangeEnd = state.normalisierungRange[1];
      return ratio >= selectedRangeStart && ratio <= selectedRangeEnd;
      // return _.inRange(ratio, selectedRangeStart, selectedRangeEnd);
    }).map( se => se );
  },
  // return the list of speech events, filtered by the user-selected range (slider) for Überlappungen
  ueberlappungenRangeFilter: (state, getters) => {
    return getters.rawData.filter(se => {
      const ratio = +se.maße.anteil_Überlappungen_mit_mehr_als_2_wörtern_tokens.replace(",", "."); // read value and extract a numeric value from it
      const selectedRangeStart = state.ueberlappungenRange[0];
      const selectedRangeEnd = state.ueberlappungenRange[1];
      return ratio >= selectedRangeStart && ratio <= selectedRangeEnd;

      // return _.inRange(ratio, selectedRangeStart, selectedRangeEnd);
    }).map( se => se );
  },
  // return the list of speech events, filtered by the user-selected range (slider) for Sprechgeschwindigkeit (Artikulationsrate)
  artikulationsrateRangeFilter: (state, getters) => {
    return getters.rawData.filter(se => {
      const ratio = +se.maße.artikulationsrate.replace(",", "."); // read value and extract a numeric value from it
      const selectedRangeStart = state.artikulationsrateRange[0];
      const selectedRangeEnd = state.artikulationsrateRange[1];
      return ratio >= selectedRangeStart && ratio <= selectedRangeEnd;
      // return _.inRange(ratio, selectedRangeStart, selectedRangeEnd);
    }).map( se => se );
  },
  // return the list of speech events, filtered by the user-drawn range in the graph area
  graphSelectionFilter: (state, getters) => {
    return state.isEmptyGraphSelection
      ? getters.rawData // // if nothing is selected in the graph area, return the list with all speech events
      : getters.rawData.filter(se => {
        let yValue;
        // make sure to access the correct value for the currently selected Y axis
        if (state.frequenzlistenOptions.includes(state.selectedYAxisGraph)) {
          yValue = isNaN(se.maße.wortschatz[state.selectedYAxisGraph].tokens_ratio)
            ? 0 // if the value is not valid (= "nicht verfügbar"), set value to 0
            : +se.maße.wortschatz[state.selectedYAxisGraph].tokens_ratio.replace(",", "."); // otherwise read value and extract a numeric value from it
        } else if (getters.getWortarten.includes(state.selectedYAxisGraph)) {
          yValue = isNaN(se.maße.wortarten[state.selectedYAxisGraph].token_ratio)
            ? 0 // if the value is not valid (= "nicht verfügbar"), set value to 0
            : +se.maße.wortarten[state.selectedYAxisGraph].token_ratio.replace(',','.'); // otherwise read value and extract a numeric value from it
        } else if (getters.getMündlichkeitsphänomene.includes(state.selectedYAxisGraph)) {
          yValue = isNaN(se.maße.mündlichkeitsphänomene[state.selectedYAxisGraph].token_ratio)
            ? 0 // if the value is not valid (= "nicht verfügbar"), set value to 0
            : +se.maße.mündlichkeitsphänomene[state.selectedYAxisGraph].token_ratio.replace(',','.'); // otherwise read value and extract a numeric value from it
        } else if (state.selectedYAxisGraph === 'dauer') {
          yValue = se.maße[state.selectedYAxisGraph];
        } else {
          yValue = isNaN(se.maße[state.selectedYAxisGraph])
            ? 0 // if the value is not valid (= "nicht verfügbar"), set value to 0
            : +se.maße[state.selectedYAxisGraph].replace(",", "."); // otherwise read value and extract a numeric value from it
        }

        // Y coordinates of the user-drawn selection on the graph
        const selectedRangeStartY = state.brushedGraph.y[0];
        const selectedRangeEndY = state.brushedGraph.y[1];

        const intervalParser = d3.timeParse('%H:%M:%S');
        
        // check if the speech event Y value falls within user selection
        const isInRangeY = state.selectedYAxisGraph != 'dauer' || intervalParser(selectedRangeStartY) === null
          ? yValue >= selectedRangeStartY && yValue <= selectedRangeEndY
          : intervalParser(yValue).getTime() >= intervalParser(selectedRangeStartY).getTime() && intervalParser(yValue).getTime() <= intervalParser(selectedRangeEndY).getTime();

        let xValue;
        // make sure to access the correct value for the currently selected X axis
        if (state.frequenzlistenOptions.includes(state.selectedXAxisGraph)) {
          xValue = isNaN(se.maße.wortschatz[state.selectedXAxisGraph].tokens_ratio)
            ? 0 // if the value is not valid (= "nicht verfügbar"), set value to 0
            : +se.maße.wortschatz[state.selectedXAxisGraph].tokens_ratio.replace(",", "."); // otherwise read value and extract a numeric value from it
        } else if (getters.getWortarten.includes(state.selectedXAxisGraph)) {
          xValue = isNaN(se.maße.wortarten[state.selectedXAxisGraph].token_ratio)
            ? 0 // if the value is not valid (= "nicht verfügbar"), set value to 0
            : +se.maße.wortarten[state.selectedXAxisGraph].token_ratio.replace(',','.'); // otherwise read value and extract a numeric value from it
        } else if (getters.getMündlichkeitsphänomene.includes(state.selectedXAxisGraph)) {
          xValue = isNaN(se.maße.mündlichkeitsphänomene[state.selectedXAxisGraph].token_ratio)
            ? 0 // if the value is not valid (= "nicht verfügbar"), set value to 0
            : +se.maße.mündlichkeitsphänomene[state.selectedXAxisGraph].token_ratio.replace(',','.'); // otherwise read value and extract a numeric value from it
        } else if (state.selectedXAxisGraph === 'dauer') {
          xValue = se.maße[state.selectedXAxisGraph];
        } else {
          xValue = isNaN(se.maße[state.selectedXAxisGraph])
            ? 0 // if the value is not valid (= "nicht verfügbar"), set value to 0
            : +se.maße[state.selectedXAxisGraph].replace(",", "."); // otherwise read value and extract a numeric value from it
        }

        // X coordinates of the user-drawn selection on the graph
        const selectedRangeStartX = state.brushedGraph.x[0];
        const selectedRangeEndX = state.brushedGraph.x[1];
        // check if the speech event X value falls within user selection
        const isInRangeX = state.selectedXAxisGraph != 'dauer' || intervalParser(selectedRangeStartX) === null
          ? xValue >= selectedRangeStartX && xValue <= selectedRangeEndX
          : intervalParser(xValue).getTime() >= intervalParser(selectedRangeStartX).getTime() && intervalParser(xValue).getTime() <= intervalParser(selectedRangeEndX).getTime();          
        
        
        return isInRangeY && isInRangeX
      }).map(se => se);
  }
}
