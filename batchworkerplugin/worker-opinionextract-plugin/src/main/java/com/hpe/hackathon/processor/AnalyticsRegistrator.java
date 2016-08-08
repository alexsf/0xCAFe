package com.hpe.hackathon.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyticsRegistrator {
    
    protected String analyticsUrl;
    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsRegistrator.class);
    
    public AnalyticsRegistrator(String analyticsUrl) {
        this.analyticsUrl = analyticsUrl;
    }
    
    /*
     * Registers entity id and its' type (i.e. "product", "feature", "opinion" or "fop")
     */
    public void process(Integer entityId, String type) {
        //
        // TODO:
        // POST http://localhost:8080/platform-services/api/filters:
        //  {
        //    "label": "1",
        //    "type": "feature"
        //  }
        //
        // where label is entityId (converted to string)
        LOG.debug("entityId/type");
    }
    
    
    public void process(Integer featureEntityId, Integer opinionEntityId) {
        LOG.debug("featureEntityId/opinionEntityId");
        //
        // Cantor algorithm (http://math.stackexchange.com/questions/23503/create-unique-number-from-2-numbers)
        //
        Integer pairingResult = ((featureEntityId + opinionEntityId)*(featureEntityId + opinionEntityId + 1))/2 + opinionEntityId;
        process(pairingResult, "fop");
        
        // or as an alternative: http://stackoverflow.com/questions/919612/mapping-two-integers-to-one-in-a-unique-and-deterministic-way
        // Szudzik's function:
        pairingResult = featureEntityId >= opinionEntityId ? featureEntityId * featureEntityId + featureEntityId + opinionEntityId : featureEntityId + opinionEntityId * opinionEntityId; 
    }
}
