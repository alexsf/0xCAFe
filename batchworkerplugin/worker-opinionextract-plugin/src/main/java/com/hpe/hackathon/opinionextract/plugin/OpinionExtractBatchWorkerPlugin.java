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
import com.hpe.hackathon.processor.TaggingRegistrator;
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
        private String feature;
        private String opinion;
        private Integer productId;
        private Integer reviewId;
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
        public Integer getReviewId() {
            return reviewId;
        }
        public void setReviewId(Integer reviewId) {
            this.reviewId = reviewId;
        }
    }
    
    public static class ExtractionResults {
        
        private String type = "fop";
        
        @JsonProperty("pairs")
        private List<FeatureOpinion> featureOpinionPairs;

        public List<FeatureOpinion> getFeatureOpinionPairs() {
            return featureOpinionPairs;
        }

        public void setFeatureOpinionPairs(List<FeatureOpinion> featureOpinionPairs) {
            this.featureOpinionPairs = featureOpinionPairs;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
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
                
                String trackFeaturesCsv = taskMessageParams.get("trackFeatures");
                String[] trackFeatures = trackFeaturesCsv.split(",");
                
                String sentence = (String)mapRequest.get("text");
                Integer reviewId = (Integer)mapRequest.get("reviewid");
                if (isSentenceToBeIgnored(sentence, trackFeatures)) {
                    LOG.debug("#0.1(a) - ignoring sentence: " + sentence);
                    FeatureOpinion fop = new FeatureOpinion();
                    Integer productId = (Integer)mapRequest.get("productId");
                    fop.setFeature("");
                    fop.setOpinion("");
                    fop.setProductId(productId);
                    fop.setReviewId(reviewId);
                    batchWorkerServices.registerItemSubtask("NOOP:sentence-ignored", 1, fop);
                    return;
                }
                
                
                
                Integer productid = (Integer)mapRequest.get("productId");
                LOG.debug("#0.2 - received sentence: " + sentence);
                
                // Check sentence length, make sure it does not exceed 100 characters (see Stanford CoreNLP)
                if (sentence.length() > 100) {
                    LOG.debug("#0.1(b) - truncating sentence to no more than 100 characters " + sentence);
                    sentence = sentence.substring(0, 100);
                }
                
                java.util.List<Pattern> patterns = extract.run(sentence);
                
                List<FeatureOpinion> featureOpinionPairs = new ArrayList<FeatureOpinion>();
                
                for (Pattern pattern : patterns) {
                    FeatureOpinion featureOpinion = new FeatureOpinion();
                    featureOpinion.setFeature(pattern.head);
                    featureOpinion.setOpinion(pattern.modifier);
                    featureOpinion.setProductId(productid);
                    featureOpinion.setReviewId(reviewId);
                    
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
                
                ApplicationResourcesRegistrator applicationResourcesRegistrator = new ApplicationResourcesRegistrator(taskMessageParams.get("applicationResources"));
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> pairs = (List<Map<String, Object>>)mapRequest.get("pairs");
                int nPairs = pairs.size();
                int nIgnoredPairs = 0;
                for (Map<String, Object> pair : pairs) {
                    
                    String feature = (String)pair.get("feature");
                    
                    String trackFeaturesCsv = taskMessageParams.get("trackFeatures");
                    String[] trackFeatures = trackFeaturesCsv.split(",");
                    
                    if (isFeatureToBeIgnored(feature, trackFeatures)) {
                        nIgnoredPairs++;
                        continue;
                    }
                    
                    String opinion = (String)pair.get("opinion");
                    Integer productId = (Integer)pair.get("productId");
                    Integer reviewId = (Integer)pair.get("reviewId");
                    
                    
                    LOG.debug("#0.3(a)--->" + String.format("Preparing for Analysis:  {\"feature\": \"%s\", \"opinion\": \"%s\", \"reviewId\": %s}", feature, opinion, reviewId));
                    
                    
                    ApplicationResourcesRegistrator.Response applicationResourcesRegistratorResponse = applicationResourcesRegistrator.process(productId, feature, opinion);
                    
                    if (applicationResourcesRegistratorResponse != null) {
                        AnalyticsRegistrator analyticsRegistrator = new AnalyticsRegistrator(taskMessageParams.get("analytics"));
                        analyticsRegistrator.process(productId, "product");
                        analyticsRegistrator.process(applicationResourcesRegistratorResponse.getFeatureEntityId(), "feature");
                        analyticsRegistrator.process(applicationResourcesRegistratorResponse.getOpinionEntityId(), "opinion");
                        analyticsRegistrator.process(applicationResourcesRegistratorResponse.getFeatureEntityId(), applicationResourcesRegistratorResponse.getOpinionEntityId());
                        
                        TaggingRegistrator taggingRegistrator = new TaggingRegistrator(taskMessageParams.get("tagging"));
                        taggingRegistrator.process(reviewId, productId);
                        taggingRegistrator.process(reviewId, applicationResourcesRegistratorResponse.getFeatureEntityId());
                        taggingRegistrator.process(reviewId, applicationResourcesRegistratorResponse.getOpinionEntityId());
                        taggingRegistrator.process(reviewId, applicationResourcesRegistratorResponse.getFeatureEntityId(), applicationResourcesRegistratorResponse.getOpinionEntityId());
                    }
                                       
                                        
                    FeatureOpinion fop = new FeatureOpinion();
                    fop.setFeature(feature);
                    fop.setOpinion(opinion);
                    fop.setProductId(productId);
                    batchWorkerServices.registerItemSubtask("NOOP:fop-extracted", 1, fop);
                }
                
                if (nPairs == nIgnoredPairs) {
                    FeatureOpinion fop = new FeatureOpinion();
                    Integer productId = (Integer)mapRequest.get("productId");
                    fop.setFeature("");
                    fop.setOpinion("");
                    fop.setProductId(productId);
                    batchWorkerServices.registerItemSubtask("NOOP:fop-ignored", 1, fop);
                }
                
                
            }
        }
        
        
    }
    
    private boolean isSentenceToBeIgnored(String sentence, String[] trackFeatures) {
        if (trackFeatures == null || trackFeatures.length == 0) {
            // no particular feature to track, therefore will track everything
            return false;
        }
        boolean ignore = true;
        for (String feature : trackFeatures) {
            if (sentence.contains(feature)) {
                ignore = false;
                break;
            }
        }
        return ignore;
        
    }
    
    private boolean isFeatureToBeIgnored(String feature, String[] trackFeatures) {
        if (trackFeatures == null || trackFeatures.length == 0) {
            // no particular feature to track, therefore will track everything
            return false;
        }
        boolean ignore = true;
        for (String trackFeature : trackFeatures) {
            
            if (trackFeature.equalsIgnoreCase(feature)) {
                ignore = false;
                break;
            }
        }
        return ignore;
        
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
