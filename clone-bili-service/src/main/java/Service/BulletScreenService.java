package Service;

import Dao.BulletScreenDao;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import domain.BulletScreen;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static domain.Constant.Constants.BULLET_SCREEN_KEY;

@Service
public class BulletScreenService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private BulletScreenDao bulletScreenDao;

    public void addBulletScreenToRedis(BulletScreen bulletScreen) {
        String key = "bullet-screen-video-" + bulletScreen.getVideo();
        String value = redisTemplate.opsForValue().get(key);
        List<BulletScreen> list = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(value)) {
            list = JSON.parseArray(value, BulletScreen.class);
        }
        list.add(bulletScreen);
        redisTemplate.opsForValue().set(key, JSONObject.toJSONString(list));
    }

    public void addBulletScreen(BulletScreen bulletScreen) {
        bulletScreenDao.addBulletScreen(bulletScreen);
    }

    @Async
    public void asyncAddBulletScreen(BulletScreen bulletScreen) {
        bulletScreenDao.addBulletScreen(bulletScreen);
    }

    //优先查询redis，没有再查数据库，并将结果保存到redis中
    public List<BulletScreen> getBulletScreen(Long videoId, String startTime, String endTime) throws ParseException {
        String key = BULLET_SCREEN_KEY + videoId;
        String value = redisTemplate.opsForValue().get(key);
        List<BulletScreen> list;
        if (!StringUtil.isNullOrEmpty(value)) {
            list = JSON.parseArray(value, BulletScreen.class);
            if (!StringUtil.isNullOrEmpty(startTime) && !StringUtil.isNullOrEmpty(endTime)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date startDate = sdf.parse(startTime);
                Date endDate = sdf.parse(endTime);
                List<BulletScreen> childList = new ArrayList<>();
                for (BulletScreen bulletScreen : list) {
                    Date createTime = bulletScreen.getCreateTime();
                    if (createTime.after(startDate) && createTime.before(endDate)) {
                        childList.add(bulletScreen);
                    }
                }
                list = childList;
            }
        } else { //数据库查询
            Map<String, Object> params = new HashMap<>();
            params.put("videoId", videoId);
            params.put("startTime", startTime);
            params.put("endTime", endTime);
            list = bulletScreenDao.getBulletScreen(params);
            //保存到redis
            redisTemplate.opsForValue().set(key, JSONObject.toJSONString(list));
        }
        return list;
    }
}
