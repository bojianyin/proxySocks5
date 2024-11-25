package com.xiaobo.proxysocks5;

import android.util.Pair;

import com.xiaobo.proxysocks5.constants.AppConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class PingUtils {

    public static String executePingAndGetDelay(String host) {
        long startTime = System.currentTimeMillis();
        String command = "ping -c 1 -w 1 " + host; // -c 1 表示只发送1次ping，-w 1 表示超时时间为1秒
        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("time=")) {
                    // 假设输出格式为 "time=xx ms"
                    String delayStr = line.split("time=")[1].split(" ")[0];
                    return delayStr;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // 如果没有获取到延迟或者发生异常，返回-1
    }

    public static Pair<ArrayList<Line>,String> getRemoteLines(){
        String urlString = AppConstant.remoteProxyUrl;
        ArrayList<Line> lines = new ArrayList<>();
        String jsonData = "";
        try {
            // 创建 URL 对象
            URL url = new URL(urlString);

            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 设置请求方法为 GET
            connection.setRequestMethod("GET");

            // 设置连接超时和读取超时
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            // 获取响应码
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) { // 响应码 200
                // 读取响应
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                // 打印响应
                System.out.println("Response: " + response);

                jsonData = response.toString();
                ArrayList<Line> arrayList = parseRemoteLineStr(jsonData);
                lines.addAll(arrayList);


            } else {
                System.out.println("Request failed. Response Code: " + responseCode);
            }

            // 断开连接
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();

        }
        return new Pair<>(lines,jsonData);
    }


    public static ArrayList<Line> parseRemoteLineStr(String json){
        ArrayList<Line> lines = new ArrayList<>();
        try {
            // 解析 JSON 数据
            JSONObject jsonObject = new JSONObject(json);

            Iterator<String> keys = jsonObject.keys();
            // 遍历并输出 IP 信息
            while (keys.hasNext()){
                String key = keys.next();
                String ip = jsonObject.optString(key);
                lines.add(new Line(Integer.parseInt(key),"线路"+key,ip,null,false));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return lines;
    }
}
