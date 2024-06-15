package Service.Util;

import com.github.tobato.fastdfs.domain.fdfs.FileInfo;
import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import domain.Exception.ConditionException;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

import static domain.Constant.Constants.*;

@Component
public class FDFSUtil {

    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    @Autowired
    private AppendFileStorageClient appendFileStorageClient;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;



    public String getFileType(MultipartFile file) {
        if (file == null) {
            throw new ConditionException("Illegal file");
        }
        String filename = file.getOriginalFilename();
        int index = filename.lastIndexOf(".");
        return filename.substring(index + 1);
    }
    //上传文件
    public String uploadCommonFile(MultipartFile file) throws IOException {
        Set<MetaData> metaDataSet = new HashSet<>();
        String fileType = this.getFileType(file);
        StorePath path = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), fileType, metaDataSet);
        return path.getPath();
    }

    //断点续传
    public String uploadAppendFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        String fileType = this.getFileType(file);
        StorePath storePath = appendFileStorageClient.uploadAppenderFile(DEFAULT_GROUP, file.getInputStream(), file.getSize(), fileType);
        return storePath.getPath();
    }

    //续传文件添加
    public void modifyAppendFile(MultipartFile file, String filePath, long offset) throws IOException {
        appendFileStorageClient.modifyFile(DEFAULT_GROUP, filePath, file.getInputStream(), file.getSize(), offset);
    }

    public String uploadFileBySlices(MultipartFile file, String fileMd5, Integer sliceNo, Integer totalSliceNo) throws IOException {
        if (file == null || sliceNo == null || totalSliceNo == null) {
            throw new ConditionException("Parameter abnormal");
        }
        String pathKey = PATH_KEY + fileMd5;
        String uploadedSizeKey = UPLOADED_SIZE_KEY + fileMd5;
        String uploadedNoKey = UPLOADED_NO_KEY;
        String uploadedSizeStr = redisTemplate.opsForValue().get(uploadedSizeKey);
        Long uploadedSize = 0L;
        //获取当前文件传到多少
        if (!StringUtil.isNullOrEmpty(uploadedSizeStr)) {
            uploadedSize = Long.valueOf(uploadedSizeStr);
        }
        String fileType = this.getFileType(file);

        if (sliceNo == 1) { //第一个分片
            String path = this.uploadAppendFile(file);
            if (StringUtil.isNullOrEmpty(path)) {
                throw new ConditionException("Uploading fails");
            }
            redisTemplate.opsForValue().set(pathKey, path); //存储上传路径
            redisTemplate.opsForValue().set(uploadedNoKey, "1"); //记录传到哪一片
        } else {
            String filePath = redisTemplate.opsForValue().get(pathKey);
            if (StringUtil.isNullOrEmpty(filePath)) {
                throw new ConditionException("Uploading fails");
            }
            this.modifyAppendFile(file, filePath, uploadedSize);
            redisTemplate.opsForValue().increment(uploadedNoKey);
        }
        uploadedSize += file.getSize();
        redisTemplate.opsForValue().set(uploadedSizeKey, String.valueOf(uploadedSize)); //更新保存已上传大小。valueof避免空指针异常

        //判断是否已经上传完整个文件，清空redis中的内容
        String uploadedNoStr = redisTemplate.opsForValue().get(uploadedNoKey);
        Integer uploadedNo = Integer.valueOf(uploadedNoStr);
        String resultPath = "";
        if (uploadedNo.equals(totalSliceNo)) {
            resultPath = redisTemplate.opsForValue().get(pathKey);
            List<String> keyList = Arrays.asList(uploadedNoKey, pathKey, uploadedSizeKey);
            redisTemplate.delete(keyList);
        }
        return resultPath;
    }

    //分片
    public void convertFileToSlices(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        String fileType = this.getFileType(file);
        File newFile = this.multipartFileToJson(file);
        long fileLength = newFile.length();
        int count = 1;
        for (int i = 0; i < fileLength; i += SLICE_SIZE) {
            RandomAccessFile randomAccessFile = new RandomAccessFile(newFile, "r"); //支持随机访问。可以直接跳到文件任意地方读取
            randomAccessFile.seek(i); //定位到分片的起始位置
            byte[] bytes = new byte[SLICE_SIZE];
            int len = randomAccessFile.read(bytes); //最后一个分片可能不是SLICE_SIZE
            String path = "/Users/tmpfile/" + count + "." + fileType;
            File slice = new File(path);
            FileOutputStream fos = new FileOutputStream(slice);
            fos.write(bytes, 0, len);
            fos.close();
            randomAccessFile.close();
            count++;
        }
        newFile.delete();
    }

    public File multipartFileToJson(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String[] fileName = originalFilename.split("\\.");
        File tempFile = File.createTempFile(fileName[0], "." + fileName[1]);
        file.transferTo(tempFile);
        return tempFile;
    }

    //删除文件
    public void deleteFile(String path)  {
        fastFileStorageClient.deleteFile(path);
    }

    @Value("${fdfs.http.storage-addr}")
    private String httpFdfsStorageAddr;

    public void viewVideoOnlineBySlices(HttpServletRequest request,
                                        HttpServletResponse response,
                                        String path) throws Exception {
        FileInfo fileInfo = fastFileStorageClient.queryFileInfo(DEFAULT_GROUP, path);
        long totalFileSize = fileInfo.getFileSize();
        String url = httpFdfsStorageAddr + path;
        //设置请求头
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, Object> headers = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            headers.put(header, request.getHeader(header));
        }
        //响应参数设置
        String[] pathSplit = path.split(",");
        String rangeStr = request.getHeader("Range");
        String[] range;
        if (StringUtil.isNullOrEmpty(rangeStr)) {
            rangeStr = "bytes=0-" + (totalFileSize - 1);
        }
        range = rangeStr.split("bytes=|-");
        long begin = 0;
        if (range.length >= 2) {
            begin = Long.parseLong(range[1]);
        }
        long end = totalFileSize - 1;
        if (range.length >= 3) {
            end = Long.parseLong(range[2]);
        }
        long len = end - begin + 1;
        String contentRange = "bytes " + begin + "-" + end + "/" + totalFileSize;
        response.setHeader("Content-Range", contentRange);
        response.setHeader("Accept-Ranges", "bytes");
        response.setContentLength((int) len);
        response.setHeader("Content-Type", pathSplit[1]);
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        //获取文件
        HttpUtil.get(url,headers, response);
    }

}
