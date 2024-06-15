package Dao;

import domain.File;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;


@Mapper
@Repository
public interface FileDao {
    File getFileByMd5(String fileMd5);
    Integer addFile(File file);
}
