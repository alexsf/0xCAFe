package com.hpe.hackathon.processor;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hpe.caf.client.RestClient;

public class TaggingRegistrator {
    protected String taggingUrl;
    private static final Logger LOG = LoggerFactory.getLogger(TaggingRegistrator.class);
    
    public TaggingRegistrator(String taggingUrl) {
        LOG.debug("Tagging URL: " + taggingUrl);
        this.taggingUrl = taggingUrl;
    }
    
    public void process(Integer reviewId, Integer entityId) {
        String url = this.taggingUrl + "/tag";
        Map<String,Object> payload = new HashMap<String,Object>();
        payload.put("entity", entityId);
        payload.put("documents", Arrays.asList(new String[] {String.valueOf(reviewId)}));
        
        RestClient<List<Map<String,Object>>> rc = new RestClient<List<Map<String,Object>>>(){};
        try {
            rc.header("X-TENANT-ID", "111800881824924672").post(url, payload);
        }
        catch(Exception e) {
            LOG.debug("Unable to submit tagging job.", e);
        }
    }
    
    public void process(Integer reviewId, Integer featureEntityId, Integer opinionEntityId) {
        Integer pairingResult = featureEntityId >= opinionEntityId ? featureEntityId * featureEntityId + featureEntityId + opinionEntityId : featureEntityId + opinionEntityId * opinionEntityId;
        process(reviewId, pairingResult);
    }

}
