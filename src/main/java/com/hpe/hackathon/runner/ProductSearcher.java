package com.hpe.hackathon.runner;

import java.util.*;
import javax.ws.rs.client.Client;

import com.hpe.aspen.core.api.ApiThreadLocal;
import com.hpe.caf.client.RestClient;
import com.hpe.hackathon.api.OpinionExtractorConfiguration;

public class ProductSearcher extends EntitySearcher {
    
    public ProductSearcher(OpinionExtractorConfiguration apiConfiguration, Client client) {
        super(apiConfiguration, client);
    }
    
    public List<Map<String, String>> getSearchResults(String feature, String opinion) {
        /*
        [
            {
              "name": "Bellagio",
              "productId": "10000",
              "feature": "location",
              "opinion": "central",
              "count": "10"
            }
        ],
        
        where
        count - number of documents matching  feature/opinion search per each product
        */
        Integer featureEntityId = getEntityId(feature, "feature");
        Integer opinionEntityId = getEntityId(opinion, "opinion");
        
        Integer fopEntityId = PairingFunction.apply(featureEntityId, opinionEntityId);
        Map<String, Object> products = getProducts(fopEntityId);
        
        
        
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> getProducts(Integer fopEntityId) {
        //
        /* using RestClient make call to analytics payload will be similar to:
        {\"printEntities\": [\"product\", \"feature\", \"opinion\", \"fop\"], \"qstmt\": \"fop==%s\",\"etypes\": [\"item\"]}
        */
        String payload = String.format("{\"printEntities\": [\"product\", \"feature\", \"opinion\", \"fop\"], \"qstmt\": \"fop==%s\",\"etypes\": [\"item\"]}", fopEntityId);
        RestClient<Map<String,Object>> rc = new RestClient<Map<String,Object>>(client){};
        Map<String,Object> response = rc.header("X-TENANT-ID", ApiThreadLocal.get()).post(apiConfiguration.getAnalytics() + "/results", payload);
        
        List<Map<String, Object>> matches = (List<Map<String, Object>>)response.get("entities");
        
        return response;
    }
}
