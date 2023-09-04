package com.antares.common.utils;

import cn.hutool.json.JSONUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CrawlerUtils {
    public static List<String> fetchPicturesByKeyword(String keyword, int pageNum, String size) throws IOException {
        String url = "https://cn.bing.com/images/search?q=" + keyword + "&qft=+filterui:license-L2_L3_L4_L5_L6_L7&form=IRFLTR&first=" + pageNum;
        if(StringUtils.isNotBlank(size)){
            switch (size){
                case "medium" : url += "&qft=+filterui:imagesize-medium";break;
            }
        }

        Document doc = Jsoup.connect(url).cookie("MUID", "1E4A7086A77C607000026384A37C61A1").get();
        Elements elements = doc.select(".iuscp.isv");
        List<String> pictures = new ArrayList<>();
        for (Element element : elements) {
            // 取图片地址（murl）
            String m = element.select(".iusc").get(0).attr("m");
            Map<String, Object> map = JSONUtil.toBean(m, Map.class);
            String murl = (String) map.get("murl");
            if(murl.length()  < 256 && !murl.contains("csdn")){

                pictures.add(murl);
            }
        }
        return pictures;
    }

    @Data
    public class Picture implements Serializable {
        private String title;
        private String url;
        private static final long serialVersionUID = 1L;
    }
}
