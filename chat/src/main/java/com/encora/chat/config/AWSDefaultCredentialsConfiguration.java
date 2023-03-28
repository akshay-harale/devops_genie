package com.encora.chat.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;

@Configuration
public class AWSDefaultCredentialsConfiguration {

    // create logger
    private static final Logger logger = LoggerFactory.getLogger(AWSDefaultCredentialsConfiguration.class);

    @Bean
    public AwsCredentialsProvider getAwsCredentialsProvider() {
        logger.info("Creating AWSDefaultCredentialsConfiguration.getAwsCredentialsProvider()");
        return DefaultCredentialsProvider.builder().build();
    }

    @Bean
    public AwsRegionProvider getAwsRegionProvider() {
        return DefaultAwsRegionProviderChain.builder()
                .build();
    }
}
