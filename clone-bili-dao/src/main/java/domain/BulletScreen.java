package domain;

import java.util.Date;

public class BulletScreen {
    private Long id;
    private Long userId;
    private Long video;
    private String content;
    private String bulletScreenTime;
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getVideo() {
        return video;
    }

    public void setVideo(Long video) {
        this.video = video;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getBulletScreenTime() {
        return bulletScreenTime;
    }

    public void setBulletScreenTime(String bulletScreenTime) {
        this.bulletScreenTime = bulletScreenTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
