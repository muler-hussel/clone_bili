package Dao;

import domain.BulletScreen;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface BulletScreenDao {
    void addBulletScreen(BulletScreen bulletScreen);

    List<BulletScreen> getBulletScreen(Map<String, Object> params);
}
