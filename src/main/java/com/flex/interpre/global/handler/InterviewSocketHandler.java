package com.flex.interpre.global.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flex.interpre.domain.interview.entity.InterviewSession;
import com.flex.interpre.domain.interview.exception.InterviewExceptions;
import com.flex.interpre.domain.interview.repository.InterviewSessionRepository;
import com.flex.interpre.domain.interview.service.InterviewService;
import com.flex.interpre.global.dto.ApiResponse;
import com.flex.interpre.global.exception.ApiException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class InterviewSocketHandler extends AbstractWebSocketHandler {

    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewService interviewService;
    private final ConcurrentHashMap<String, List<byte[]>> audioChunksMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {

        String sessionId = getSessionId(session);
        try {
            InterviewSession interviewSession = interviewSessionRepository.findById(UUID.fromString(sessionId))
                    .orElseThrow(InterviewExceptions.INVALID_SESSION_ID::toException);

            //TODO: 자소서 객체가 생성되면 llm으로 자소서 내용 전달.

            audioChunksMap.put(session.getId(), new ArrayList<>());
        } catch (ApiException e) {

            session.close(CloseStatus.NOT_ACCEPTABLE.withReason(e.getMessage()));
        } catch (Exception e) {

            session.close(CloseStatus.SERVER_ERROR.withReason("서버 에러 발생"));
        }


    }

    @Override
    public void handleBinaryMessage(@NonNull WebSocketSession session, @NonNull BinaryMessage message) throws IOException {

        try {
            String wsSessionId = session.getId();

            ByteBuffer byteBuffer = message.getPayload();
            byte[] audioChunk = new byte[byteBuffer.remaining()];
            byteBuffer.get(audioChunk);


            // 청크를 리스트에 추가
            List<byte[]> chunks = audioChunksMap.get(wsSessionId);
            if (chunks != null) {
                chunks.add(audioChunk);

            } else {
                session.close(CloseStatus.SERVER_ERROR.withReason("서버 에러 발생"));
            }
        } catch (Exception e) {
            session.close(CloseStatus.SERVER_ERROR.withReason("오디오 처리중 에러 발생"));
        }
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws IOException {

        try {
            String wsSessionId = session.getId();
            String payload = message.getPayload();

            // JSON 파싱, 종료 요청이 오는 경우
            if (payload.contains("\"type\":\"END\"")) {
                List<byte[]> chunks = audioChunksMap.get(wsSessionId);

                if (chunks == null || chunks.isEmpty()) {
                    sendResponse(session, ApiResponse.error(InterviewExceptions.NO_AUDIO_DATA.toException()));
                    return;
                }

                // 프론트에서 받은 PCM 합치기
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                for (byte[] chunk : chunks) {
                    outputStream.write(chunk);
                }
                byte[] mergedPcm = outputStream.toByteArray();

                // PCM → WAV 변환 (16kHz, mono, 16bit)
                byte[] wavData = addWavHeader(mergedPcm);

                // STT 실행
                String result = interviewService.transcribe(wavData);
                // 다음 요청을 위해 지우기
                chunks.clear();
            }

        } catch (ApiException e) {
            sendResponse(session, ApiResponse.error(e));
        }
    }

    //맨앞에 WAV 헤더 붙히기
    private byte[] addWavHeader(byte[] pcmData) {

        int byteRate = 16000 * 16 / 8;
        int dataSize = pcmData.length;

        byte[] header = new byte[44];
        // ChunkID "RIFF"
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        // ChunkSize
        int chunkSize = 36 + dataSize;
        header[4] = (byte) (chunkSize & 0xff);
        header[5] = (byte) ((chunkSize >> 8) & 0xff);
        header[6] = (byte) ((chunkSize >> 16) & 0xff);
        header[7] = (byte) ((chunkSize >> 24) & 0xff);
        // Format "WAVE"
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        // Subchunk1ID "fmt "
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        // Subchunk1Size (16 for PCM)
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        // AudioFormat (1 for PCM)
        header[20] = 1;
        header[21] = 0;
        // NumChannels
        header[22] = (byte) 1;
        header[23] = 0;
        // SampleRate
        header[24] = (byte) (16000 & 0xff);
        header[25] = (byte) ((16000 >> 8) & 0xff);
        header[26] = (byte) ((16000 >> 16) & 0xff);
        header[27] = (byte) ((16000 >> 24) & 0xff);
        // ByteRate
        header[28] = (byte) (0);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) (0);
        header[31] = (byte) (0);
        // BlockAlign
        int blockAlign = 2;
        header[32] = (byte) (blockAlign & 0xff);
        header[33] = (byte) (0);
        // BitsPerSample
        header[34] = (byte) 16;
        header[35] = 0;
        // Subchunk2ID "data"
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        // Subchunk2Size
        header[40] = (byte) (dataSize & 0xff);
        header[41] = (byte) ((dataSize >> 8) & 0xff);
        header[42] = (byte) ((dataSize >> 16) & 0xff);
        header[43] = (byte) ((dataSize >> 24) & 0xff);

        // 합치기
        byte[] wavData = new byte[header.length + pcmData.length];
        System.arraycopy(header, 0, wavData, 0, header.length);
        System.arraycopy(pcmData, 0, wavData, header.length, pcmData.length);

        return wavData;
    }

    private String getSessionId(WebSocketSession session) {

        String uri = Objects.requireNonNull(session.getUri()).toString();

        return UriComponentsBuilder.fromUriString(uri)
                .build()
                .getQueryParams()
                .getFirst("sessionId");
    }

    private <T> void sendResponse(WebSocketSession session, ApiResponse<T> response) throws IOException {

        String json = objectMapper.writeValueAsString(response);
        session.sendMessage(new TextMessage(json));
    }
}
