package com.flex.interpre.domain.recruitment.service;

import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.recruitment.index.RecruitmentDocumentIndex;
import com.flex.interpre.global.module.embedding.ClovaEmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch._types.query_dsl.KnnQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitmentIndexService {

    private final OpenSearchClient client;
    private final ClovaEmbeddingService clovaEmbeddingService;

    private static final String INDEX_NAME = "recruitments";

    // 공고문 인덱싱
    public void indexRecruitment(Recruitment recruitment) throws IOException {
        String textToEmbed = buildFullText(recruitment); // 공고문 전체 텍스트 구성
        List<Double> embedding = clovaEmbeddingService.embed(textToEmbed); // 임베딩 생성

        // 인덱스 문서 구성
        RecruitmentDocumentIndex doc = RecruitmentDocumentIndex.builder()
                .id(recruitment.getId())
                .title(recruitment.getTitle())
                .description(recruitment.getDescription())
                .companyName(recruitment.getCompany().getCompanyName())
                .location(recruitment.getLocation())
                .jobAreas(recruitment.getJobAreas().stream().map(Enum::name).toList())
                .jobFirsts(recruitment.getJobFirsts().stream().map(Enum::name).toList())
                .jobSeconds(recruitment.getJobSeconds().stream().map(Enum::name).toList())
                .jobThirds(recruitment.getJobThirds().stream().map(Enum::name).toList())
                .employmentTypes(recruitment.getEmploymentTypes().stream().map(Enum::name).toList())
                .requirements(recruitment.getRequirements() != null ? recruitment.getRequirements().stream().toList() : List.of())
                .benefits(recruitment.getBenefits() != null ? recruitment.getBenefits().stream().toList() : List.of())
                .skills(recruitment.getSkills() != null ? recruitment.getSkills().stream().toList() : List.of())
                .embedding(embedding)
                .build();

        // OpenSearch 인덱싱
        IndexResponse response = client.index(i -> i
                .index(INDEX_NAME)
                .id(doc.getId().toString())
                .document(doc)
        );

        log.info("Indexed recruitment [{}] → docId={}", recruitment.getId(), response.id());
    }

    // 인덱스 문서 삭제
    public void deleteRecruitment(UUID recruitmentId) {

        try {
            client.delete(d -> d.index(INDEX_NAME).id(recruitmentId.toString()));
            log.info(" 인덱스 문서 삭제 [{}]", recruitmentId);
        } catch (Exception e) {
            log.error("인덱스 문서 삭제 실패 [{}]: {}", recruitmentId, e.getMessage());
        }
    }

    // KNN 기반 벡터 검색
    public List<Map<String, Object>> searchByVector(List<Double> queryVector, int k) throws IOException {
        float[] vectorArray = new float[queryVector.size()];
        for (int i = 0; i < queryVector.size(); i++) {
            vectorArray[i] = queryVector.get(i).floatValue();
        }

        KnnQuery knnQuery = new KnnQuery.Builder()
                .field("embedding")
                .vector(vectorArray)
                .k(k)
                .build();

        Query query = new Query.Builder().knn(knnQuery).build();

        SearchResponse<Map<String, Object>> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .size(k)
                        .query(query),
                (Class<Map<String, Object>>) (Class<?>) Map.class
        );

        return response.hits().hits().stream()
                .map(Hit::source)
                .toList();
    }

    // 공고문 전체 텍스트 생성 (임베딩용)
    private String buildFullText(Recruitment recruitment) {
        return String.join(" ",
                recruitment.getTitle(),
                recruitment.getDescription(),
                recruitment.getLocation() != null ? recruitment.getLocation() : "",
                String.join(" ", recruitment.getJobAreas().stream().map(Enum::name).toList()),
                String.join(" ", recruitment.getJobFirsts().stream().map(Enum::name).toList()),
                String.join(" ", recruitment.getJobSeconds().stream().map(Enum::name).toList()),
                String.join(" ", recruitment.getJobThirds().stream().map(Enum::name).toList()),
                String.join(" ", recruitment.getEmploymentTypes().stream().map(Enum::name).toList()),
                recruitment.getRequirements() != null ? String.join(" ", recruitment.getRequirements()) : "",
                recruitment.getBenefits() != null ? String.join(" ", recruitment.getBenefits()) : "",
                recruitment.getSkills() != null ? String.join(" ", recruitment.getSkills()) : ""
        );
    }
}
