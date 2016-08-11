package com.hpe.hackathon.runner;

import java.util.*;
import javax.ws.rs.client.Client;
import com.hpe.hackathon.api.OpinionExtractorConfiguration;
import com.hpe.aspen.core.api.ApiThreadLocal;
import com.hpe.caf.client.RestClient;

public class OpinionSearcher extends EntitySearcher {
    
    public OpinionSearcher(OpinionExtractorConfiguration apiConfiguration, Client client) {
        super(apiConfiguration, client);
    }
    
    public List<Map<String, String>> getSearchResults(Integer productId, String feature) {
        /*
        [
            {
              "productId": "10000",
              "feature": "location",
              "opinion": "nice"
              "count": "10"
            }
        ],
        where count - number of occurences an opinion has accross all features of the product
        */
        
        Integer featureEntityId = getEntityId(feature, "feature");
        String payload = String.format("{\"printEntities\": [\"opinion\"],\"qstmt\": \"product==%s AND feature==%s\",\"etypes\": [\"item\"]}", productId, featureEntityId);
        
        /*
         
        {\"printEntities\": [\"opinion\"],\"qstmt\": \"product==%s AND feature==%s\",\"etypes\": [\"item\"]}
          
          
         */
        
        String url = apiConfiguration.getAnalytics() + "/results";
        RestClient<Map<String, Object>> rc = new RestClient<Map<String, Object>>(client){};
        Map<String, Object> response = rc.header("X-TENANT-ID", ApiThreadLocal.get()).post(url, payload);
        
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        
        //
        // walk thru response and form a result for output
        //
        response.keySet();
        
        return result;
    }
}
