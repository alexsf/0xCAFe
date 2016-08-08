package com.hpe.hackathon.processor;

public class ApplicationResourcesRegistrator {
    
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
    
    /*
     * Associates feature with productId and opinion with feature
     * Returns: ProcessResponse if associations have been made, otherwise return null
     */
    public static Response process(Integer productId, String feature, String opinion) {
        return (Response)null;
    }
}
