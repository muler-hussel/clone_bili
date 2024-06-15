package Service;

import Dao.FileDao;
import Service.Util.FDFSUtil;
import Service.Util.MD5Util;
import domain.File;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;

@Service
public class FileService {

    @Autowired
    private FDFSUtil fdfsUtil;

    @Autowired
    private FileDao fileDao;

    public String uploadFileBySlices(MultipartFile slice,
                                     String fileMd5,
                                     Integer sliceNo,
                                     Integer totalSliceNo) throws IOException {
        File dbFileMd5 = fileDao.getFileByMd5(fileMd5);
        if (dbFileMd5 != null) { //已经传过，秒传
            return dbFileMd5.getUrl();
        }
        String url = fdfsUtil.uploadFileBySlices(slice, fileMd5, sliceNo, totalSliceNo);
        if (!StringUtil.isNullOrEmpty(url)) { //文件上传成功,存入数据库
            dbFileMd5 = new File();
            dbFileMd5.setCreateTime(new Date());
            dbFileMd5.setMd5(fileMd5);
            dbFileMd5.setUrl(url);
            dbFileMd5.setType(fdfsUtil.getFileType(slice));
            fileDao.addFile(dbFileMd5);
        }
        return url;
    }

    public String getFileMd5(MultipartFile file) throws IOException {
        return MD5Util.getFileMd5(file);
    }
}
