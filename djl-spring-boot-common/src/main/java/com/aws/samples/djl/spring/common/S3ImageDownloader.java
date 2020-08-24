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
package com.aws.samples.djl.spring.common;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class S3ImageDownloader {

    private static final Logger LOG = LoggerFactory.getLogger(S3ImageDownloader.class);

    private AmazonS3 s3;

    private String bucketName;

    private String folder;

    public S3ImageDownloader(AmazonS3 s3, String bucketName, String folder) {
        this.s3 = s3;
        this.bucketName = bucketName;
        this.folder = folder == null ? "" : folder.concat("/");
    }

    public InputStream downloadStream(String fileName) throws IOException {
        String key = fileName.contains("/")? fileName:  folder.concat(fileName);
        LOG.info("Downloading {} from S3 bucket {}...\n", key, bucketName);
        return s3.getObject(bucketName, key).getObjectContent();
    }

    public ObjectListing listFolder() {
        return s3.listObjects(bucketName, folder);
    }

    public ObjectListing listFolder(String folder) {
        return s3.listObjects(bucketName, folder);
    }
}
