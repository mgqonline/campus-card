package com.campus.card.admin.repository;

import com.campus.card.admin.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    // 预先拉取角色、权限与菜单，避免懒加载异常
    @Query("select distinct u from User u \n            left join fetch u.roles r \n            left join fetch r.permissions p \n            left join fetch r.menus m \n            where u.id = :id")
    Optional<User> findByIdWithRolesPermsMenus(@Param("id") Long id);
}