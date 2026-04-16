package com.sprint.mission.findex.domain.syncjob.service;

import com.sprint.mission.findex.domain.syncjob.dto.SyncJobDto;
import com.sprint.mission.findex.domain.syncjob.dto.CursorPageResponseSyncJobDto;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SyncJobService {

  void syncIndexData(List<UUID> indexInfoIds, LocalDate baseDateFrom, LocalDate baseDateTo, String workerIp);
  List<SyncJobDto> getSyncJobHistory(
      CursorPageResponseSyncJobDto condition,
      UUID idAfter,
      String cursor,
      String sortField,
      String sortDirection,
      int size);
}