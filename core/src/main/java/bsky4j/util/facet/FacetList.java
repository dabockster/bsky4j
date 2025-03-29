package bsky4j.util.facet;

import bsky4j.model.bsky.richtext.RichtextFacet;
import bsky4j.model.bsky.richtext.RichtextFacetByteSlice;
import bsky4j.model.bsky.richtext.RichtextFacetLink;
import bsky4j.model.bsky.richtext.RichtextFacetMention;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacetList {

    private final List<FacetRecord> records;

    public FacetList(List<FacetRecord> records) {
        this.records = records;
    }

    public String getDisplayText() {
        return records.stream()
                .map(FacetRecord::getDisplayText)
                .collect(Collectors.joining(""));
    }

    /**
     * RichtextFacet のリストに変換
     * Convert to RichtextFacet List
     */
    public List<RichtextFacet> getRichTextFacets(
            Map<String, String> handleToDidMap
    ) {
        int bytes = 0;
        List<RichtextFacet> facets = new ArrayList<>();
        
        // Pre-calculate byte lengths for all records to avoid repeated calculations
        Map<FacetRecord, Integer> byteLengths = new HashMap<>();
        for (FacetRecord record : records) {
            byteLengths.put(record, record.getDisplayText().getBytes(StandardCharsets.UTF_8).length);
        }

        for (FacetRecord record : records) {
            int recordByteLength = byteLengths.get(record);
            
            switch (record.getType()) {
                case Text: {
                    bytes += recordByteLength;
                    break;
                }

                case Mention:
                    // If DID is set, prepare a Facet as a link
                    if (handleToDidMap.containsKey(record.getDisplayText())) {
                        RichtextFacetByteSlice slice = new RichtextFacetByteSlice();

                        slice.setByteStart(bytes);
                        bytes += recordByteLength;
                        slice.setByteEnd(bytes);

                        RichtextFacet facet = new RichtextFacet();
                        facet.setFeatures(new ArrayList<>());
                        facet.setIndex(slice);

                        RichtextFacetMention mention = new RichtextFacetMention();
                        mention.setDid(handleToDidMap.get(record.getDisplayText()));
                        facet.getFeatures().add(mention);
                        facets.add(facet);

                    } else {
                        // If DID is not set, display as simple text
                        bytes += recordByteLength;
                    }
                    break;

                case Link: {
                    RichtextFacetByteSlice slice = new RichtextFacetByteSlice();

                    slice.setByteStart(bytes);
                    bytes += recordByteLength;
                    slice.setByteEnd(bytes);

                    RichtextFacet facet = new RichtextFacet();
                    facet.setFeatures(new ArrayList<>());
                    facet.setIndex(slice);

                    RichtextFacetLink mention = new RichtextFacetLink();
                    mention.setUri(record.getContentText());
                    facet.getFeatures().add(mention);

                    facets.add(facet);
                    break;
                }
            }
        }

        return facets;
    }

    public List<FacetRecord> getRecords() {
        return records;
    }
}
