package com.inwza.inventory.infrastructure.grpc;

import com.inwza.contracts.inventory.InventoryGrpcServiceGrpc;
import com.inwza.contracts.inventory.IsAvailableToSellReq;
import com.inwza.contracts.inventory.IsAvailableToSellRes;
import com.inwza.inventory.application.InventoryApplicationService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class InventoryGrpcServiceImpl extends InventoryGrpcServiceGrpc.InventoryGrpcServiceImplBase {

    private final InventoryApplicationService inventoryService;

    public InventoryGrpcServiceImpl(InventoryApplicationService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public void isAvailableToSell(IsAvailableToSellReq request, StreamObserver<IsAvailableToSellRes> responseObserver) {
        boolean available = inventoryService.isAvailableToSell(request.getPlayerId(), request.getItemId());
        responseObserver.onNext(IsAvailableToSellRes.newBuilder().setIsAvailable(available).build());
        responseObserver.onCompleted();
    }
}
