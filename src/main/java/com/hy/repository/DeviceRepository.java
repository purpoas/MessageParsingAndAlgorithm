package com.hy.repository;

import com.hy.domain.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device,Long>, JpaSpecificationExecutor<Device> {
    Device findDeviceByCodeAndDeletedFalse(String code);

    Device findDeviceByPoleIdAndNameAndDeletedFalse(Long id,String name);

    Device findDeviceByIdAndDeletedFalse(Long id);

    @Query("select d from device d join d.pole p where p.org.id in :ids and d.deleted=false")
    Page<Device> findDevicesByOrgIdsInAndDeletedFalse(List<Long> ids, Pageable pageable);

    @Query("select d from device d join d.pole p where p.org.id in :ids and d.deleted=false and ( d.code like %:name% or d.name like %:name%)")
    Page<Device> findDevicesByOrgIdsInAndNameLikeAndDeletedFalse(List<Long> ids,String name, Pageable pageable);

    @Query("select d from device d join d.pole p join p.org o where o.id in :ids and d.deleted=false ")
    List<Device> findDevicesByOrgIdListAndDeletedFalse(List<Long> ids);

    List<Device> findDevicesByIdInAndDeletedFalse(List<Long> ids);

    @Query("select d from device d join d.pole p where p.id in :poleIdList and d.deleted=false ")
    List<Device>findDevicesByPoleIdInAndDeletedFalse(List<Long> poleIdList);

    @Query("select d.id from device d join d.pole p where p.id in :poleIdList and d.deleted=false ")
    List<Long> findDeviceIdsByPoleIdInAndDeletedFalse(List<Long> poleIdList);

    /**
     * 根据杆塔id删除设备
     *
     * @param poleId
     * @param deleteById
     * @param deleteTime
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "update device set deleted=true,deleteBy = :deleteById,deletedTime = :deleteTime where pole.id = :poleId")
    void updateDeviceDeletedFalseByPoleId(@Param("poleId") Long poleId, @Param("deleteById")Long deleteById,@Param("deleteTime") Instant deleteTime);

    /**
     * 根据杆塔id数组删除设备
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "update device set deleted=true,deleteBy = :deleteById,deletedTime = :deleteTime where pole.id in :poleIdList")
    void updateDeviceDeletedFalseByPoleIdList(@Param("poleIdList") List<Long> poleIdList, @Param("deleteById") Long deleteById, @Param("deleteTime") Instant deleteTime);

    /**
     * 查询所有未隐藏设备的简洁信息
     **/
    @Query("select new com.hy.domain.Device(d.id,d.name,d.code) from device d where d.deleted = false")
    List<Device> findBriefDeviceByDeletedFalse();

    /**
     * 分页查询指定设备集合设备
     *
     * @param deviceIds 设备Id集合
     * @param pageable  分页对象
     * @return Page<Device>
     */
    Page<Device> findByIdIn(Collection<Long> deviceIds, Pageable pageable);

    Integer countByPoleOrgParentIdAndDeletedFalse(Long orgId);
}
