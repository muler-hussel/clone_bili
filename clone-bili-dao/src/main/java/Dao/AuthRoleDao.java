package Dao;

import domain.Auth.AuthRole;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface AuthRoleDao {
    AuthRole getRoleByCode(String code);
}
