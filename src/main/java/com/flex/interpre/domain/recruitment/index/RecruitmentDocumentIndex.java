package com.flex.interpre.domain.recruitment.index;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.opensearch.annotations.Document;
import org.springframework.data.opensearch.annotations.Field;
import org.springframework.data.opensearch.annotations.FieldType;

@Document(indexName = "recruitment_embeddings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruitmentDocumentIndex {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String description;

    // Clova 임베딩 벡터
    @Field(type = FieldType.Dense_Vector, dims = 1024)
    private float[] embeddingVector;
}
