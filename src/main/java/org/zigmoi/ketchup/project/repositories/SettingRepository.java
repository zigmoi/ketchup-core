package org.zigmoi.ketchup.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.zigmoi.ketchup.project.entities.SettingId;
import org.zigmoi.ketchup.project.entities.Setting;

import java.util.List;

public interface SettingRepository extends JpaRepository<Setting, SettingId> {
    @Query("select s from Setting s where s.id.projectResourceId = :projectResourceId and s.type= :type")
    List<Setting> findAllByProjectResourceIdAndType(String projectResourceId, String type);

    @Query("select count(s) from Setting s where s.id.projectResourceId = :projectResourceId and s.type= :type")
    long countAllByProjectResourceIdAndType(String projectResourceId, String type);
}
