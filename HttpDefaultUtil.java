package com.h3c.vdi.viewscreen.utils.httpclient;

import com.google.gson.Gson;
import com.h3c.vdi.viewscreen.api.model.response.monitoring.ResponseLocation;
import com.h3c.vdi.viewscreen.api.result.ApiResponse;
import com.h3c.vdi.viewscreen.constant.Constant;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;

/**
 * @Date 2020/10/9 17:07
 * @Created by lgw2845
 */

@Log4j2
public class HttpDefaultUtil {

    public static ApiResponse<ResponseLocation> addressLongitude(String address) {
        ApiResponse<ResponseLocation> returnValue = null;

        String url = HttpConstant.ADDRESS_TO_LONGITUDE_URL + "&key=" + HttpConstant.KEY + "&address=" + address;
        log.info("请求url:" + url);


        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)                      //设置连接超时时间，单位毫秒
                .setConnectionRequestTimeout(1000)            //设置从connect Manager获取Connection 超时时间，单位毫秒
                .setSocketTimeout(5000).build();              //请求获取数据的超时时间，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
        HttpGet httpGet = new HttpGet(url);// 创建一个get请求

        try (CloseableHttpClient client = HttpClients
                .custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {

            CloseableHttpResponse chr = client.execute(httpGet);       // 用http连接去执行get请求并且获得http响应
            if (chr.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = chr.getEntity();                       // 从response中取到响实体
                String data = EntityUtils.toString(entity);                     // 把响应实体转成文本
                log.info("返回信息：" + data);

                // JSON转对象
                String json = data.replaceAll("\\[]", Constant.Message.NULL);
                log.info("返回信息：" + json);
                ResponseLocation responseLocation = new Gson().fromJson(json, ResponseLocation.class);
                returnValue = ApiResponse.buildSuccess(responseLocation);
            }
            chr.close();
            return returnValue;
        } catch (IOException e) {
            log.error("地理编码异常", e);
            return null;
        }
    }


    public static String getLocation(String address) {
        if (StringUtils.isBlank(address)) {
            return null;
        }
        ApiResponse<ResponseLocation> apiResponse = addressLongitude(address);
        if (null != apiResponse && null != apiResponse.getData()) {
            List<ResponseLocation.GeocodesBean> geoCodes = apiResponse.getData().getGeocodes();
            if (CollectionUtils.isNotEmpty(geoCodes)) {
                if (null != geoCodes.get(0).getLocation()) return geoCodes.get(0).getLocation();
            }
        }
        return null;
    }
}
