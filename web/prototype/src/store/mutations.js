// All the changes to the data in state.js is applied using functions (setters) defined here

export default {
  SET_CORPUS: (state, value) => {
    if (value != state.selectedCorpus) {
      state.selectedCorpus = value;
    }
  },
  SET_FREQUENZLISTE: (state, value) => {
    if (value != state.selectedFrequenzliste) {
      state.selectedFrequenzliste = value;
    }
  },
  SET_Y_AXIS: (state, value) => {
      state.selectedYAxisGraph = value;
  },
  SET_X_AXIS: (state, value) => {
      state.selectedXAxisGraph = value;
  },
  SET_IS_EMPTY_GRAPH_SELECTION: (state, value) => {
      state.isEmptyGraphSelection = value;
  },
  SET_BRUSHED_GRAPH_RANGE: (state, value) => {
      state.brushedGraph = value;
  },
  SET_SELECTED_SE_ID: (state, value) => {
      state.selectedSeId = value;
  },
  UPDATE_RANGE: (state, {measure, extent}) => {
    switch (measure) {
      case "normalisierungsrate":
        state.normalisierungRange = extent;
        break;
      case "artikulationsrate":
        state.artikulationsrateRange = extent;
        break;
      case "anteil_Überlappungen_mit_mehr_als_2_wörtern_tokens":
        state.ueberlappungenRange = extent;
        break;
      case "dauer":
        state.dauerRange = extent;
        break;
      case "art":
        state.selectedArt = extent;
        break;
      case "sprachen":
        state.selectedSprachen = extent;
        break;
      case "themen":
        state.selectedThemen = extent;
        break;
      case "sprachregion":
        state.selectedSprachregion = extent;
        break;
      case "gespraechstyp":
        state.selectedGespraechstyp = extent;
        break;
      case "NE":
      case "NN":
      case "ADJ":
      case "ADV":
      case "PTKVZ":
      case "V":
        state.wortartenRanges[measure] = extent;
        break;
      case "CLITIC":
      case "NGHES":
      case "NGIRR":
      case "PTKMA":
      case "SEDM":
      case "SEQU":
        state.mündlichkeitsphänomeneRanges[measure] = extent;
        break;  
      default:
        state.frequenzlistenRange = extent;
        break;
    }
  },
  SET_IS_LOADING: (state, value) => {
    state.isLoading = value;
  },
  SET_AJAX_DATA(state, {corpus, key, data}) {
    state.corpusData[corpus][key] = data;
  },
  GET_CONFIG_AJAX(state, value) {
    state.config = value
  }
}
