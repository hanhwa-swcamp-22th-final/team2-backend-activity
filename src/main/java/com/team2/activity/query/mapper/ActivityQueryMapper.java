package com.team2.activity.query.mapper;

import com.team2.activity.entity.Activity;
import com.team2.activity.entity.enums.ActivityType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

// 활동 조회용 SQL을 연결하는 MyBatis mapper다.
@Mapper
public interface ActivityQueryMapper {

    // 활동 ID로 단건을 조회한다.
    Activity findById(Long activityId);

    // 전체 활동 목록을 조회한다.
    List<Activity> findAll();

    // 거래처 ID로 활동 목록을 조회한다.
    List<Activity> findByClientId(Long clientId);

    // 활동 타입으로 활동 목록을 조회한다.
    List<Activity> findByActivityType(ActivityType activityType);

    // 날짜 범위로 활동 목록을 조회한다.
    List<Activity> findByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // 작성자 ID로 활동 목록을 조회한다.
    List<Activity> findByAuthorId(Long authorId);

    // 거래처 ID와 활동 타입을 함께 사용해 활동 목록을 조회한다.
    List<Activity> findByClientIdAndActivityType(@Param("clientId") Long clientId,
                                                  @Param("activityType") ActivityType activityType);
}
