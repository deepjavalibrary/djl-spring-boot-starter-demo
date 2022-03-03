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

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.translate.TranslateException;
import com.aws.samples.djl.spring.common.S3ImageDownloader;
import com.aws.samples.djl.spring.common.S3ImageUploader;
import com.aws.samples.djl.spring.model.InferenceResponse;
import com.aws.samples.djl.spring.model.InferredObject;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.function.Supplier;

@RestController
public class InferencePointController {

    private static final String PNG = ".png";

    @Resource
    private Supplier<Predictor<Image, DetectedObjects>> predictorSupplier;

    @Resource
    private ImageFactory imageFactory;

    @Resource
	private S3ImageUploader uploader;

    @Resource
	private S3ImageDownloader downloader;

    @GetMapping
    @RequestMapping("/inference")
    public InferenceResponse detect(@RequestParam(name = "file") String fileName,
                                    @RequestParam(name = "generateOutputImage") Boolean generateOutputImage)
			throws IOException, TranslateException {

        Image image = imageFactory.fromInputStream(downloader.downloadStream(fileName));
        var inferredObjects = new LinkedList<InferredObject>();

        var outputReference = "";

        try(var p = predictorSupplier.get()) {
            var detected = p.predict(image);
            if(generateOutputImage != null && generateOutputImage) {
                RenderedImage newImage = createImage(detected, image);
                outputReference = uploader.upload(newImage, fileName.concat(PNG));
            }
            detected.items().forEach(e -> inferredObjects.add(new InferredObject(e.getClassName(), e.getProbability())));
            return new InferenceResponse(inferredObjects, outputReference);
        }
    }

    private static RenderedImage createImage(DetectedObjects detection, Image original) {
        Image newImage = original.duplicate();
        newImage.drawBoundingBoxes(detection);
		return (RenderedImage) newImage.getWrappedImage();
	}

}
