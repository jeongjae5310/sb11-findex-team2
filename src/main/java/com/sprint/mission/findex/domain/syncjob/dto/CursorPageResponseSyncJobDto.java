package com.sprint.mission.findex.domain.syncjob.dto;

import com.sprint.mission.findex.domain.syncjob.entity.JobResult;
import com.sprint.mission.findex.domain.syncjob.entity.JobType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// 연동 이력 검색 필터용
public record CursorPageResponseSyncJobDto(
    JobType jobType,
    UUID indexInfoId,
    LocalDate baseDateFrom,
    LocalDate baseDateTo,
    String worker,
    Instant jobTimeFrom,
    Instant jobTimeTo,
    JobResult status
) {
  public CursorPageResponseSyncJobDto {
    if (baseDateFrom != null && baseDateTo != null && baseDateFrom.isAfter(baseDateTo)) {
      throw new IllegalArgumentException("대상 날짜(부터)는 대상 날짜(까지)보다 미래일 수 없습니다.");
    }

    if (jobTimeFrom != null && jobTimeTo != null && jobTimeFrom.isAfter(jobTimeTo)) {
      throw new IllegalArgumentException("작업 일시(부터)는 작업 일시(까지)보다 미래일 수 없습니다.");
    }
  }
}