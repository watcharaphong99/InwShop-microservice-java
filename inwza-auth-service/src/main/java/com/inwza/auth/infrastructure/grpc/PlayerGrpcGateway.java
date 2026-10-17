package com.inwza.auth.infrastructure.grpc;

import com.inwza.auth.application.AuthFailureException;
import com.inwza.contracts.player.CredentialSearchReq;
import com.inwza.contracts.player.FindOnePlayerProfileToRefreshReq;
import com.inwza.contracts.player.PlayerGrpcServiceGrpc;
import com.inwza.contracts.player.PlayerProfile;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class PlayerGrpcGateway {

    @GrpcClient("player")
    private PlayerGrpcServiceGrpc.PlayerGrpcServiceBlockingStub playerStub;

    public PlayerProfile credentialSearch(String email, String password) {
        try {
            return playerStub.credentialSearch(CredentialSearchReq.newBuilder()
                    .setEmail(email)
                    .setPassword(password)
                    .build());
        } catch (StatusRuntimeException ex) {
            if (ex.getStatus().getCode() == Status.Code.UNAUTHENTICATED
                    || ex.getStatus().getCode() == Status.Code.PERMISSION_DENIED) {
                throw new AuthFailureException("error: grpc authorization failed");
            }
            throw new AuthFailureException("error: email or password is incorrect");
        } catch (Exception ex) {
            throw new AuthFailureException("error: gRpc connection failed");
        }
    }

    public PlayerProfile findProfileForRefresh(String playerIdWithoutPrefix) {
        try {
            return playerStub.findOnePlayerProfileToRefresh(FindOnePlayerProfileToRefreshReq.newBuilder()
                    .setPlayerId(playerIdWithoutPrefix)
                    .build());
        } catch (Exception ex) {
            throw new AuthFailureException("error: player profile not found");
        }
    }
}
