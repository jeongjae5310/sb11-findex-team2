package com.sprint.mission.findex.domain.syncjob.repository;

import com.sprint.mission.findex.domain.syncjob.dto.CursorPageResponseSyncJobDto;
import com.sprint.mission.findex.domain.syncjob.entity.JobType;
import com.sprint.mission.findex.domain.syncjob.entity.SyncJob;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SyncJobRepository extends JpaRepository<SyncJob, UUID> {

  @Query("SELECT MAX(s.targetDate) FROM SyncJob s " +
      "WHERE s.indexInfo.id = :indexId " +
      "AND s.jobType = :jobType " +
      "AND s.result = com.sprint.mission.findex.domain.syncjob.entity.JobResult.SUCCESS")
  Optional<LocalDate> findLastSuccessDate(@Param("indexId") UUID indexId, @Param("jobType") JobType jobType);

  @Query("SELECT s FROM SyncJob s " +
      "LEFT JOIN FETCH s.indexInfo " +
      "WHERE (:#{#condition.jobType} IS NULL OR s.jobType = :#{#condition.jobType}) " +
      "AND (:#{#condition.indexInfoId} IS NULL OR s.indexInfo.id = :#{#condition.indexInfoId}) " +
      "AND (:#{#condition.result} IS NULL OR s.result = :#{#condition.result}) " +
      "AND (:#{#condition.startDate} IS NULL OR s.targetDate >= :#{#condition.startDate}) " +
      "AND (:#{#condition.endDate} IS NULL OR s.targetDate <= :#{#condition.endDate}) " +
      "AND (:#{#condition.worker} IS NULL OR s.worker LIKE %:#{#condition.worker}%) " +
      "AND (:lastJobTime IS NULL OR s.jobTime < :lastJobTime " +
      "OR (s.jobTime = :lastJobTime AND s.id < :lastId)) " +
      "ORDER BY s.jobTime DESC, s.id DESC")
  List<SyncJob> searchSyncJobs(
      @Param("condition") CursorPageResponseSyncJobDto condition,
      @Param("lastJobTime") Instant lastJobTime,
      @Param("lastId") UUID lastId,
      Pageable pageable
  );
}


