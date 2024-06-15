package Dao;

import domain.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface VideoDao {
    Integer addVideos(Video video);
    Integer batchAddVideoTags(List<VideoTag> videoTagList);

    Integer pageCountVideos(Map<String, Object> params);

    List<Video> pageListVideos(Map<String, Object> params);

    Video getVideoById(Long videoId);

    VideoLike getVideoLikeByVideoIdAndUserId(@Param("videoId") Long videoId, @Param("userId") Long userId);

    void addVideoLike(VideoLike videoLike);

    void deleteVideoLike(@Param("videoId") Long videoId, @Param("userId") Long userId);

    Long getVideoLikes(Long videoId);

    void deleteVideoCollection( @Param("videoId") Long videoId,  @Param("userId") Long userId);

    void addVideoCollection(VideoCollection videoCollection);

    Long getVideoCollections(Long videoId);

    VideoCollection getVideoCollectionByVideoIdAndUserId(@Param("videoId")Long videoId,  @Param("userId") Long userId);

    VideoCoin getVideoCoinByVideoIdAndUserId(@Param("videoId") Long videoId, @Param("userId") Long userId);

    void addVideoCoin(VideoCoin videoCoin);

    void updateVideoCoin(VideoCoin videoCoin);

    Long getVideoCoinsAmount(Long videoId);

    void addVideoComment(VideoComment videoComment);

    List<VideoComment> pageListVideoComment(Map<String, Object> params);

    Integer pageCountVideoComment(Map<String, Object> params);

    List<VideoComment> batchGetVideoCommentsByRootId(@Param("rootIdList") List<Long> parentIdList);

    Video getVideoDetails(Long videoId);

    VideoView getVideoView(Map<String, Object> params);

    void addVideoView(VideoView videoView);

    Integer getVideoViewCounts(Long videoId);

    List<UserPreference> getAllUserPreference();

    List<Video> batchGetVideosByIds(@Param("idList") List<Long> idList);

    List<VideoTag> getVideoTagsByVideoId(Long videoId);

    void deleteVideoTags(@Param("tagIdList") List<Long> tagIdList, @Param("videoId") Long videoId);
}
