package com.inwza.payment.infrastructure.grpc;

import com.inwza.contracts.item.FindItemsInIdsReq;
import com.inwza.contracts.item.FindItemsInIdsRes;
import com.inwza.contracts.item.ItemGrpcServiceGrpc;
import com.inwza.payment.application.PaymentDomainException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ItemGrpcGateway {

    @GrpcClient("item")
    private ItemGrpcServiceGrpc.ItemGrpcServiceBlockingStub itemStub;

    public FindItemsInIdsRes findItemsInIds(List<String> itemIds) {
        try {
            FindItemsInIdsRes res =
                    itemStub.findItemsInIds(FindItemsInIdsReq.newBuilder().addAllIds(itemIds).build());
            if (res.getItemsCount() == 0) {
                throw new PaymentDomainException("error: items not found");
            }
            return res;
        } catch (PaymentDomainException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new PaymentDomainException("error: items not found");
        }
    }
}
