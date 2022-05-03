// Data that is shared and accessed by the different components
// is stored here. Initial values are already defined, but these
// change as the data is loaded via AJAX, and as the filters are applied
import { parseHHMMSS } from '@/helper';

export default {
  corpusData: {
    FOLK: {
      data: [],
      gesprachstypData: [],
      themenData: [],
      artData: [],
      sprachregionData: [],
      sprachen: []
    },
    GWSS: {
      data: [],
      themenData: [],
      artData: [],
      sprachregionData: [],
      sprachen: []
    }
  },
  isLoading: false,
  sliderPairs: [],
  seDurations: [],
  selectedCorpus: "FOLK", // default corpus selection
  selectedGespraechstyp: [],
  selectedThemen: [],
  selectedArt: [], 
  selectedSprachregion: [],
  selectedSprachen: [],
  selectedFrequenzliste: "",
  selectedYAxisGraph: "normalisierungsrate", // default selection for Y Axis
  selectedXAxisGraph: "artikulationsrate", // default selection for X Axis
  selectedSeId: "",
  frequenzlistenOptions: ["GOETHE_A1", "GOETHE_A2", "GOETHE_B1", "HERDER_1000", "HERDER_2000", "HERDER_3000", "HERDER_4000", "HERDER_5000"],
  frequenzlistenRange: [],
  normalisierungRange: [],
  ueberlappungenRange: [],
  beitragslaengeRange: [],
  artikulationsrateRange: [],
  wortartenRanges: {
    ADJ: [],
    ADV: [],
    NE: [],
    NN: [],
    PTKVZ: [],
    V: []
  },
  mündlichkeitsphänomeneRanges: {
    CLITIC: [],
    NGHES: [],
    NGIRR: [],
    PTKMA: [],
    SEDM: [],
    SEQU: []
  },
  dauerRange: ["00:00","06:00"],
  yAxisOptions: [
    {item: "GOETHE_A1", name: "GOETHE A1"},
    {item: "GOETHE_A2", name: "GOETHE A2"},
    {item: "GOETHE_B1", name: "GOETHE B1"},
    {item: "HERDER_1000", name: "HERDER 1000"},
    {item: "HERDER_2000", name: "HERDER 2000"},
    {item: "HERDER_3000", name: "HERDER 3000"},
    {item: "HERDER_4000", name: "HERDER 4000"},
    {item: "HERDER_5000", name: "HERDER 5000"},
    {item: "normalisierungsrate", name: "Normalisierungsrate"},
    {item: "anteil_Überlappungen_mit_mehr_als_2_wörtern_tokens", name: "Überlappungen"},
    {item: "artikulationsrate", name: "Artikulationsrate"},
    {item: "dauer", name: "Dauer"},
    {item: "ADJ", name: "ADJ"},
    {item: "ADV", name: "ADV"},
    {item: "NE", name: "NE"},
    {item: "NN", name: "NN"},
    {item: "PTKVZ", name: "PTKVZ"},
    {item: "V", name: "V"},
    {item: "CLITIC", name: "CLITIC"},
    {item: "NGHES", name: "NGHES"},
    {item: "NGIRR", name: "NGIRR"},
    {item: "PTKMA", name: "PTKMA"},
    {item: "SEDM", name: "SEDM"},
    {item: "SEQU", name: "SEQU"}
  ],
  xAxisOptions: [
    {item: "GOETHE_A1", name: "GOETHE A1"},
    {item: "GOETHE_A2", name: "GOETHE A2"},
    {item: "GOETHE_B1", name: "GOETHE B1"},
    {item: "HERDER_1000", name: "HERDER 1000"},
    {item: "HERDER_2000", name: "HERDER 2000"},
    {item: "HERDER_3000", name: "HERDER 3000"},
    {item: "HERDER_4000", name: "HERDER 4000"},
    {item: "HERDER_5000", name: "HERDER 5000"},
    {item: "normalisierungsrate", name: "Normalisierungsrate"},
    {item: "anteil_Überlappungen_mit_mehr_als_2_wörtern_tokens", name: "Überlappungen"},
    {item: "artikulationsrate", name: "Artikulationsrate"},
    {item: "dauer", name: "Dauer"},
    {item: "ADJ", name: "ADJ"},
    {item: "ADV", name: "ADV"},
    {item: "NE", name: "NE"},
    {item: "NN", name: "NN"},
    {item: "PTKVZ", name: "PTKVZ"},
    {item: "V", name: "V"},
    {item: "CLITIC", name: "CLITIC"},
    {item: "NGHES", name: "NGHES"},
    {item: "NGIRR", name: "NGIRR"},
    {item: "PTKMA", name: "PTKMA"},
    {item: "SEDM", name: "SEDM"},
    {item: "SEQU", name: "SEQU"}
  ],
  filteredSEList: [],
  brushRange: { // Dauer
    min: new Date(parseHHMMSS('0:00:00')),
    max: new Date(parseHHMMSS('6:00:00'))
  },
  brushedGraph: {
    x: [0, 0],
    y: [0, 0]
  },
  isEmptyGraphSelection: true,
  config: {}
};
