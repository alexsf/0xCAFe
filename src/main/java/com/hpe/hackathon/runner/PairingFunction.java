package com.hpe.hackathon.runner;

public class PairingFunction {
    public static Integer apply(Integer featureEntityId, Integer opinionEntityId) {
        Integer pairingResult = featureEntityId >= opinionEntityId ? featureEntityId * featureEntityId + featureEntityId + opinionEntityId : featureEntityId + opinionEntityId * opinionEntityId;
        return pairingResult;
    }
}
