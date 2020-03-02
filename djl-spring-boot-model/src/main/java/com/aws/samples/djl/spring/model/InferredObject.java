/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.aws.samples.djl.spring.model;

public class InferredObject {

    private String objectClass;

    private Double probability;

    public InferredObject(){}

    public InferredObject(String objectClass, Double probability) {
        this.objectClass = objectClass;
        this.probability = probability;
    }

    public String getObjectClass() {
        return objectClass;
    }

    public Double getProbability() {
        return probability;
    }
}
