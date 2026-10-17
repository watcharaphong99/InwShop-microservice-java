package com.inwza.payment.config;

import com.inwza.payment.application.JwtTokenService;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcApiKeyConfig {

    private static final Metadata.Key<String> AUTH_KEY =
            Metadata.Key.of("auth", Metadata.ASCII_STRING_MARSHALLER);

    @GrpcGlobalClientInterceptor
    ClientInterceptor apiKeyClientInterceptor(JwtTokenService jwtTokenService) {
        return new ClientInterceptor() {
            @Override
            public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                    MethodDescriptor<ReqT, RespT> method,
                    io.grpc.CallOptions callOptions,
                    io.grpc.Channel next) {
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
