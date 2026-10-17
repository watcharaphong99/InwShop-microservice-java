package com.inwza.player.infrastructure.grpc;

import com.inwza.contracts.player.CredentialSearchReq;
import com.inwza.contracts.player.FindOnePlayerProfileToRefreshReq;
import com.inwza.contracts.player.GetPlayerSavingAccountReq;
import com.inwza.contracts.player.GetPlayerSavingAccountRes;
import com.inwza.contracts.player.PlayerGrpcServiceGrpc;
import com.inwza.contracts.player.PlayerProfile;
import com.inwza.player.application.PlayerApplicationService;
import com.inwza.player.application.PlayerIdCodec;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class PlayerGrpcServiceImpl extends PlayerGrpcServiceGrpc.PlayerGrpcServiceImplBase {

    private final PlayerApplicationService playerService;

    public PlayerGrpcServiceImpl(PlayerApplicationService playerService) {
        this.playerService = playerService;
    }

    @Override
    public void credentialSearch(CredentialSearchReq request, StreamObserver<PlayerProfile> responseObserver) {
        PlayerProfile profile = playerService.credentialSearch(request.getEmail(), request.getPassword());
        responseObserver.onNext(profile);
        responseObserver.onCompleted();
    }

    @Override
    public void findOnePlayerProfileToRefresh(
            FindOnePlayerProfileToRefreshReq request, StreamObserver<PlayerProfile> responseObserver) {
        PlayerProfile profile = playerService.findProfileForRefresh(request.getPlayerId());
        responseObserver.onNext(profile);
        responseObserver.onCompleted();
    }

    @Override
    public void getPlayerSavingAccount(
            GetPlayerSavingAccountReq request, StreamObserver<GetPlayerSavingAccountRes> responseObserver) {
        var account = playerService.getSavingAccount(PlayerIdCodec.format(request.getPlayerId()));
        responseObserver.onNext(GetPlayerSavingAccountRes.newBuilder()
                .setPlayerId(account.playerId())
                .setBalance(account.balance())
                .build());
        responseObserver.onCompleted();
    }
}
