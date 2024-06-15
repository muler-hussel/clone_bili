package Dao;

import domain.UserMoment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UserMomentsDao {
    void addUserMoments(UserMoment userMoment);
}
