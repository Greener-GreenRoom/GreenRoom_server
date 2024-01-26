package com.greenroom.server.api.data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.web.reactive.function.client.WebClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

@Component
@Slf4j
public class GardeningData {
    @Value("${gardening-data.api-key}")
    private String apiKey;
    WebClient webClient = WebClient.create();

    ///실내 정원 식물 list 추출
    public Map<String, ArrayList<String>> plantList() {

        Map<String,ArrayList<String>> plantBasic = new HashMap<String,ArrayList<String>>();
        for (int num=1;num<2;num++) {

            String baseURL = "http://api.nongsaro.go.kr/service/garden/gardenList?apiKey="+apiKey+"&pageNo="+Integer.toString(num);
            String listResult = webClient.get().uri(baseURL)
                    .retrieve()
                    .bodyToMono(String.class).block();


            JSONObject jsonResult = XML.toJSONObject(listResult);
            String jsonStr = jsonResult.toString(4);

            JSONArray items = (JSONArray)jsonResult.getJSONObject("response")
                    .getJSONObject("body")
                    .getJSONObject("items")
                    .getJSONArray("item");

            for (int i =0; i<items.length();i++) {
                String name = (String) items.getJSONObject(i).get("cntntsSj");
                String id = (String) items.getJSONObject(i).get("cntntsNo");
                String imageURL = (String) items.getJSONObject(i).get("rtnFileUrl");
                String[] imageURLS = (String[]) imageURL.split(".jpg");
                ArrayList<String> nameAndUrl = new ArrayList<String>();
                nameAndUrl.add(name);
                nameAndUrl.add(imageURLS[0]+".jpg");
                plantBasic.put(id,nameAndUrl);
            }
        }
        return plantBasic;

    }

    ///실내 정원 식물 세부 정보
    public JSONArray plantInfo(Map<String,ArrayList<String>> plantList) {
        String name = "";
        String imageURL = "";
        String sname = "";
        String manageLevel = "";
        String temperature = "";
        String hdCode = "";
        String fertilizer = "";
        String light = "";
        String place = "";
        String waterSpring = "";
        String waterSummer = "";
        String waterAutumn = "";
        String waterWinter = "";
        String origin = "";
        String height = "";
        String type= "";
        String leafColor = "";
        String flowerColor = "";


        JSONArray objList = new JSONArray();
        Iterator<String> keys = plantList.keySet().iterator();

        while(keys.hasNext()){
            String strKey = keys.next();
            name = plantList.get(strKey).get(0);
            imageURL = plantList.get(strKey).get(1);
            String baseURL = "http://api.nongsaro.go.kr/service/garden/gardenDtl?apiKey="+apiKey+"&cntntsNo="+strKey;


            String infoResult = webClient.get().uri(baseURL)
                    .retrieve()
                    .bodyToMono(String.class).block();
            JSONObject jsonResult = XML.toJSONObject(infoResult);

//			String jsonStr = jsonResult.toString(4);
//			System.out.println(jsonStr);

            JSONObject information = jsonResult.getJSONObject("response")
                    .getJSONObject("body")
                    .getJSONObject("item");

            try{
                sname=(String)information.get("plntbneNm");
            }
            catch(JSONException e) {
                sname = "";
            }

            try{
                manageLevel=(String)information.get("managelevelCode");
            }
            catch(JSONException e) {
                manageLevel = "";
            }

            try{
                temperature =(String)information.get("grwhTpCode");
            }
            catch(JSONException e) {
                temperature = "";
            }
            try{
                hdCode=(String)information.get("hdCode");
            }
            catch(JSONException e) {
                hdCode = "";
            }

            try{
                fertilizer=(String)information.get("frtlzrInfo");
            }
            catch(JSONException e) {
                fertilizer = "";
            }

            try{
                light=(String)information.get("lighttdemanddoCodeNm");
            }
            catch(JSONException e) {
                light = "";
            }

            try{
                waterSpring=(String)information.get("watercycleSprngCode");
            }
            catch(JSONException e) {
                waterSpring = "";}

            try{
                waterSummer=(String)information.get("watercycleSummerCode");
            }
            catch(JSONException e) {
                waterSummer = "";}
            try{
                waterAutumn=(String)information.get("watercycleAutumnCode");
            }
            catch(JSONException e) {
                waterAutumn = "";}
            try{
                waterWinter=(String)information.get("watercycleWinterCode");
            }
            catch(JSONException e) {
                waterWinter = "";}

            try{
                origin=(String)information.get("orgplceInfo");
            }
            catch(JSONException e) {
                origin = "";}

            try{
                height=(String)information.get("growthHgInfo");
            }
            catch(JSONException e) {
                height = "";}

            try{
                type =(String)information.get("fmlCodeNm");
            }
            catch(JSONException e) {
                type = "";}

            try{
                place =(String)information.get("postngplaceCodeNm");
            }
            catch(JSONException e) {
                place = "";}

            try{
                leafColor =(String)information.get("lefcolrCodeNm");
            }
            catch(JSONException e) {
                leafColor = "";}

            try{
                flowerColor =(String)information.get("flclrCodeNm");
            }
            catch(JSONException e) {
                flowerColor = "";}


//			String result = "이름:"+name+", 이미지:"+imageUrl+", 학명:"+sname+", 관리난이도:"+manageLevel+", 온도:"+temperature+", 습도코드:"+hdCode+", 비료:"+fertilizer+", 광도:"+light+", 물봄:"+waterSpring+" 물여름:"+waterSummer+" 물가을:"+waterAutumn+" 물겨울:"+waterWinter
//					+", 원산지:"+origin+", 위치:"+place+", 성장 높이 정보:"+height+", 종류:"+type+", 잎 색깔:"+leafColor+", 꽃 색:"+flowerColor;
//			System.out.println(result);
            JSONObject obj = new JSONObject();

            obj.put("name", name);
            obj.put("sname", sname);
            obj.put("imageURL", imageURL);
            obj.put("manageLevel",manageLevel );
            obj.put("temperature", temperature);
            obj.put("hdCode", hdCode);
            obj.put("fertilizer", fertilizer);
            obj.put("light", light);
            obj.put("waterSpring", waterSpring);
            obj.put("waterSummer", waterSummer);
            obj.put("waterAutumn",waterAutumn);
            obj.put("waterWinter", waterWinter);
            obj.put("origin", origin);
            obj.put("height", height);
            obj.put("type", type);
            obj.put("place", place);
            obj.put("leafColor", leafColor);
            obj.put("flowerColor", flowerColor);

            objList.put(obj);}
        return objList;


    }

}
