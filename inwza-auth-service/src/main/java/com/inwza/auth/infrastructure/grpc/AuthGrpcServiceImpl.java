package com.inwza.auth.infrastructure.grpc;

import com.inwza.auth.application.AuthApplicationService;
import com.inwza.contracts.auth.AccessTokenSearchReq;
import com.inwza.contracts.auth.AccessTokenSearchRes;
import com.inwza.contracts.auth.AuthGrpcServiceGrpc;
import com.inwza.contracts.auth.RolesCountReq;
import com.inwza.contracts.auth.RolesCountRes;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class AuthGrpcServiceImpl extends AuthGrpcServiceGrpc.AuthGrpcServiceImplBase {

    private final AuthApplicationService authService;

    public AuthGrpcServiceImpl(AuthApplicationService authService) {
        this.authService = authService;
    }

    @Override
    public void accessTokenSearch(AccessTokenSearchReq request, StreamObserver<AccessTokenSearchRes> responseObserver) {
        AccessTokenSearchRes response = authService.accessTokenSearch(request.getAccessToken());
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void rolesCount(RolesCountReq request, StreamObserver<RolesCountRes> responseObserver) {
        RolesCountRes response = authService.rolesCount();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
