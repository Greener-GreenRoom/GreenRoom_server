package com.greenroom.server.api.data;

import com.greenroom.server.api.domain.greenroom.entity.Plant;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Transactional
@RequiredArgsConstructor
@Service
public class GardeningDataService {
    private final GardeningData gd;
    private final GardeningDataRepository gdr;
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    public void insertPlant() {
        Map<String, ArrayList<String>> plantList = gd.plantList();
        JSONArray objects = gd.plantInfo(plantList);
        for(int i=0;i<objects.length();i++) {

            JSONObject object = objects.getJSONObject(i);

            ///json parsing -> 변수 할당
            String name = object.getString("name");
            name = name.replace(" ","");
            String sname = object.getString("sname");
            String manageLevel = object.getString("manageLevel");
            String temperature = object.getString("temperature");
            String hdCode = object.getString("hdCode");
            String fertilizer = object.getString("fertilizer");
            String light = object.getString("light");
            String waterSpring = object.getString("waterSpring");
            String waterSummer = object.getString("waterSummer");
            String waterAutumn = object.getString("waterAutumn");
            String waterWinter = object.getString("waterWinter");
            String place = object.getString("place");
            String origin = object.getString("origin").replace(", ", ",");
            String height = object.getString("height");
            String type= object.getString("type");
            String leafColor = object.getString("leafColor").replace(", ", ",");
            String flowerColor = object.getString("flowerColor").replace(", ", ",");
            String imageURL=object.getString("imageURL");

            //불필요한 워딩 제거
            place = place.replace(" (실내깊이 300~500cm)", "").replace(" (실내깊이 150~300cm)", "")
                    .replace(" (실내깊이 50~150cm)", "")
                    .replace(" (실내깊이 0~50cm)", "")
                    .replace(" (실내깊이 500 이상cm)","");

            fertilizer = fertilizer.replace("요구함","요구해요.").replace("하지않음","하지 않아요.");

            // 정보 조합해서 other_information 말 만들기
            String leafinformation = "";
            String flowerinformation = "";
            String baseinformation = name+"는(은) "+type+" 식물입니다. 높이는 "+height+"cm까지 자라며, "+ place+"에서 키우는 것이 가장 적합합니다.";
            if (!leafColor.isEmpty()) {leafinformation = "잎은 "+ leafColor;}
            if (!flowerColor.isEmpty()) {flowerinformation = "이며 " +flowerColor+"의 꽃이 핍니다."; }
            else {flowerinformation ="입니다.";}

            String information = baseinformation + leafinformation+flowerinformation;

            //// 물주기 계절별로 요약
            List<String> one = new ArrayList<>();
            List<String> two = new ArrayList<>();
            List<String> three = new ArrayList<>();
            List<String> four = new ArrayList<>();

            if (waterSpring.equals("053001")) {one.add("봄");}
            else if (waterSpring.equals("053002")) {two.add("봄");}
            else if (waterSpring.equals("053003")) {three.add("봄");}
            else if (waterSpring.equals("053004")) {four.add("봄");}

            if (waterSummer.equals("053001")) {one.add("여름");}
            else if (waterSummer.equals("053002")) {two.add("여름");}
            else if (waterSummer.equals("053003")) {three.add("여름");}
            else if (waterSummer.equals("053004")) {four.add("여름");}

            if (waterAutumn.equals("053001")) {one.add("가을");;}
            else if (waterAutumn.equals("053002")) {two.add("가을");}
            else if (waterAutumn.equals("053003")) {three.add("가을");}
            else if (waterAutumn.equals("053004")) {four.add("가을");}

            if (waterWinter.equals("053001")) {one.add("겨울");}
            else if (waterWinter.equals("053002")) {two.add("겨울");}
            else if (waterWinter.equals("053003")) {three.add("겨울");}
            else if (waterWinter.equals("053004")) {four.add("겨울");}

            String watercycle = "";

            if(!one.isEmpty()) {
                String  temp = String.join(",", one);
                watercycle = watercycle +temp+"에는 흙을 항상 축축하게 물에 잠길 정도로 유지해주세요.";
            }
            if(!two.isEmpty()) {
                String  temp = String.join(",", two);
                watercycle = watercycle +temp+"에는 물에 잠기지 않도록 주의하여 흙을 촉촉하게 유지해주세요.";
            }
            if(!three.isEmpty()) {
                String  temp = String.join(",", three);
                watercycle = watercycle +temp+"에는 토양 표면이 말랐을때 충분히 관수해주세요.";
            }
            if(!four.isEmpty()) {
                String  temp = String.join(",", four);
                watercycle = watercycle +temp+"에는 화분 흙이 대부분 말랐을때 충분히 관수해주세요.";
            }

            /////광도 표현 방법 변경

            if (light.contains("낮은 광도(300~800 Lux)") && light.contains("중간 광도(800~1,500 Lux)") && light.contains("높은 광도(1,500~10,000 Lux)")) {light="햇빛에 크게 영향을 받지 않아요.";}
            else if (light.contains("낮은 광도(300~800 Lux)") && light.contains("중간 광도(800~1,500 Lux)"))  {light="햇빛이 필요하지 않지만 적당한 햇빛은 괜찮아요.";}
            else if (light.contains("낮은 광도(300~800 Lux)") && light.contains("높은 광도(1,500~10,000 Lux)")) {light="햇빛이 필요하지 않지만 때로는 강한 햇빛이 필요해요.";}
            else if (light.contains("중간 광도(800~1,500 Lux)") && light.contains("높은 광도(1,500~10,000 Lux)")) {light="적당한 햇빛 또는 강한 햇빛이 필요해요.";}
            else if (light.contains("낮은 광도(300~800 Lux)")) {light="햇빛이 필요하지 않아요.";}
            else if (light.contains("중간 광도(800~1,500 Lux)")) {light="적당한 햇빛을 유지해주세요.";}
            else if (light.contains("높은 광도(1,500~10,000 Lux)")) {light="아주 강한 햇빛이 필요해요.";}


            /// 온도 코드에서 다른 표현으로 변경
            if (temperature.equals("082001")) {temperature = "10~15℃";}
            else if (temperature.equals("082002")) {temperature = "16~20℃";}
            else if (temperature.equals("082003")) {temperature = "21~25℃";}
            else if (temperature.equals("082004")) {temperature = "26~30℃";}

            /// 습도 코드에서 다른 표현으로 변경
            if (hdCode.equals("083001")) {hdCode = "40%미만의 건조한 환경을 유지해주세요.";}
            else if (hdCode.equals("083002")) {hdCode = "40~70%의 적당한 습도를 유지해주세요.";}
            else if (hdCode.equals("083003")) {hdCode = "70%이상의 습한 환경을 유지해주세요.";}

            // 관리 난이도 코드에서 다른 표현으로 변경
            if (manageLevel.equals("089001")) {manageLevel = "쉬움";}
            else if (manageLevel.equals("089002")) {manageLevel = "보통";}
            else if (manageLevel.equals("089003")) {manageLevel = "어려움";}

//            String re = i + 1 + name + type + imageURL + timestamp + timestamp + 0 + watercycle+light+temperature+hdCode+fertilizer+manageLevel+information;
//            System.out.println(re);

            Plant plant = Plant.builder()
                    .plantAlias(sname)
                    .distributionName(name)
                    .plantPictureUrl(imageURL)
                    .plantCount(0)
                    .waterCycle(watercycle)
                    .fertilizer(fertilizer)
                    .lightDemand(light)
                    .humidity(hdCode)
                    .manageLevel(manageLevel)
                    .growthTemperature(temperature)
                    .otherInformation(information)
                    .build();
            Plant result = gdr.save(plant);
        }
    }
    public void deleteData(){
        gdr.deleteAllInBatch();
    }
}
