package com.team2.activity.mapper;

import com.team2.activity.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ActivityQueryMapper {

    Activity findById(Long activityId);

    List<Activity> findAll();

    List<Activity> findByClientId(Long clientId);

    List<Activity> findByActivityType(String activityType);

    List<Activity> findByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    List<Activity> findByAuthorId(Long authorId);

    List<Activity> findByClientIdAndActivityType(@Param("clientId") Long clientId,
                                                  @Param("activityType") String activityType);
}
