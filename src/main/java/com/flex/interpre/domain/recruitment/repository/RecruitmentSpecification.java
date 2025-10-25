package com.flex.interpre.domain.recruitment.repository;

import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.global.constant.Area;
import com.flex.interpre.global.constant.EmploymentType;
import com.flex.interpre.global.constant.JobFirst;
import com.flex.interpre.global.constant.JobSecond;
import com.flex.interpre.global.constant.JobThird;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.*;

public class RecruitmentSpecification {

    public static Specification<Recruitment> filterByConditions(
            Set<Area> jobAreas,
            Set<EmploymentType> employmentTypes,
            Integer minExperience,
            Integer maxExperience,
            Set<JobFirst> jobFirsts,
            Set<JobSecond> jobSeconds,
            Set<JobThird> jobThirds
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (jobAreas != null && !jobAreas.isEmpty())
                predicates.add(root.join("jobAreas").in(jobAreas));

            if (employmentTypes != null && !employmentTypes.isEmpty())
                predicates.add(root.join("employmentTypes").in(employmentTypes));

            if (minExperience != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("minExperience"), minExperience));

            if (maxExperience != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("maxExperience"), maxExperience));

            if (jobFirsts != null && !jobFirsts.isEmpty())
                predicates.add(root.join("jobFirsts").in(jobFirsts));

            if (jobSeconds != null && !jobSeconds.isEmpty())
                predicates.add(root.join("jobSeconds").in(jobSeconds));

            if (jobThirds != null && !jobThirds.isEmpty())
                predicates.add(root.join("jobThirds").in(jobThirds));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
