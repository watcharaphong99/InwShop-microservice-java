package com.inwza.item.infrastructure.grpc;

import com.inwza.contracts.item.FindItemsInIdsReq;
import com.inwza.contracts.item.FindItemsInIdsRes;
import com.inwza.contracts.item.ItemGrpcServiceGrpc;
import com.inwza.item.application.ItemApplicationService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class ItemGrpcServiceImpl extends ItemGrpcServiceGrpc.ItemGrpcServiceImplBase {

    private final ItemApplicationService itemService;

    public ItemGrpcServiceImpl(ItemApplicationService itemService) {
        this.itemService = itemService;
    }

    @Override
    public void findItemsInIds(FindItemsInIdsReq request, StreamObserver<FindItemsInIdsRes> responseObserver) {
        FindItemsInIdsRes response = itemService.findItemsInIds(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
