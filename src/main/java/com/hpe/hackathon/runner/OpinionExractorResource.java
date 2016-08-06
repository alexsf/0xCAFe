package com.hpe.hackathon.runner;

import java.util.Properties;

import com.hpe.aspen.core.api.ApiApplication;
import com.hpe.hackathon.api.OpinionExtractorConfiguration;


public class OpinionExractorResource extends ApiApplication<OpinionExtractorConfiguration>{

    @Override
    protected Properties getSpringProperties(OpinionExtractorConfiguration apiConfiguration) {
        apiConfiguration.getJobserviceConfiguration();
        Properties p = new Properties();
        return p;
    }
    
    public static void main(String[] args) throws Exception {
        new OpinionExractorResource().run(args);
    }

}
