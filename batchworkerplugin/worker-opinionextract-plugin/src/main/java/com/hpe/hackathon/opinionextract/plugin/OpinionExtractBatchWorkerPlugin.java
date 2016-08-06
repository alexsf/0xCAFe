package com.hpe.hackathon.opinionextract.plugin;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hpe.caf.worker.batch.BatchWorkerPlugin;
import com.hpe.caf.worker.batch.BatchWorkerServices;

import com.hpe.caf.client.RestClient;

public class OpinionExtractBatchWorkerPlugin implements BatchWorkerPlugin {
    
    private static final Logger LOG = LoggerFactory.getLogger(OpinionExtractBatchWorkerPlugin.class);
    
    
    @Override
    public void processBatch(BatchWorkerServices batchWorkerServices, String batchDefinition, String taskMessageType, Map<String, String> taskMessageParams) {
        LOG.debug("#0 - received batchDefinition: " + batchDefinition);        
    }
    
}
