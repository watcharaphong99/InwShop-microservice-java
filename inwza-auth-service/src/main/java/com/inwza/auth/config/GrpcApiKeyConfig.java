package com.inwza.auth.config;

import com.inwza.auth.application.InvalidTokenException;
import com.inwza.auth.application.JwtTokenService;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
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
                } catch (InvalidTokenException ex) {
                    call.close(Status.UNAUTHENTICATED.withDescription(ex.getMessage()), new Metadata());
                    return new ServerCall.Listener<>() {
                    };
                }
                return next.startCall(call, headers);
            }
        };
    }

    @GrpcGlobalClientInterceptor
    ClientInterceptor apiKeyClientInterceptor(JwtTokenService jwtTokenService) {
        return new ClientInterceptor() {
            @Override
            public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                    MethodDescriptor<ReqT, RespT> method,
                    CallOptions callOptions,
                    Channel next) {
                return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
                    @Override
                    public void start(Listener<RespT> responseListener, Metadata headers) {
                        headers.put(AUTH_KEY, jwtTokenService.apiKeyToken());
                        super.start(responseListener, headers);
                    }
                };
            }
        };
    }
}
