package com.sprint.mission.findex.domain.syncjob.service;

import com.sprint.mission.findex.domain.syncjob.dto.SyncJobResponse;
import com.sprint.mission.findex.domain.syncjob.dto.SyncJobSearchCondition;
import com.sprint.mission.findex.global.common.dto.CursorPageResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SyncJobService {

  void syncIndexData(List<UUID> indexInfoIds, LocalDate baseDateFrom, LocalDate baseDateTo, String workerIp);
  CursorPageResponse<SyncJobResponse> getSyncJobHistory(
      SyncJobSearchCondition condition,
      String cursor,
      UUID idAfter,
      String sortField,
      String sortDirection,
      int size);
}