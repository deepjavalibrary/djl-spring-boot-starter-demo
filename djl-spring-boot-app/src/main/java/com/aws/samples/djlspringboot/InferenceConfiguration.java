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
package com.aws.samples.djlspringboot;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.DetectedObjects;
import ai.djl.modality.cv.SingleShotDetectionTranslator;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.mxnet.zoo.MxModelZoo;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Pipeline;
import ai.djl.translate.Translator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Configuration
public class InferenceConfiguration {

    @Bean
    public Map<String, String> criteria() {
        Map<String, String> criteria = new ConcurrentHashMap<>();
        criteria.put("size", "512");
        criteria.put("backbone", "resnet50");
        criteria.put("flavor", "v1");
        criteria.put("dataset", "voc");
        return criteria;
    }

    @Bean
    public ZooModel<BufferedImage, DetectedObjects> model(@Qualifier("criteria") Map<String, String> criteria)
            throws MalformedModelException, ModelNotFoundException, IOException {
        return MxModelZoo.SSD.loadModel(criteria, new ProgressBar());
    }

    /**
     * Scoped proxy is one way to have a predictor configured and closed.
     * @param model
     * @param translator
     * @return
     */
    @Bean(destroyMethod = "close")
    @Scope(value = "prototype", proxyMode = ScopedProxyMode.INTERFACES)
    public Predictor<BufferedImage, DetectedObjects> predictor(ZooModel<BufferedImage, DetectedObjects> model,
                                                               Translator<BufferedImage, DetectedObjects> translator) {
        return model.newPredictor(translator);
    }

    /**
     * Inject with @Resource or autowired. Only safe to be used in the try with resources.
     * @param model
     * @param translator
     * @return
     */
    @Bean
    public Supplier<Predictor<BufferedImage, DetectedObjects>> predictorProvider(ZooModel<BufferedImage, DetectedObjects> model,
                                                                         Translator<BufferedImage, DetectedObjects> translator) {
        return () -> model.newPredictor(translator);
    }


    @Bean
    public Translator<BufferedImage, DetectedObjects> translator() {

        int width = 512;
        int height = 512;
        double threshold = 0.1d;

        Pipeline pipeline = new Pipeline();
        pipeline.add(new Resize(width, height)).add(new ToTensor());

        return SingleShotDetectionTranslator.builder()
                .setPipeline(pipeline)
                .setSynsetArtifactName("classes.txt")
                .optThreshold((float) threshold)
                .optRescaleSize(width, height)
                .build();
    }
}
