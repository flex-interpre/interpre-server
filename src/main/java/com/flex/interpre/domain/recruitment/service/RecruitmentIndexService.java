package com.flex.interpre.domain.recruitment.opensearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flex.interpre.domain.recruitment.index.RecruitmentDocumentIndex;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.knn.KnnQuery;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecruitmentIndexService {

    private final OpenSearchClient client;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final String INDEX_NAME = "recruitments_index";

    // 인덱스 등록
    public void indexRecruitment(RecruitmentDocumentIndex doc) throws IOException {
        IndexResponse response = client.index(i -> i
                .index(INDEX_NAME)
                .id(doc.getId().toString())
                .document(doc)
        );
        System.out.println("Indexed document ID: " + response.id());
    }

    // 벡터 기반 검색
    public List<Map<String, Object>> searchByVector(List<Double> queryVector, int k) throws IOException {
        SearchResponse<Map> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .knn(k -> k
                                .field("embedding")
                                .queryVector(queryVector)
                                .k(k)
                        ),
                Map.class
        );

        return response.hits().hits().stream()
                .map(hit -> hit.source())
                .toList();
    }
}
