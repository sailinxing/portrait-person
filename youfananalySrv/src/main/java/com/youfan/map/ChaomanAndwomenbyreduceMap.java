package com.youfan.map;

import com.alibaba.fastjson.JSONObject;
import com.youfan.entity.ChaomanAndWomenInfo;
import com.youfan.kafka.KafkaEvent;
import com.youfan.log.ScanProductLog;
import com.youfan.util.HbaseUtils;
import com.youfan.utils.ReadProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

import java.util.*;

/**
 * Created by li on 2019/1/6.
 */
public class ChaomanAndwomenbyreduceMap implements FlatMapFunction<ChaomanAndWomenInfo, ChaomanAndWomenInfo> {

    @Override
    public void flatMap(ChaomanAndWomenInfo chaomanAndWomenInfo, Collector<ChaomanAndWomenInfo> collector) throws Exception {
        Map<String, Long> resultMap = new HashMap<>();
        String rowkey = "-1";
        if (rowkey.equals("-1")) {
            rowkey = chaomanAndWomenInfo.getUserid() + "";
        }
        String chaotype = chaomanAndWomenInfo.getChaotype();
        Long count = chaomanAndWomenInfo.getCount();
        long pre = resultMap.get(chaotype) == null ? 0l : resultMap.get(chaotype);
        resultMap.put(chaotype, pre + count);

        String tablename = "userflaginfo";

        String famliyname = "userbehavior";
        String colum = "chaomanandwomen";
        String data = HbaseUtils.getdata(tablename, rowkey, famliyname, colum);
        if (StringUtils.isNotBlank(data)) {
            Map<String, Long> datamap = JSONObject.parseObject(data, Map.class);
            Set<String> keys = resultMap.keySet();
            for (String key : keys) {
                Long pre1 = datamap.get(key) == null ? 0l : datamap.get(key);
                resultMap.put(key, pre1 + resultMap.get(key));
            }
        }

        if (!resultMap.isEmpty()) {
            String chaomandanwomenmap = JSONObject.toJSONString(resultMap);
            HbaseUtils.putdata(tablename, rowkey, famliyname, colum, chaomandanwomenmap);
            long chaoman = resultMap.get("1") == null ? 0l : resultMap.get("1");
            long chaowomen = resultMap.get("2") == null ? 0l : resultMap.get("2");
            String flag = "women";
            long finalcount = chaowomen;
            if (chaoman > chaowomen) {
                flag = "man";
                finalcount = chaoman;
            }
            if (finalcount > 2000) {
                colum = "chaotype";

                ChaomanAndWomenInfo chaomanAndWomenInfotemp = new ChaomanAndWomenInfo();
                chaomanAndWomenInfotemp.setChaotype(flag);
                chaomanAndWomenInfotemp.setCount(1l);
                chaomanAndWomenInfotemp.setGroupbyfield(flag + "==chaomanAndWomenInforeduce");
                String type = HbaseUtils.getdata(tablename, rowkey, famliyname, colum);
                if (StringUtils.isNotBlank(type) && !type.equals(flag)) {
                    ChaomanAndWomenInfo chaomanAndWomenInfopre = new ChaomanAndWomenInfo();
                    chaomanAndWomenInfopre.setChaotype(type);
                    chaomanAndWomenInfopre.setCount(-1l);
                    chaomanAndWomenInfopre.setGroupbyfield(type + "==chaomanAndWomenInforeduce");
                    collector.collect(chaomanAndWomenInfopre);
                }

                HbaseUtils.putdata(tablename, rowkey, famliyname, colum, flag);
                collector.collect(chaomanAndWomenInfotemp);
            }

        }
    }

}
