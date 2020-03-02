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
package com.aws.samples.djldemoweb.backend

import com.aws.samples.djl.spring.model.InferenceResponse
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class ObjectDetectionClient(private val webClient: WebClient) {

    fun detect(file: String, generateOutputImage: Boolean?): Mono<InferenceResponse> {
        return this.webClient.get().uri{
            builder -> builder.path("/inference")
                .queryParam("file", file)
                .queryParam("generateOutputImage", generateOutputImage)
                .build()
        }
                .retrieve()
                .bodyToMono(InferenceResponse::class.java)
    }
}