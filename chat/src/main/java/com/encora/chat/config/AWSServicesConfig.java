package com.encora.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@RequiredArgsConstructor
public class AWSServicesConfig {
    private final AwsCredentialsProvider awsCredentials;
    private final AwsRegionProvider awsRegionProvider;

    @Bean
    public S3Client getAmazonS3Client() {
        return S3Client.builder()
                .credentialsProvider(awsCredentials)
                .region(awsRegionProvider.getRegion())
                .build();
    }

    @Bean
    public Ec2Client getEC2Client() {
        return Ec2Client.builder()
                .credentialsProvider(awsCredentials)
                .region(awsRegionProvider.getRegion())
                .build();
    }

    @Bean
    public EcsClient getEcsClient() {
        return EcsClient.builder()
                .credentialsProvider(awsCredentials)
                .region(awsRegionProvider.getRegion())
                .build();
    }
}
