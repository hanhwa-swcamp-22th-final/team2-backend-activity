package com.team2.activity.query.mapper;

import com.team2.activity.entity.ActivityPackage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ActivityPackageQueryMapper {

    ActivityPackage findById(Long packageId);

    List<ActivityPackage> findAll();

    List<ActivityPackage> findAllByCreatorId(Long creatorId);
}
