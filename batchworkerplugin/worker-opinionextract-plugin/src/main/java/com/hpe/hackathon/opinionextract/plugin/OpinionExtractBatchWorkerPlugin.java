package com.hpe.hackathon.opinionextract.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hpe.caf.worker.batch.BatchWorkerPlugin;
import com.hpe.caf.worker.batch.BatchWorkerServices;
import com.hpe.hackathon.processor.AnalyticsRegistrator;
import com.hpe.hackathon.processor.ApplicationResourcesRegistrator;
import com.hpe.hackathon.stanford.nlp.Extract;
import com.hpe.hackathon.stanford.nlp.Pattern;

//import com.hpe.caf.client.RestClient;

public class OpinionExtractBatchWorkerPlugin implements BatchWorkerPlugin {
    
    private static final Logger LOG = LoggerFactory.getLogger(OpinionExtractBatchWorkerPlugin.class);
    private static Extract extract;
    static {
        LOG.info("Initializing NLP models, please wait...");
        extract = Extract.getInstance();
        LOG.info("NLP models have been initialized!");
    }
    
    public static class FeatureOpinion {
        private String type;
        private String feature;
        private String opinion;
        private Integer productId; 
        public String getFeature() {
            return feature;
        }
        public void setFeature(String feature) {
            this.feature = feature;
        }
        public String getOpinion() {
            return opinion;
        }
        public void setOpinion(String opinion) {
            this.opinion = opinion;
        }
        public Integer getProductId() {
            return productId;
        }
        public void setProductId(Integer productId) {
            this.productId = productId;
        }
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
    }
    
    public static class ExtractionResults {
        @JsonProperty("pairs")
        private List<FeatureOpinion> featureOpinionPairs;

        public List<FeatureOpinion> getFeatureOpinionPairs() {
            return featureOpinionPairs;
        }

        public void setFeatureOpinionPairs(List<FeatureOpinion> featureOpinionPairs) {
            this.featureOpinionPairs = featureOpinionPairs;
        }
    }
    
    @Override
    public void processBatch(BatchWorkerServices batchWorkerServices, String batchDefinition, String taskMessageType, Map<String, String> taskMessageParams) {
        LOG.debug("#0 - received batchDefinition: " + batchDefinition);
        
        
        Map<String,Object> mapRequest = jsonStringToMap(batchDefinition);
        Object requestType = mapRequest.get("type");
        if (requestType == null) {
            String text = (String)mapRequest.get("text");
            Integer productid = (Integer)mapRequest.get("productId");
            Integer reviewid = (Integer)mapRequest.get("id");
            LOG.debug("#0.1 - received review: " + text);
            List<String> sentences = extract.getSentences(text);
            for (String sentence : sentences) {
                String sentencePayload = String.format("{\"text\": \"%s\", \"productId\": %s, \"type\": \"sentence\", \"reviewid\": %s}", sentence, productid, reviewid);
                // back on input queue
                batchWorkerServices.registerBatchSubtask(sentencePayload);
            }
        }
        else  {
            String stringRequestType = (String)requestType;
            if ("sentence".equalsIgnoreCase(stringRequestType)) {
                String text = (String)mapRequest.get("text");
                Integer productid = (Integer)mapRequest.get("productId");
                LOG.debug("#0.2 - received sentence: " + text);
                java.util.List<Pattern> patterns = extract.run(text);
                
                List<FeatureOpinion> featureOpinionPairs = new ArrayList<FeatureOpinion>();
                
                for (Pattern pattern : patterns) {
                    FeatureOpinion featureOpinion = new FeatureOpinion();
                    featureOpinion.setFeature(pattern.head);
                    featureOpinion.setOpinion(pattern.modifier);
                    featureOpinion.setProductId(productid);
                    featureOpinion.setType("fop");
                    
                    featureOpinionPairs.add(featureOpinion);
                }
                
                ExtractionResults extractionResults = new ExtractionResults();
                extractionResults.setFeatureOpinionPairs(featureOpinionPairs);
                
                String extractionResultString = jsonObjectToString(extractionResults);
                
                // back on input queue
                batchWorkerServices.registerBatchSubtask(extractionResultString);
                
             
            }
            else if ("fop".equalsIgnoreCase(stringRequestType)) {
                
                LOG.debug("#0.3--->" + batchDefinition);
                
                Integer productId = (Integer)mapRequest.get("productId");
                String feature = (String)mapRequest.get("feature");
                String opinion = (String)mapRequest.get("opinion");
                
                ApplicationResourcesRegistrator.Response applicationResourcesRegistratorResponse = ApplicationResourcesRegistrator.process(productId, feature, opinion);
                if (applicationResourcesRegistratorResponse != null) {
                    AnalyticsRegistrator.process(productId, "product");
                    AnalyticsRegistrator.process(applicationResourcesRegistratorResponse.getFeatureEntityId(), "feature");
                    AnalyticsRegistrator.process(applicationResourcesRegistratorResponse.getOpinionEntityId(), "opinion");
                    AnalyticsRegistrator.process(applicationResourcesRegistratorResponse.getFeatureEntityId(), applicationResourcesRegistratorResponse.getOpinionEntityId());
                }
                
                // to next worker - which one ????
                batchWorkerServices.registerItemSubtask("BRIDGE_TO_NOWHERE", 1, new Object());
            }
        }
        
        
    }
    
    private Map<String,Object> jsonStringToMap(String jsonString) {
        Map<String,Object> map = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            map = mapper.readValue(jsonString.getBytes("UTF-8"), new TypeReference<Map<String,Object>>(){});
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        
        return map;
    }
    
    private byte[] jsonObjectToBytes(Object results) {
        try {            
            ObjectMapper mapper = new ObjectMapper();
            byte b[] = mapper.writeValueAsBytes(results);
            
            return b;
        }
        catch (Exception e) {
            throw new RuntimeException (e);
        }
    }
    
    private String jsonObjectToString(Object results) {
        return new String(jsonObjectToBytes(results));
    }
    
}
