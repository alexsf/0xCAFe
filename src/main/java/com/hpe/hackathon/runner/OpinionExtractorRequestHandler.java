package com.hpe.hackathon.runner;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hpe.aspen.core.api.ApiThreadLocal;
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
    
    @GET
    @Path("/opinion/search")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)    
    @ApiOperation(value = "Search all", notes = "Search all")
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "Error Code: 2200.  Something wrong")
    })
    @ApiImplicitParams({
        @ApiImplicitParam(name = "X-TENANT-ID", required = false, value = "Tenant ID", dataType = "string", paramType = "header", defaultValue = "111800881824924672")
    })
    public List<ProductFeatureResponse> search(@ApiParam(value = "Feature") @QueryParam("feature") String feature, @ApiParam(value = "Opinion") @QueryParam("opinion") String opinion) {
        /*
         [
            {
              "name": "Bellagio",
              "productId": "10000",
              "feature": "location",
              "opinion": "central",
              "count": "10"
            }
         ]
         */
        
        List<Map<String, String>> listProducts = new ProductSearcher(apiConfiguration, client).getSearchResults(feature, opinion);
        List<ProductFeatureResponse> productFeatureResponses = new ArrayList<ProductFeatureResponse>();
        for (Map<String, String> product : listProducts) {
            Integer productId = Integer.parseInt((String)product.get("productId"));
            List<Map<String, String>> opinions = new OpinionSearcher(apiConfiguration, client).getSearchResults(productId, feature);
            
            ProductFeatureResponse productFeatureResponse = new ProductFeatureResponse();
            productFeatureResponses.add(productFeatureResponse);
            productFeatureResponse.setFeature(feature);
            productFeatureResponse.setName((String)product.get("name"));
            productFeatureResponse.setSize(Integer.parseInt((String)product.get("count")));
            
            List<OpinionResponse> opinionResponses = new ArrayList<OpinionResponse>();
            productFeatureResponse.setData(opinionResponses);
            
            for (Map<String,String> opini : opinions) {
                OpinionResponse or = new OpinionResponse();
                int size = Integer.parseInt((String)opini.get("count"));
                or.setText((String)opini.get("opinion"));
                or.setSize(size);
                opinionResponses.add(or);
            }
        }
        Collections.sort(productFeatureResponses, new Comparator<ProductFeatureResponse>(){
            @Override
            public int compare(ProductFeatureResponse o1, ProductFeatureResponse o2) {
                return o1.getSize().compareTo(o2.getSize());
            }
        });
        return productFeatureResponses;
    }
    
    @GET
    @Path("/opinion/search/product")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)    
    @ApiOperation(value = "Products matching feature/opinion search", notes = "Products matching feature/opinion search")
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "Error Code: 2200.  Something wrong")
    })
    @ApiImplicitParams({
        @ApiImplicitParam(name = "X-TENANT-ID", required = false, value = "Tenant ID", dataType = "string", paramType = "header", defaultValue = "111800881824924672")
    })
    public Response searchProduct(@ApiParam(value = "Feature") @QueryParam("feature") String feature, @ApiParam(value = "Opinion") @QueryParam("opinion") String opinion) {
        /*
         [
            {
              "name": "Bellagio",
              "productId": "10000",
              "feature": "location",
              "opinion": "central",
              "count": "10"
            }
         ]
         */
        
        List<Map<String, String>> listProducts = new ProductSearcher(apiConfiguration, client).getSearchResults(feature, opinion);
        return Response.ok(listProducts).build();
    }
    
    @GET
    @Path("/opinion/search/opinion")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)    
    @ApiOperation(value = "Opinions matching product/feature search", notes = "Opinions matching product/feature search")
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "Error Code: 2200.  Something wrong")
    })
    @ApiImplicitParams({
        @ApiImplicitParam(name = "X-TENANT-ID", required = false, value = "Tenant ID", dataType = "string", paramType = "header", defaultValue = "111800881824924672")
    })
    public Response searchOpinion(@ApiParam(value = "Product ID") @QueryParam("productId") Integer productId, @ApiParam(value = "Feature") @QueryParam("feature") String feature) {
        List<Map<String, String>> opinions = new OpinionSearcher(apiConfiguration, client).getSearchResults(productId, feature);
        return Response.ok(opinions).build();
    }
    
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
        NewJob extractJob = createBatchJob(jobId, payload, review.getProductId());
        
        
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
    protected NewJob createBatchJob(String jobId, String payload, Integer productId) {
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
        taskMessageParams.put("trackFeatures", getTrackFeatures(productId));
        taskMessageParams.put("tagging", apiConfiguration.getTagging());
        
        task.batchDefinition = payload;
        task.batchType = "OpinionExtractBatchWorkerPlugin";
        task.taskMessageParams = taskMessageParams;
        task.targetPipe = apiConfiguration.getMessagingConfiguration().getOutputQueueName();
        
        String taskString = new String(getTaskDataAsBytes(task));
        
        workerAction.setTaskData(taskString);
        
        newJob.setTask(workerAction);
        
        return newJob;
    }/***/
    
    @SuppressWarnings("unchecked")
    private String getTrackFeatures(Integer productId) {
        RestClient<List<Map<String, Object>>> rc = new RestClient<List<Map<String, Object>>>() {};
        String url = apiConfiguration.getApplicationResources() + "/entity?type=product&attributes=";
        try {
            String attributes = java.net.URLEncoder.encode("{\"productId\":  " + productId + "}", "UTF-8");
            url = url + attributes;
        }
        catch (Exception e) {
            //
        }
        List<Map<String, Object>> results = rc.header("X-TENANT-ID", ApiThreadLocal.get()).get(url);
        Map<String, Object> result = results.get(0);
        Map<String, Object> attributes = (Map<String, Object>)result.get("attributes");
        List<String> trackFeatures = (List<String>)attributes.get("track-features");
        
        return String.join(",", trackFeatures);
    }

}
