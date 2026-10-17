package com.inwza.player.config;

import com.inwza.player.application.JwtTokenService;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcApiKeyConfig {

    private static final Metadata.Key<String> AUTH_KEY =
            Metadata.Key.of("auth", Metadata.ASCII_STRING_MARSHALLER);

    @GrpcGlobalServerInterceptor
    ServerInterceptor apiKeyServerInterceptor(JwtTokenService jwtTokenService) {
        return new ServerInterceptor() {
            @Override
            public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                    ServerCall<ReqT, RespT> call,
                    Metadata headers,
                    ServerCallHandler<ReqT, RespT> next) {
                String token = headers.get(AUTH_KEY);
                if (token == null || token.isBlank()) {
                    call.close(Status.UNAUTHENTICATED.withDescription("error: metadata not found"), new Metadata());
                    return new ServerCall.Listener<>() {
                    };
                }
                try {
                    jwtTokenService.validateApiKey(token);
                } catch (JwtTokenService.InvalidTokenException ex) {
                    call.close(Status.UNAUTHENTICATED.withDescription(ex.getMessage()), new Metadata());
                    return new ServerCall.Listener<>() {
                    };
                }
                return next.startCall(call, headers);
            }
        };
    }
}
