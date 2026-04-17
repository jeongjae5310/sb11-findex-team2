package com.sprint.mission.findex.domain.syncjob.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.findex.domain.syncjob.dto.SyncJobSearchCondition;
import com.sprint.mission.findex.domain.syncjob.entity.JobResult;
import com.sprint.mission.findex.domain.syncjob.entity.JobType;
import com.sprint.mission.findex.domain.syncjob.entity.SyncJob;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.sprint.mission.findex.domain.indexinfo.entity.QIndexInfo.indexInfo;
import static com.sprint.mission.findex.domain.syncjob.entity.QSyncJob.syncJob;

@Repository
@RequiredArgsConstructor
public class SyncJobRepositoryImpl implements SyncJobCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<SyncJob> searchSyncJobs(SyncJobSearchCondition condition, String cursor, UUID idAfter, Pageable pageable) {

    // 🚨 서브쿼리 제거됨! 프론트가 준 값으로 바로 조회 시작
    return queryFactory
        .selectFrom(syncJob)
        .leftJoin(syncJob.indexInfo, indexInfo).fetchJoin()
        .where(
            eqJobType(condition.jobType()),
            eqIndexInfoId(condition.indexInfoId()),
            eqStatus(condition.status()),
            goeBaseDateFrom(condition.baseDateFrom()),
            loeBaseDateTo(condition.baseDateTo()),
            containsWorker(condition.worker()),
            goeJobTimeFrom(condition.jobTimeFrom()),
            loeJobTimeTo(condition.jobTimeTo()),
            getCursorCondition(cursor, idAfter, pageable) // 🚨 변경된 메서드 호출
        )
        .orderBy(getOrderSpecifiers(pageable))
        .limit(pageable.getPageSize())
        .fetch();
  }

  private BooleanExpression getCursorCondition(String cursor, UUID idAfter, Pageable pageable) {
    if (cursor == null || cursor.isBlank() || idAfter == null) return null;

    Sort.Order order = pageable.getSort().isSorted() ? pageable.getSort().iterator().next() : Sort.Order.desc("jobTime");
    String property = order.getProperty();
    boolean isAsc = order.isAscending();

    if ("targetDate".equals(property)) {
      LocalDate targetDateCursor = LocalDate.parse(cursor);
      if (isAsc) {
        return syncJob.targetDate.gt(targetDateCursor)
            .or(syncJob.targetDate.eq(targetDateCursor).and(syncJob.id.gt(idAfter)));
      } else {
        return syncJob.targetDate.lt(targetDateCursor)
            .or(syncJob.targetDate.eq(targetDateCursor).and(syncJob.id.lt(idAfter)));
      }
    } else {
      Instant jobTimeCursor = Instant.parse(cursor);
      if (isAsc) {
        return syncJob.jobTime.gt(jobTimeCursor)
            .or(syncJob.jobTime.eq(jobTimeCursor).and(syncJob.id.gt(idAfter)));
      } else {
        return syncJob.jobTime.lt(jobTimeCursor)
            .or(syncJob.jobTime.eq(jobTimeCursor).and(syncJob.id.lt(idAfter)));
      }
    }
  }

  private OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable) {
    List<OrderSpecifier<?>> orders = new ArrayList<>();
    if (pageable.getSort().isSorted()) {
      for (Sort.Order order : pageable.getSort()) {
        Order direction = order.isAscending() ? Order.ASC : Order.DESC;
        switch (order.getProperty()) {
          case "targetDate" -> orders.add(new OrderSpecifier<>(direction, syncJob.targetDate));
          case "jobTime" -> orders.add(new OrderSpecifier<>(direction, syncJob.jobTime));
        }
      }
    } else {
      orders.add(new OrderSpecifier<>(Order.DESC, syncJob.jobTime));
    }

    Order tieBreaker = orders.isEmpty() || orders.get(0).getOrder() == Order.DESC ? Order.DESC : Order.ASC;
    orders.add(new OrderSpecifier<>(tieBreaker, syncJob.id));

    return orders.toArray(new OrderSpecifier[0]);
  }

  private BooleanExpression eqJobType(JobType jobType) { return jobType != null ? syncJob.jobType.eq(jobType) : null; }
  private BooleanExpression eqIndexInfoId(UUID indexInfoId) { return indexInfoId != null ? syncJob.indexInfo.id.eq(indexInfoId) : null; }
  private BooleanExpression eqStatus(JobResult status) { return status != null ? syncJob.result.eq(status) : null; }
  private BooleanExpression goeBaseDateFrom(LocalDate baseDateFrom) { return baseDateFrom != null ? syncJob.targetDate.goe(baseDateFrom) : null; }
  private BooleanExpression loeBaseDateTo(LocalDate baseDateTo) { return baseDateTo != null ? syncJob.targetDate.loe(baseDateTo) : null; }
  private BooleanExpression containsWorker(String worker) { return worker != null && !worker.isBlank() ? syncJob.worker.contains(worker) : null; }
  private BooleanExpression goeJobTimeFrom(Instant jobTimeFrom) { return jobTimeFrom != null ? syncJob.jobTime.goe(jobTimeFrom) : null; }
  private BooleanExpression loeJobTimeTo(Instant jobTimeTo) { return jobTimeTo != null ? syncJob.jobTime.loe(jobTimeTo) : null; }
}