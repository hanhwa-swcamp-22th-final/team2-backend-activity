package com.team2.activity.query.mapper;

import com.team2.activity.command.domain.entity.ActivityPackage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ActivityPackageQueryMapper {

    ActivityPackage findActivityPackageById(Long packageId);

    List<ActivityPackage> findAllActivityPackages();

    List<ActivityPackage> findAllActivityPackagesByCreatorId(Long creatorId);

    List<ActivityPackage> findAllActivityPackagesByViewerUserId(@Param("userId") Long userId,
                                                 @Param("creatorId") Long creatorId,
                                                 @Param("poId") String poId);

    List<ActivityPackage> findAllActivityPackagesWithFilters(@Param("creatorId") Long creatorId,
                                              @Param("poId") String poId);

    List<ActivityPackage> findActivityPackagesWithFilters(@Param("creatorId") Long creatorId,
                                                          @Param("poId") String poId,
                                                          @Param("limit") int limit,
                                                          @Param("offset") int offset);

    long countActivityPackagesWithFilters(@Param("creatorId") Long creatorId,
                                          @Param("poId") String poId);
}
