package com.hpe.hackathon.processor;

import java.net.URLEncoder;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hpe.caf.client.RestClient;

public class ApplicationResourcesRegistrator {
    
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationResourcesRegistrator.class);
    
    public static class Response {
        private Integer featureEntityId;
        private Integer opinionEntityId;
        public Integer getFeatureEntityId() {
            return featureEntityId;
        }
        public void setFeatureEntityId(Integer featureEntityId) {
            this.featureEntityId = featureEntityId;
        }
        public Integer getOpinionEntityId() {
            return opinionEntityId;
        }
        public void setOpinionEntityId(Integer opinionEntityId) {
            this.opinionEntityId = opinionEntityId;
        }
    }
    
    protected String applicationResourcesUrl;
    
    public ApplicationResourcesRegistrator(String applicationResourcesUrl) {
        this.applicationResourcesUrl = applicationResourcesUrl;
    }
    
    /*
     * Associates feature with productId and opinion with feature
     * Returns: ProcessResponse if associations have been made, otherwise return null
     */
    public Response process(Integer productId, String feature, String opinion) {
        int featureId = this.getEntityId("value", feature, "feature");
        int opinionId = this.getEntityId("value", opinion, "opinion");
        
        if (featureId == 0 ) {
            featureId = saveFeatureOrOpinion(feature, "feature");
        }
        if (opinionId == 0 ) {
            opinionId = saveFeatureOrOpinion(opinion, "opinion");
        }
        
        try {
            associate(featureId, opinionId);
        }
        catch (Exception e) {
            LOG.debug("Association between feature " + featureId + " and opinion " + opinionId + " already failed to be created: might already exist");
            try {
                associate(productId, featureId);
            }
            catch (Exception ex) {
                LOG.debug("Association between product " + productId + " and feature " + featureId + " already failed to be created: might already exist");
            }
            return (Response)null;
        }
        
        try {
            associate(productId, featureId);
        }
        catch (Exception e) {
            LOG.debug("Association between product " + productId + " and feature " + featureId + " already failed to be created: might already exist");
        }
        
        try {
            associate(productId, opinionId);
        }
        catch (Exception e) {
            LOG.debug("Association between product " + productId + " and opinion " + opinionId + " already failed to be created: might already exist");
        }
        
        Response response = new Response();
        
        response.setFeatureEntityId(featureId);
        response.setOpinionEntityId(opinionId);
        
        return response;
    }
    
    private void associate(Integer parentId, Integer entityId) throws Exception {
        String url = this.applicationResourcesUrl + "/association";
        Map<String, Object> association = new HashMap<String,Object>();
        association.put("entityId", entityId);
        association.put("parentId", parentId);
        RestClient<Map<String,Object>> rc = new RestClient<Map<String,Object>>(){};
        
        rc.header("X-TENANT-ID", "111800881824924672").post(url, association);
    }
    
    private Integer saveFeatureOrOpinion(String value, String type) {
       Map<String, Object> entity = new HashMap<String,Object>();
       Map<String, Object> attributes = new HashMap<String,Object>();
       attributes.put("value", value);
       entity.put("name", value);
       entity.put("type", type);
       entity.put("attributes", attributes);
       
       String url = this.applicationResourcesUrl + "/entity";
       RestClient<Map<String,Object>> rc = new RestClient<Map<String,Object>>(){};
       Map<String,Object> response = rc.header("X-TENANT-ID", "111800881824924672").post(url, entity);
       
       Integer id = (Integer)response.get("id");
       
       return id; 
    }
    
    private Integer getEntityId(String attribute, String value, String type) {
        String attributes = String.format("{\"%s\": \"%s\"}", attribute, value);
        try {
            attributes = URLEncoder.encode(attributes, "UTF-8");
        }
        catch(Exception e) {
            // later;
        }
        String url = this.applicationResourcesUrl + "/entity?type=%s&attributes=%s";
        url = String.format(url, type, attributes);
        
        RestClient<List<Map<String,Object>>> rc = new RestClient<List<Map<String,Object>>>(){};
        List<Map<String,Object>> response = rc.header("X-TENANT-ID", "111800881824924672").get(url);
        
        if (response.size() == 0) {
            return 0;
        }
        
        Integer id = (Integer)response.get(0).get("id");
        return id;
    }
}
