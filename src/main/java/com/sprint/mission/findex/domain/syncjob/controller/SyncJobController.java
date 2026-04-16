package com.sprint.mission.findex.domain.syncjob.controller;

import com.sprint.mission.findex.domain.syncjob.dto.IndexDataSyncRequest;
import com.sprint.mission.findex.domain.syncjob.dto.SyncJobDto;
import com.sprint.mission.findex.domain.syncjob.dto.CursorPageResponseSyncJobDto;
import com.sprint.mission.findex.domain.syncjob.service.SyncJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Sync Job", description = "지수 데이터 연동 및 이력 관리 API (M4)")
@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class SyncJobController {

  private final SyncJobService syncJobService;


  //지수 데이터 연동 API
  @Operation(summary = "지수 데이터 수동 연동", description = "지정한 기간(baseDateFrom ~ baseDateTo)의 하나 이상의 지수 데이터를 외부 API로부터 연동합니다.")
  @PostMapping("/index-data")
  public ResponseEntity<String> syncIndexData(

      @Valid @RequestBody IndexDataSyncRequest request,
      HttpServletRequest servletRequest) {

    String workerIp = servletRequest.getRemoteAddr();

    syncJobService.syncIndexData(
        request.indexInfoIds(),
        request.baseDateFrom(),
        request.baseDateTo(),
        workerIp
    );
    return ResponseEntity.ok("데이터 연동이 성공적으로 완료되었습니다.");
  }

  // 연동 작업 목록 조회 API
  @Operation(summary = "연동 작업 목록 조회", description = "조건(검색 필터)에 맞는 지수 데이터 연동 이력을 페이징하여 조회합니다.")
  @GetMapping
  public ResponseEntity<List<SyncJobDto>> getSyncJobHistory(
      @ParameterObject @ModelAttribute CursorPageResponseSyncJobDto condition,

      @RequestParam(required = false) UUID idAfter,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "jobTime") String sortField,
      @RequestParam(defaultValue = "desc") String sortDirection,
      @RequestParam(defaultValue = "10") int size) {

    List<SyncJobDto> response = syncJobService.getSyncJobHistory(
        condition, idAfter, cursor, sortField, sortDirection, size
    );

    return ResponseEntity.ok(response);
  }
}