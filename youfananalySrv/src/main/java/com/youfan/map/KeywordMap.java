package com.youfan.map;

import com.youfan.entity.KeyWordEntity;
import com.youfan.tfIdf.TfIdfEntity;
import com.youfan.util.HbaseUtils;
import com.youfan.util.IkUtil;
import org.apache.flink.api.common.functions.MapFunction;

import java.util.*;

/**
 * Created by li on 2019/1/20.
 */

/**
 * 一条数据 userid,小米8 全面屏游戏智能手机 6GB+64GB 金色 全网通4G 双卡双待
 */
public class KeywordMap implements MapFunction<String, KeyWordEntity> {

    @Override
    public KeyWordEntity map(String s) throws Exception {
        String[] productwordarray = s.split(",");
        String userid = productwordarray[0];
        String wordarray = productwordarray[1];

        KeyWordEntity keyWordEntity = new KeyWordEntity();
        keyWordEntity.setUserid(userid);
        List<String> words = new ArrayList<>();
        words.add(wordarray);
        keyWordEntity.setOriginalwords(words);
        return keyWordEntity;
    }
}
