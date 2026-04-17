package com.sprint.mission.findex.domain.syncjob.repository;

import com.sprint.mission.findex.domain.syncjob.dto.SyncJobSearchCondition;
import com.sprint.mission.findex.domain.syncjob.entity.SyncJob;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface SyncJobCustomRepository {
  List<SyncJob> searchSyncJobs(SyncJobSearchCondition condition, UUID cursor, Pageable pageable);
}