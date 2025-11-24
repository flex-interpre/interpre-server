package com.flex.interpre.domain.recruitment.service;

import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.recruitment.index.RecruitmentDocumentIndex;
import com.flex.interpre.global.module.embedding.ClovaEmbeddingService;
import java.io.IOException;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.KnnQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitmentIndexService {
    private final OpenSearchClient client;
    private final ClovaEmbeddingService clovaEmbeddingService;

    private static final String INDEX_NAME = "recruitments"; // 인덱스 문서 이름

    // 공고문 단건 인덱싱
    public void indexRecruitment(Recruitment recruitment) throws IOException {
        String textToEmbed = buildFullText(recruitment); // 공고문 전체 텍스트 구성
        List<Double> embedding = clovaEmbeddingService.embed(textToEmbed);

        // 인덱스 문서 구성
        RecruitmentDocumentIndex doc = RecruitmentDocumentIndex.builder()
                .id(recruitment.getId())
                .company(RecruitmentDocumentIndex.CompanyInfo.builder()
                        .id(recruitment.getCompany().getId())
                        .companyName(recruitment.getCompany().getCompanyName())
                        .build())
                .title(recruitment.getTitle())
                .description(recruitment.getDescription())
                .location(recruitment.getLocation())
                .jobAreas(recruitment.getJobAreas().stream().map(Enum::name).toList())
                .jobFirsts(recruitment.getJobFirsts().stream().map(Enum::name).toList())
                .jobSeconds(recruitment.getJobSeconds().stream().map(Enum::name).toList())
                .jobThirds(recruitment.getJobThirds().stream().map(Enum::name).toList())
                .employmentTypes(recruitment.getEmploymentTypes().stream().map(Enum::name).toList())
                .requirements(recruitment.getRequirements().stream().toList())
                .benefits(recruitment.getBenefits().stream().toList())
                .skills(recruitment.getSkills().stream().toList())
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

    // 공고문 데이터 벌크 인덱싱
    public void bulkIndexRecruitments(List<Recruitment> recruitments) throws Exception {

        final int BULK_SIZE = 100; // t3.small
        List<Recruitment> buffer = new ArrayList<>();
        int indexed = 0;

        for (Recruitment r : recruitments) {
            buffer.add(r);

            if (buffer.size() == BULK_SIZE) {
                sendBulk(buffer); // Bulk 전송
                indexed += buffer.size();
                buffer.clear();
                Thread.sleep(3000); // 노드 보호용
            }
        }

        if (!buffer.isEmpty()) {
            sendBulk(buffer); // 마지막 배치 전송
            indexed += buffer.size();
        }

        log.info("Bulk 인덱싱 전체 완료: {}건", indexed);
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
    public List<Recruitment> searchByVector(List<Double> queryVector, int k) throws IOException {
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

        SearchResponse<Recruitment> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .size(k)
                        .query(query)
                        .source(source -> source
                                .filter(f -> f.excludes("embedding"))  // embedding 필드 제외
                        ),
                Recruitment.class
        );

        return response.hits().hits().stream()
                .map(Hit::source)
                .toList();
    }

    // 키워드로 검색
    @SneakyThrows
    public List<UUID> searchIdsByKeyword(String keyword, String excludeKeyword, Set<String> fields){
        if (keyword == null || keyword.isBlank()) return List.of();

        Set<String> searchFields = (fields == null || fields.isEmpty())
                ? Set.of("title", "description", "companyName", "skills")
                : fields;

        final String exclude = excludeKeyword;

        var boolQuery = new Query.Builder().bool(b -> {
            b.must(m -> m.multiMatch(mm -> mm.fields(fields.stream().toList()).query(keyword)));
            if (excludeKeyword != null && !excludeKeyword.isBlank()) {
                b.mustNot(m -> m.multiMatch(mm -> mm.fields(fields.stream().toList()).query(excludeKeyword)));
            }
            return b;
        }).build();

        SearchResponse<RecruitmentDocumentIndex> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .size(200)
                        .query(boolQuery),
                RecruitmentDocumentIndex.class
        );

        return response.hits().hits().stream()
                .map(hit -> UUID.fromString(hit.source().getId().toString()))
                .toList();
    }

    // 오픈서치에서 공고문 ID-> 해당 공고 임베딩 조회
    public Map<UUID, List<Double>> getEmbeddingsByIds(List<UUID> ids) {
        try {
            // UUID->FieldValue로 변환
            List<FieldValue> fieldValues = ids.stream().map(UUID::toString).map(FieldValue::of).toList();

            SearchResponse<RecruitmentDocumentIndex> response = client.search(s -> s
                            .index(INDEX_NAME)
                            .size(ids.size())
                            .query(q -> q.terms(t -> t
                                    .field("_id")
                                    .terms(tt -> tt.value(fieldValues))
                            ))
                            .source(src -> src.filter(f -> f.includes("embedding"))),
                    RecruitmentDocumentIndex.class
            );

            Map<UUID, List<Double>> result = new HashMap<>();

            response.hits().hits().forEach(hit -> {
                UUID id = UUID.fromString(hit.id());
                List<Double> embedding = hit.source().getEmbedding();
                result.put(id, embedding);
            });

            return result;

        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch embeddings from OpenSearch", e);
        }
    }


    // 내부 메서드 //

    private void sendBulk(List<Recruitment> recruitments) throws Exception {

        List<BulkOperation> ops = new ArrayList<>();

        for (Recruitment recruitment : recruitments) {
            String docId = recruitment.getId().toString();

            // 이미 인덱싱된 문서인지 확인
            boolean exists = client.exists(e -> e.index(INDEX_NAME).id(docId)).value();
            if (exists) {
                log.info("이미 인덱싱됨 → embedding 스킵 ID={}", docId);
                continue;  // embed() 호출 스킺
            }

            List<Double> embedding = clovaEmbeddingService.embed(buildFullText(recruitment));

            RecruitmentDocumentIndex doc = RecruitmentDocumentIndex.builder()
                    .id(recruitment.getId())
                    .company(RecruitmentDocumentIndex.CompanyInfo.builder()
                            .id(recruitment.getCompany().getId())
                            .companyName(recruitment.getCompany().getCompanyName())
                            .build())
                    .title(recruitment.getTitle())
                    .description(recruitment.getDescription())
                    .location(recruitment.getLocation())
                    .jobAreas(recruitment.getJobAreas().stream().map(Enum::name).toList())
                    .jobFirsts(recruitment.getJobFirsts().stream().map(Enum::name).toList())
                    .jobSeconds(recruitment.getJobSeconds().stream().map(Enum::name).toList())
                    .jobThirds(recruitment.getJobThirds().stream().map(Enum::name).toList())
                    .employmentTypes(recruitment.getEmploymentTypes().stream().map(Enum::name).toList())
                    .requirements(recruitment.getRequirements().stream().toList())
                    .benefits(recruitment.getBenefits().stream().toList())
                    .skills(recruitment.getSkills().stream().toList())
                    .embedding(embedding)
                    .build();

            ops.add(
                    BulkOperation.of(o -> o
                            .index(i -> i
                                    .index(INDEX_NAME)
                                    .id(doc.getId().toString())
                                    .document(doc)
                            )
                    )
            );
        }

        BulkRequest request = new BulkRequest.Builder()
                .operations(ops)
                .build();

        BulkResponse response = client.bulk(request);

        // 실패 항목 로깅
        if (response.errors()) {
            response.items().forEach(item -> {
                if (item.error() != null) {
                    log.error("[Bulk Error] id={} reason={}",
                            item.id(),
                            item.error().reason());
                }
            });
        }

        log.info("Bulk {}건 처리 완료", recruitments.size());
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
