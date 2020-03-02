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
package com.aws.samples.djldemoweb.controller

import com.aws.samples.djl.spring.common.S3ImageDownloader
import com.aws.samples.djl.spring.common.S3ImageUploader
import com.aws.samples.djldemoweb.backend.ObjectDetectionClient
import com.aws.samples.djldemoweb.form.ObjectDetectionForm
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.time.Duration


@Controller
class ObjectDetectionController(private val uploader: S3ImageUploader,
                                private val downloader: S3ImageDownloader,
                                private val apiClient: ObjectDetectionClient) {

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(ObjectDetectionController::class.java)
    }

    @RequestMapping("/object-detection/inbox")
    fun listFiles(model: Model): String {
        model.addAttribute("files", downloader.listFolder("inbox"))
        return "object-detection-files"
    }

    @RequestMapping("/object-detection/inbox/new-object-detection")
    fun newObjectDetection(model: Model): String  {
        model.addAttribute("objectDetectionForm", ObjectDetectionForm())
        return "new-object-detection"
    }

    @RequestMapping("/object-detection/inbox", method = [RequestMethod.POST])
    fun detectObjects(@ModelAttribute form: ObjectDetectionForm, model: Model): String {
        if(form.file?.originalFilename == null) {
            return "object-detection-inbox"
        }
        val fileName = form.file?.originalFilename ?: ""
        uploader.upload(form.file?.inputStream, form.file?.size ?:0, fileName)
        val results = apiClient.detect(fileName, true).block(Duration.ofSeconds(30))
        val jsonResults = ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(results)
        LOG.info("Object detection results: {} ", jsonResults)
        model.addAttribute("files", downloader.listFolder("inbox"))
        model.addAttribute("results", jsonResults)
        model.addAttribute("originalFile", form.file?.originalFilename)
        model.addAttribute("resultFile", form.file?.originalFilename.plus(".png"))
        return "object-detection-files"
    }

    @RequestMapping("/object-detection/images/inbox/{file-name}")
    @ResponseBody
    fun getInboxImage(@PathVariable("file-name") fileName: String) : Resource {
        return InputStreamResource(downloader.downloadStream("inbox/".plus(fileName)))
    }

    @RequestMapping("/object-detection/images/outbox/{file-name}")
    @ResponseBody
    fun getOutboxImage(@PathVariable("file-name") fileName: String) : Resource {
        return InputStreamResource(downloader.downloadStream("outbox/".plus(fileName)))
    }
}
