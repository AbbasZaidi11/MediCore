package org.example.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BillingServiceGrpcClient {

    private final BillingServiceGrpc.BillingServiceBlockingStub blockingStub;

    //localhost: 9001/BillingService/CreatePatientAccount
    // aws.grpc: 123123/BillingService/CreatePatientAccount
    public BillingServiceGrpcClient(
            @Value("${billing.service.address:localhost}") String serverAddress, // Default to localhost if not set
            @Value("${billing.service.grpc.port:9001}") int serverPort // Default to 9001 if not set
    ) {
        log.info("Connection to Billing Service GRPC service at {}:{}", serverAddress, serverPort);

        ManagedChannel channel = ManagedChannelBuilder //   Create a channel to the gRPC server
                .forAddress(serverAddress, serverPort) // Set the server address and port
                .usePlaintext() // -- In production, you should use TLS for secure communication
                .build(); // Create a channel to the gRPC server

        blockingStub = BillingServiceGrpc.newBlockingStub(channel); // Create a blocking stub for making synchronous calls to the gRPC service
    }

    public BillingResponse createBillingAccount(String patientId, String name, String email) {
        // Build the gRPC request with patient details
        BillingRequest request = BillingRequest.newBuilder()
                // Set the patient ID, name, and email in the request
                .setPatientId(patientId)
                // Set the patient ID, name, and email in the request
                .setName(name)
                // Set the patient ID, name, and email in the request
                .setEmail(email)
                //  Build the gRPC request with patient details
                .build();
        //  Build the gRPC request with patient details
        BillingResponse response = blockingStub.createBillingAccount(request);
        // Log the response received from the billing service
        log.info("Received response from billing service via GRPC: {}", response);
        // Return the response received from the billing service
        return response;
    }
}
