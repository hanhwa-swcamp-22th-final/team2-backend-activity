package com.team2.activity.query.mapper;

import com.team2.activity.entity.ActivityPackage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

// 활동 패키지 조회용 SQL을 연결하는 MyBatis mapper다.
@Mapper
public interface ActivityPackageQueryMapper {

    // 패키지 ID로 단건을 조회한다.
    ActivityPackage findById(Long packageId);

    // 전체 패키지 목록을 조회한다.
    List<ActivityPackage> findAll();

    // 생성자 ID로 패키지 목록을 조회한다.
    List<ActivityPackage> findAllByCreatorId(Long creatorId);
}
