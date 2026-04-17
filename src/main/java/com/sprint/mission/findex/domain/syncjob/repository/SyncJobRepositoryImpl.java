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
  public List<SyncJob> searchSyncJobs(SyncJobSearchCondition condition, UUID cursorId, Pageable pageable) {

    SyncJob cursorJob = null;
    if (cursorId != null) {
      cursorJob = queryFactory.selectFrom(syncJob).where(syncJob.id.eq(cursorId)).fetchOne();
    }

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
            getCursorCondition(cursorJob, pageable) //
        )
        .orderBy(getOrderSpecifiers(pageable)) //
        .limit(pageable.getPageSize())
        .fetch();
  }

  private BooleanExpression getCursorCondition(SyncJob cursorJob, Pageable pageable) {
    if (cursorJob == null) return null;

    Sort.Order order = pageable.getSort().isSorted() ? pageable.getSort().iterator().next() : Sort.Order.desc("jobTime");
    String property = order.getProperty();
    boolean isAsc = order.isAscending();

    if ("targetDate".equals(property)) {
      if (isAsc) {
        return syncJob.targetDate.gt(cursorJob.getTargetDate())
            .or(syncJob.targetDate.eq(cursorJob.getTargetDate()).and(syncJob.id.gt(cursorJob.getId())));
      } else {
        return syncJob.targetDate.lt(cursorJob.getTargetDate())
            .or(syncJob.targetDate.eq(cursorJob.getTargetDate()).and(syncJob.id.lt(cursorJob.getId())));
      }
    } else {
      if (isAsc) {
        return syncJob.jobTime.gt(cursorJob.getJobTime())
            .or(syncJob.jobTime.eq(cursorJob.getJobTime()).and(syncJob.id.gt(cursorJob.getId())));
      } else {
        return syncJob.jobTime.lt(cursorJob.getJobTime())
            .or(syncJob.jobTime.eq(cursorJob.getJobTime()).and(syncJob.id.lt(cursorJob.getId())));
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