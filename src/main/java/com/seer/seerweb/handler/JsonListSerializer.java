package com.seer.seerweb.handler;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;

/**
 * 序列化工具类，string转stringList
 */
public class JsonListSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String s, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        try{
            if(s != null){
                List<String> strings = JSON.parseArray(s).toList(String.class);
                String[] strArray = strings.toArray(String[]::new);
                jsonGenerator.writeArray(strArray, 0, strArray.length);
            } else {
                jsonGenerator.writeNull();
            }
        } catch (Exception e){
            jsonGenerator.writeNull();
        }
    }
}
