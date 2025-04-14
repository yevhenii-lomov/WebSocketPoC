package com.poc.websocketconnectionmanager.model;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ResponseStub {

    @NonNull
    private String status;
}
