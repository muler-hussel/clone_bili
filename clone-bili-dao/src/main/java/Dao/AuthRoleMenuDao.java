package Dao;

import domain.Auth.AuthRoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Mapper
@Repository

public interface AuthRoleMenuDao {
    List<AuthRoleMenu> authRoleMenuByRoleIds(@Param("roleIdSet") Set<Long> roleIdSet);
}
