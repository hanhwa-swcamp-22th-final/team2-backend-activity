package com.team2.activity.query.mapper;

import com.team2.activity.command.domain.entity.ActivityPackage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ActivityPackageQueryMapper {

    ActivityPackage findById(Long packageId);

    List<ActivityPackage> findAll();

    List<ActivityPackage> findAllByCreatorId(Long creatorId);

    List<ActivityPackage> findAllByViewerUserId(@Param("userId") Long userId,
                                                 @Param("creatorId") Long creatorId,
                                                 @Param("poId") String poId);
}
