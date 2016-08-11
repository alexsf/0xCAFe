package com.hpe.hackathon.processor;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hpe.caf.client.RestClient;

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
        if (filterExists(entityId, type)) {
            return;
        }
        createFilter(entityId, type);
    }
    
    
    public void process(Integer featureEntityId, Integer opinionEntityId) {
        LOG.debug("featureEntityId/opinionEntityId");
        //
        // Cantor algorithm (http://math.stackexchange.com/questions/23503/create-unique-number-from-2-numbers)
        //
        //Integer pairingResult = ((featureEntityId + opinionEntityId)*(featureEntityId + opinionEntityId + 1))/2 + opinionEntityId;
        //process(pairingResult, "fop");
        
        // or as an alternative: http://stackoverflow.com/questions/919612/mapping-two-integers-to-one-in-a-unique-and-deterministic-way
        // Szudzik's function:
        Integer pairingResult = featureEntityId >= opinionEntityId ? featureEntityId * featureEntityId + featureEntityId + opinionEntityId : featureEntityId + opinionEntityId * opinionEntityId;
        process(pairingResult, "fop");
    }
    
    private boolean filterExists(Integer entityId, String type) {
        String url = this.analyticsUrl + "/filters?label=%s&type=%s";
        url = String.format(url, entityId, type);
        RestClient<Map<String,Object>> rc = new RestClient<Map<String,Object>>(){};
        try {
            Map<String,Object> response = rc.header("X-TENANT-ID", "111800881824924672").get(url);
            Integer totalResults = (Integer)response.get("totalResults");
            LOG.debug("Found " + totalResults + " filter(s) for label " + entityId + "and type " + type);
            return true;
        }
        catch (Exception e) {
            LOG.debug("Filter for label " + entityId + " and type " + type + " not found", e);
            return false;
        }
        
    }
    
    private void createFilter(Integer entityId, String type) {
        String url = this.analyticsUrl + "/filters";
        Map<String, Object> payload = new HashMap<String,Object>();
        payload.put("label", String.valueOf(entityId));
        payload.put("type", type);
        RestClient<Map<String,Object>> rc = new RestClient<Map<String,Object>>(){};
        try {
            rc.header("X-TENANT-ID", "111800881824924672").post(url, payload);
        }
        catch (Exception e) {
            LOG.debug("Filter for label " + entityId + " and type " + type + " not created", e);
        }
        
    }
}
