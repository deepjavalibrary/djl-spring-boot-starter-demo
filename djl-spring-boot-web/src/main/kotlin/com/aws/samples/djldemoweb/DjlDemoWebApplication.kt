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
package com.aws.samples.djldemoweb

import com.aws.samples.djl.spring.common.AmazonClientConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@SpringBootApplication
@Import(AmazonClientConfiguration::class)
class DjlDemoWebApplication {

	companion object {
		val LOG: Logger = LoggerFactory.getLogger(DjlDemoWebApplication::class.java)
	}

	@Value("\${djl.app.url}")
	lateinit var apiUrl: String

	@Bean
	fun backendWebClient(builder: WebClient.Builder): WebClient {
		LOG.info("Initializing backend API client with url: {}", apiUrl)
		return builder.baseUrl(apiUrl)
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.build();
	}
}

fun main(args: Array<String>) {
	runApplication<DjlDemoWebApplication>(*args)
}
