package com.encora.chat.service.impl;

import com.encora.chat.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@RequiredArgsConstructor
@Service
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    @Override
    public void uploadTOS3(String senderName, String response) {

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket("terraform-code-poc").key(senderName+"/"+senderName+"_"+getDate()+".tf").build();
        s3Client.putObject(putObjectRequest, RequestBody.fromString(response));
    }
    private String getDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(new Date());
    }
}
