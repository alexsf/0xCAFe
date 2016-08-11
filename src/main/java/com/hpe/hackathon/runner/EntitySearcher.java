package com.hpe.hackathon.runner;

import java.net.URLEncoder;
import java.util.*;
import javax.ws.rs.client.Client;
import com.hpe.hackathon.api.OpinionExtractorConfiguration;
import com.hpe.aspen.core.api.ApiThreadLocal;
import com.hpe.caf.client.RestClient;

public class EntitySearcher {
    protected OpinionExtractorConfiguration apiConfiguration;
    protected Client client;
    
    public EntitySearcher(OpinionExtractorConfiguration apiConfiguration, Client client) {
        this.apiConfiguration = apiConfiguration;
        this.client = client;
    }
    
    public Integer getEntityId(String featureOrOpinion, String type) {
        //
        // using RestClient make call to application resources:
        //
        //111800881824924672
        String attr = String.format("{\"value\": \"%s\"}", featureOrOpinion);
        try {
            attr = URLEncoder.encode(attr, "UTF-8");
        }
        catch(Exception e) {
            //
        }
        String url = String.format(apiConfiguration.getApplicationResources() + "/entity?type=%s&attributes=%s", type, attr);
        RestClient<List<Map<String, Object>>> rc = new RestClient<List<Map<String, Object>>>(client){};
        List<Map<String, Object>> responses = rc.header("X-TENANT-ID", ApiThreadLocal.get()).get(url);
        Map<String, Object> response = responses.get(0);
        Integer id = (Integer)response.get("id");
        return id;
    }
}
