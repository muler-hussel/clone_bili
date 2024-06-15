package Dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Mapper
@Repository
public interface UserCoinDao {
    Integer getUserCoinsAmount(Long userId);

    void updateUserCoinsAmount(@Param("userId") Long userId, @Param("amount") Integer amount, @Param("updateTime") Date updateTime);
}
