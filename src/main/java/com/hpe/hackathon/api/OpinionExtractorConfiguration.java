package com.hpe.hackathon.api;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hpe.aspen.core.api.ApiConfiguration;

@Service
public class OpinionExtractorConfiguration extends ApiConfiguration {
    
    public class JobserviceConfiguration {

        @NotEmpty
        private String host;
        
        @NotEmpty
        private String type;
        
        @Min(1)
        @Max(65535)
        private Integer port = 5672;
        
        @Min(1)
        private Integer version = 1;
        
        @JsonProperty
        public String getHost() {
            return host;
        }

        @JsonProperty
        public void setHost(String host) {
            this.host = host;
        }
        
        @JsonProperty
        public String getType() {
            return type;
        }

        @JsonProperty
        public void setType(String type) {
            this.type = type;
        }
        
        @JsonProperty
        public Integer getPort() {
            return port;
        }

        @JsonProperty
        public void setPort(Integer port) {
            this.port = port;
        }
        
        @JsonProperty
        public Integer getVersion() {
            return version;
        }

        @JsonProperty
        public void setVersion(Integer version) {
            this.version = version;
        }
        
        @JsonIgnore
        public String getJobserviceEndpoint() {
            return String.format("%s://%s:%s/job-service/v%s", type, host, port, version);
        }
    }
    
    public class MessagingConfiguration {
                
        
        @NotEmpty
        private String inputQueueName;
        
        @NotEmpty
        private String outputQueueName;                
        
        
        
        @JsonProperty("input-queue")
        public String getInputQueueName() {
            return this.inputQueueName;
        }

        @JsonProperty("input-queue")
        public void setInputQueueName(String q) {
            this.inputQueueName = q;
        }
        
        @JsonProperty("output-queue")
        public String getOutputQueueName() {
            return this.outputQueueName;
        }

        @JsonProperty("output-queue")
        public void setOutputQueueName(String q) {
            this.outputQueueName = q;
        }
                
       
    }
    
    @Valid
    @NotNull
    JobserviceConfiguration jobservice = new JobserviceConfiguration();
    
    @Valid
    @NotNull
    MessagingConfiguration messaging = new MessagingConfiguration();
    
    
    @Valid
    @NotNull
    String analytics;
    
    @Valid
    @NotNull
    String applicationResources;
    
    
    @JsonProperty("jobservice")
    public JobserviceConfiguration getJobserviceConfiguration() {
        return this.jobservice;
    }
    
    @JsonProperty("jobservice")
    public void setJobserviceConfiguration(JobserviceConfiguration ms) {
        this.jobservice = ms;
    }
    
    @JsonProperty("messaging")
    public MessagingConfiguration getMessagingConfiguration() {
        return this.messaging;
    }
    
    @JsonProperty("messaging")
    public void setMessagingConfiguration(MessagingConfiguration ms) {
        this.messaging = ms;
    }
    
    @JsonProperty
    public String getAnalytics() {
        return this.analytics;
    }
    
    @JsonProperty
    public void setAnalytics(String a) {
        this.analytics = a;
    }
    
    @JsonProperty("application-resources")
    public String getApplicationResources() {
        return this.applicationResources;
    }
    
    @JsonProperty("application-resources")
    public void setApplicationResources(String a) {
        this.applicationResources = a;
    }

}
