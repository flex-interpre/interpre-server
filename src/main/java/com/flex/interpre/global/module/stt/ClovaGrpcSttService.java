package com.flex.interpre.global.module.stt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flex.interpre.global.property.ClovaProperty;
import com.naver.cloud.speech.grpc.*;
import io.grpc.*;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClovaGrpcSttService {

    private static final String GRPC_HOST = "clovaspeech-gw.ncloud.com";
    private static final int GRPC_PORT = 50051;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ClovaProperty clovaProperty;

    private ManagedChannel channel;
    private NestServiceGrpc.NestServiceStub asyncStub;

    @PostConstruct
    private void initChannel() {
        channel = NettyChannelBuilder
                .forAddress(GRPC_HOST, GRPC_PORT)
                .useTransportSecurity()
                .build();

        asyncStub = NestServiceGrpc.newStub(channel);
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public StreamObserver<NestRequest> startStreaming(
            WebSocketSession session,
            TranscriptionCallback onTranscription
    ) {
        StreamObserver<NestResponse> observer = new StreamObserver<>() {

            @Override
            public void onNext(NestResponse response) {
                String contents = response.getContents();

                try {
                    JsonNode jsonNode = objectMapper.readTree(contents);

                    if (jsonNode.has("responseType")) {
                        JsonNode responseTypeNode = jsonNode.get("responseType");
                        if (responseTypeNode.isArray() && !responseTypeNode.isEmpty()) {
                            String responseType = responseTypeNode.get(0).asText();

                            if ("transcription".equals(responseType)) {
                                JsonNode transcription = jsonNode.get("transcription");
                                if (transcription != null && transcription.has("text")) {
                                    String text = transcription.get("text").asText();
                                    if (!text.isEmpty()) {
                                        onTranscription.onTranscription(text);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("STT 응답 파싱 오류: {}", e.getMessage(), e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("gRPC STT 오류: {}", throwable.getMessage(), throwable);

                try {
                    if (session.isOpen()) {
                        String errorJson = objectMapper.writeValueAsString(
                                Map.of("success", false, "message", "STT 오류: " + throwable.getMessage())
                        );
                        session.sendMessage(new TextMessage(errorJson));
                        session.close();
                    }
                } catch (Exception e) {
                    log.error("WebSocket 오류 처리 실패: {}", e.getMessage(), e);
                }
            }

            @Override
            public void onCompleted() {
                log.info("gRPC STT 스트림 완료");
                try {
                    if (session.isOpen()) {
                        session.close(CloseStatus.NORMAL);
                    }
                } catch (Exception e) {
                    log.error("WebSocket 종료 실패: {}", e.getMessage(), e);
                }
            }
        };

        ClientInterceptor interceptor = new ClientInterceptor() {
            @Override
            public <REQUEST, RESPONSE> ClientCall<REQUEST, RESPONSE> interceptCall(
                    MethodDescriptor<REQUEST, RESPONSE> method,
                    CallOptions callOptions,
                    Channel next
            ) {
                return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
                    @Override
                    public void start(Listener<RESPONSE> responseListener, Metadata headers) {
                        headers.put(
                                Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER),
                                "Bearer " + clovaProperty.getSttSecret()
                        );
                        super.start(responseListener, headers);
                    }
                };
            }
        };

        return asyncStub.withInterceptors(interceptor).recognize(observer);
    }

    public void sendConfig(StreamObserver<NestRequest> requestObserver) {
        NestConfig config = NestConfig.newBuilder()
                .setConfig("{\"transcription\":{\"language\":\"ko\"}}")
                .build();

        NestRequest request = NestRequest.newBuilder()
                .setType(RequestType.CONFIG)
                .setConfig(config)
                .build();

        requestObserver.onNext(request);
    }

    public void sendAudioData(StreamObserver<NestRequest> requestObserver, byte[] audioData) {
        NestData data = NestData.newBuilder()
                .setChunk(com.google.protobuf.ByteString.copyFrom(audioData))
                .build();

        NestRequest request = NestRequest.newBuilder()
                .setType(RequestType.DATA)
                .setData(data)
                .build();

        requestObserver.onNext(request);
    }

    @FunctionalInterface
    public interface TranscriptionCallback {
        void onTranscription(String text);
    }
}
