package com.hpe.hackathon.runner;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hpe.caf.client.RestClient;
import com.hpe.caf.services.job.client.model.NewJob;
import com.hpe.caf.services.job.client.model.WorkerAction;
import com.hpe.caf.services.job.client.model.WorkerAction.TaskDataEncodingEnum;
import com.hpe.caf.worker.batch.BatchWorkerConstants;
import com.hpe.caf.worker.batch.BatchWorkerTask;
import com.hpe.hackathon.api.OpinionExtractorConfiguration;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import java.util.*;

@Service
@Api(value = "hackathon/api", description = "Opinion Extractor")
@Path("/")
public class OpinionExtractorRequestHandler {
    
    @Autowired
    OpinionExtractorConfiguration apiConfiguration;
    
    @Autowired
    Client client;
    
    private static final Logger logger = LoggerFactory.getLogger(OpinionExtractorRequestHandler.class);
    
    public static class Review {
        private Integer id = null;
        private String text = null;
        private Integer productId = null;
        public Integer getId() {
            return id;
        }
        public void setId(Integer id) {
            this.id = id;
        }
        public String getText() {
            return text;
        }
        public void setText(String text) {
            this.text = text;
        }
        public Integer getProductId() {
            return productId;
        }
        public void setProduct(Integer productId) {
            this.productId = productId;
        }
    }
    
    public static class FeatureOpinion {
        private String feature;
        private String opinion;
        private Integer productId = null;
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
        public void setProduct(Integer productId) {
            this.productId = productId;
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
    
    /***public OpinionExtractorRequestHandler() {
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
    public Response extractFeatureOpinion(String payload) {
        
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
    
    @POST
    @Path("/opinion/graph")
    @Timed
    @Produces(MediaType.TEXT_PLAIN)    
    @ApiOperation(value = "Text Graph of NLP Parse", notes = "Renders graph of NLP parse")
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "Error Code: 2200.  Something wrong")
    })
    @ApiImplicitParams({
        @ApiImplicitParam(name = "X-TENANT-ID", required = false, value = "Tenant ID", dataType = "string", paramType = "header", defaultValue = "1")
    })
    public Response produceGraph(String sentence) {
        
        Extract extract = Extract.getInstance();
        String output = extract.graph(sentence);
        
        return Response.ok(output).build();
    }***/
    
    @POST
    @Path("/opinion/review")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)    
    @ApiOperation(value = "Submit Review", notes = "Submit Review")
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "Error Code: 2200.  Something wrong")
    })
    @ApiImplicitParams({
        @ApiImplicitParam(name = "X-TENANT-ID", required = false, value = "Tenant ID", dataType = "string", paramType = "header", defaultValue = "1")
    })
    public Review submitReview(@ApiParam(value = "Review Text") Review review) {
        
        String jobId = UUID.randomUUID().toString();
        logger.debug("Created JobId #" + jobId);
        
        String payload = new String(this.getTaskDataAsBytes(review));
        NewJob extractJob = createBatchJob(jobId, payload);
        
        
        RestClient<String> restClient = (new RestClient<String>(client) {});
        restClient.put(apiConfiguration.getJobserviceConfiguration().getJobserviceEndpoint() + "/jobs/" + jobId, extractJob);
        
        return review;
    }
    
    private byte[] getTaskDataAsBytes(Object taskData) {
        try {            
            ObjectMapper mapper = new ObjectMapper();
            byte b[] = mapper.writeValueAsBytes(taskData);
            
            return b;
        }
        catch (Exception e) {
            throw new RuntimeException (e);
        }
    }
    
    /***/
    protected NewJob createBatchJob(String jobId, String payload) {
        String jobName = "Job_" + jobId;
        NewJob newJob = new NewJob();
        newJob.setDescription(jobName  +  "__description");
        newJob.setExternalData(jobName +  "__externalData");
        newJob.setName(jobName);
        
        WorkerAction workerAction = new WorkerAction();
        workerAction.setTaskClassifier(BatchWorkerConstants.WORKER_NAME);
        workerAction.setTaskApiVersion(BatchWorkerConstants.WORKER_API_VERSION);
        workerAction.setTaskDataEncoding(TaskDataEncodingEnum.UTF8);
        
        workerAction.setTaskPipe(apiConfiguration.getMessagingConfiguration().getInputQueueName());
        workerAction.setTargetPipe(apiConfiguration.getMessagingConfiguration().getOutputQueueName());
        
        BatchWorkerTask task = new BatchWorkerTask();
        
        Map<String, String> taskMessageParams = new HashMap<String, String>();
        taskMessageParams.put("analytics", apiConfiguration.getAnalytics());
        taskMessageParams.put("applicationResources", apiConfiguration.getApplicationResources());
        
        task.batchDefinition = payload;
        task.batchType = "OpinionExtractBatchWorkerPlugin";
        task.taskMessageParams = taskMessageParams;
        task.targetPipe = apiConfiguration.getMessagingConfiguration().getOutputQueueName();
        
        String taskString = new String(getTaskDataAsBytes(task));
        
        workerAction.setTaskData(taskString);
        
        newJob.setTask(workerAction);
        
        return newJob;
    }/***/

}
