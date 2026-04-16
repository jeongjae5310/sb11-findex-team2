package com.sprint.mission.findex.domain.syncjob.dto;

import com.sprint.mission.findex.domain.syncjob.entity.JobResult;
import com.sprint.mission.findex.domain.syncjob.entity.JobType;
import com.sprint.mission.findex.domain.syncjob.entity.SyncJob;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// 연동 이력 응답용
public record SyncJobResponse(
    UUID id,
    JobType jobType,
    String indexName,
    String indexClassification,
    LocalDate targetDate,
    String worker,
    Instant jobTime,
    JobResult result,
    String errorMessage
) {
  public static SyncJobResponse from(SyncJob syncJob) {
    return new SyncJobResponse(
        syncJob.getId(),
        syncJob.getJobType(),
        syncJob.getIndexInfo().getIndexName(),
        syncJob.getIndexInfo().getIndexClassification(),
        syncJob.getTargetDate(),
        syncJob.getWorker(),
        syncJob.getJobTime(),
        syncJob.getResult(),
        syncJob.getErrorMessage()
    );
  }
}