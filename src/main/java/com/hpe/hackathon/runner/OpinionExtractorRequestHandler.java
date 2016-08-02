package com.hpe.hackathon.runner;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hpe.hackathon.stanford.nlp.Extract;
import com.hpe.hackathon.stanford.nlp.Pattern;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import java.util.*;

@Service
@Api(value = "hackathon/api", description = "Opinion Extractor")
@Path("/")
public class OpinionExtractorRequestHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(OpinionExtractorRequestHandler.class);
    
    public static class FeatureOpinion {
        private String feature;
        private String opinion;
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
    
    public OpinionExtractorRequestHandler() {
        logger.info("Initializing Extractor instance, please wait...");
        Extract.getInstance();
        logger.info("Done initializing Extractor instance!");
    }
    
    @POST
    @Path("/opinion")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)    
    @ApiOperation(value = "Extract feature/opinion pairs", notes = "Extracts feature/opinion pairs from arbitrary text")
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "Error Code: 2200.  Something wrong")
    })
    @ApiImplicitParams({
        @ApiImplicitParam(name = "X-TENANT-ID", required = false, value = "Tenant ID", dataType = "string", paramType = "header", defaultValue = "1")
    })
    public Response doSomething(String payload) {
        
        Extract extract = Extract.getInstance();
        java.util.List<Pattern> patterns = extract.run(payload);
        
        List<FeatureOpinion> featureOpinionPairs = new ArrayList<FeatureOpinion>();
        
        for (Pattern pattern : patterns) {
            FeatureOpinion featureOpinion = new FeatureOpinion();
            featureOpinion.setFeature(pattern.head);
            featureOpinion.setOpinion(pattern.modifier);
            
            featureOpinionPairs.add(featureOpinion);
        }
        
        ExtractionResults extractionResults = new ExtractionResults();
        extractionResults.setFeatureOpinionPairs(featureOpinionPairs);
        
        return Response.ok(extractionResults).build();
    }

}
