package com.hpe.hackathon.runner;

import java.util.Properties;

import com.hpe.aspen.core.api.ApiApplication;


public class OpinionExractorResource extends ApiApplication<OpinionExtractorConfiguration>{

    @Override
    protected Properties getSpringProperties(OpinionExtractorConfiguration arg0) {
        Properties p = new Properties();
        return p;
    }
    
    public static void main(String[] args) throws Exception {
        new OpinionExractorResource().run(args);
    }

}
