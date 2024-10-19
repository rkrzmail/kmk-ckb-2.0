package com.kmkbe.nikita.data;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.kmkbe.nikita.data.nson.JsonArray;
import com.kmkbe.nikita.data.nson.JsonObject;
import com.kmkbe.nikita.data.nson.Nullable;


import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Nson implements JsonObject, JsonArray {
    Object object;
   /*
    []
    {}
    args= [0,0,0,0,{}] like flutter
     */

    public static void main(String[] args) {
        Nson a = Nson.newArray();
        a.add("aa");
        a.add("bb");
        a.set("tiga","s");
        a.set("satu","satu");
        a.set("dUua","dua");
        a.set("tiga","tiga");
        a.asJsonArray().add("cc");
        a.add(123);
        a.add(34.09);
        a.add(true);

        a.add(Nson.empty());
        System.out.println(a.getIntern());

        System.out.println(a.asJsonArray().get(3).asString());
        System.out.println(a.toJson());




    }

    private static Map _createObject(){
        return new TreeMap(String.CASE_INSENSITIVE_ORDER);
    }


    public static Nson empty() {
        return new Nson();
    }

    private Nson(Object object){
        this.object = object;
    }
    public Nson(){

    }
    public static Nson readJson(String json) {
        return readJson(json.getBytes(StandardCharsets.UTF_8));
    }

    public static Nson readJson(byte[] json) {
        return readJson(new ByteArrayInputStream(json));
    }

    public static Nson readJson(InputStream json) {
        Nson nson = new Nson();
        nson.parseJson(json);

        return nson;
    }
    public static Nson from(String text){
        return new Nson(text);
    }

    public static Nson from(List list){
        return new Nson(list);
    }

    public static Nson from(Map map){
        return new Nson(map);
    }

    public static Nson newArray() {
        return new Nson(new ArrayList<>());
    }

    public static Nson newObject() {
        return new Nson(_createObject());
    }

    public Nson set(String key, Nson value) {
        return _setInternal(key, value.getIntern());
    }

    public Nson set(String key, String value) {
        return _setInternal(key, value);
    }

    public Nson set(String key, Boolean value) {
        return _setInternal(key, value);
    }

    public Nson set(String key, Number value) {
        return _setInternal(key, value);
    }

    public Nson set(String key, Object value) {
        return _setInternal(key, value);
    }

    private Nson _setInternal(String key, Object value) {
        if (object instanceof Map map) {
            map.put(key, value);
        }else if (object instanceof List list) {
            //add 30/10/23
            Map curr = _createObject();
            curr.put(key, value);
            list.add(curr);//add sebagai object
        }
        return this;
    }

    public Nson add(Nson value) {
        return _addInternal(value.getIntern());
    }

    public Nson add(String value) {
        return _addInternal(value);
    }

    public Nson add(Boolean value) {
        return _addInternal(value);
    }

    public Nson add(Number value) {
        return _addInternal(value);
    }

    public Nson add(Object value) {
        return _addInternal(value);
    }

    private Nson _addInternal(Object value) {
        if (object instanceof List list) {
            list.add(value);
        }
        return this;
    }
    public Nson get(int index) {
        if (object instanceof List list) {
            if (list.size() > index && index >= 0) {
                return  new Nson(list.get(index));
            }
        } else if (object instanceof String[] ss) {
            //add 28/08/22
            if (ss.length > index && index >= 0) {
                return new Nson(ss[index]);
            }
        }
        return Nson.empty();
    }
    public Nson get(String key) {
        if (object instanceof Map map) {
            if (map.containsKey(key)) {
                return new Nson(map.get(key));
            }
        }else if (object instanceof List list) {
            //add 30/10/23 like flutter
            if (!list.isEmpty() && list.getLast() instanceof Map map){
                if (map.containsKey(key)) {
                    return new Nson(map.get(key));
                }
            }
        }
        return Nson.empty();
    }
    public Nson get(JsonArray keys) {
        //add 15/01/24
        Nson ret = this;
        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i).isNumber()){
                ret = ret.get(keys.get(i).asNumber().intValue());
            }else{
                ret = ret.get(keys.get(i).asString());
            }

        }
        return ret;
    }
    public String asString() {
        return String.valueOf(object);
    }
    public boolean asBoolean() {
        if (getIntern() instanceof Boolean) {
            return (Boolean) getIntern();
        }
        return Boolean.getBoolean(getInternalAsString());
    }

    public int asInteger() {
        if (getIntern() instanceof Integer) {
            return (Integer) getIntern();
        }
        return getNumber(getInternalAsString()).intValue();
    }

    public Number asNumber() {
        if (getIntern() instanceof Number) {
            return (Number) getIntern();
        }
        return getNumber(getInternalAsString());
    }

    public long asLong() {
        if (getIntern() instanceof Long) {
            return (Long) getIntern();
        }
        return getNumber(getInternalAsString()).longValue();
    }

    public double asDouble() {
        if (getIntern() instanceof Double) {
            return (Double) getIntern();
        }
        return getNumber(getInternalAsString()).doubleValue();
    }

    private String getInternalAsString() {
        return String.valueOf(getIntern());
    }

    public String asDecimalString() {
        if (getIntern() instanceof Double) {
            return BigDecimal.valueOf((Double) getIntern()).toPlainString();
        } else if (getIntern() instanceof Integer) {
            return BigDecimal.valueOf((Integer) getIntern()).toPlainString();
        } else if (getIntern() instanceof Long) {
            return BigDecimal.valueOf((Long) getIntern()).toPlainString();
        }
        return BigDecimal.valueOf(asNumber().doubleValue()).toPlainString();
    }

    public Object getIntern(){
        return this.object;
    }
    public void setIntern(Object internalObject) {
        this.object = internalObject;
    }
    public Nson asNson(){
        return this;
    }
    public JsonObject asJsonObject(){
        if (object instanceof Map ){
            return this;
        }else{
            return Nson.empty();
        }
    }

    public Map asArrayMap(){
        if (object instanceof Map map){
            return map;
        }else{
            return _createObject();
        }
    }

    public JsonArray asJsonArray(){
        if (object instanceof List){
            return  this;
        }else{
            return Nson.empty();
        }
    }
    public List asArrayList(){
        if (object instanceof List list){
            return  list;
        }else{
            return new ArrayList();
        }
    }



    public String toJson() {
        StringWriter writer = new StringWriter();
        toJson(writer);
        return writer.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return object.equals((obj instanceof Nson nson ? nson.object : obj));
    }

    @Override
    public String toString() {
        return toJson() ;
    }
    public int size(){
        if (object instanceof List lst) {
            return lst.size();
        }else if (object instanceof Map map){
            return map.size();
        }else{
            return 0;
        }
    }

    protected final void parseJson(InputStream is) {
        Object dataObject;
        //setErrorMessage(null);
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            reader.setLenient(true);
            switch (reader.peek()) {
                case BEGIN_ARRAY:
                    dataObject = JsonArray(reader);
                    break;
                case BEGIN_OBJECT:
                    dataObject = JsonObject(reader);
                    break;
                default:
                    dataObject = "";
                    break;
            }
            setIntern(dataObject);
        } catch (Exception ex) {
            //setErrorMessage(ex.getMessage());
            setIntern(null);
        }

    }
    private static Object JsonArray(JsonReader reader) throws IOException {
        List vector = new ArrayList();
        reader.beginArray();
        while (reader.hasNext()) {
            vector.add(JsonValue(reader));
        }
        reader.endArray();
        return vector;
    }


    private static Object JsonObject(JsonReader reader) throws IOException {
        Map hashtable = _createObject();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = JsonMember(reader);
            hashtable.put(name, JsonValue(reader));
        }
        reader.endObject();
        return hashtable;
    }

    private static String JsonMember(JsonReader reader) throws IOException {
        if (reader.peek().equals(JsonToken.NAME)) {
            return reader.nextName();
        }
        return "";//must error
    }

    private static Object JsonValue(JsonReader reader) throws IOException {
        switch (reader.peek()) {
            case BEGIN_ARRAY:
                return JsonArray(reader);
            case BEGIN_OBJECT:
                return JsonObject(reader);
            case NUMBER:
                String s = reader.nextString();
                if (_isLongIntegerNumber(s)) {
                    return getNumber(s).longValue();
                } else {
                    return getNumber(s).doubleValue();
                }
            case BOOLEAN:
                return reader.nextBoolean();
            case NULL:
                reader.nextNull();
                return Nullable.NULL;
            case STRING:
            default:
                return reader.nextString();
        }
    }
    private static Number getNumber(Object n) {
        if (n instanceof Number) {
            return ((Number) n);
        } else if (_isDecimalNumber(String.valueOf(n))) {
            return Double.valueOf(String.valueOf(n));
        }
        return 0;
    }
    private static boolean _isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
    private static boolean _isDecimalNumber(String str) {
        return str.matches("^[-+]?[0-9]*.?[0-9]+([eE][-+]?[0-9]+)?$");
    }

    private static boolean _isLongIntegerNumber(String str) {
        return str.matches("-?\\d+");
    }

    public void toJson(OutputStream outputStream) {
        toJson(new OutputStreamWriter(outputStream));
    }

    public void toJson(Writer writer) {
        JsonWriter jsonWriter = new JsonWriter(writer);
        Object dataObject = getIntern();
        try {
            if (dataObject == null) {
                //nothing
            } else if (dataObject instanceof List) {
                OutJsonArray(jsonWriter, (List) dataObject);
            } else if (dataObject instanceof Map) {
                OutJsonObject(jsonWriter, (Map) dataObject);
            } else if (dataObject instanceof String[]) {
                OutJsonArrayString(jsonWriter, ((String[]) dataObject));
            }
            jsonWriter.flush();
        } catch (IOException e) {

        }
    }

    private static void OutJsonArrayString(JsonWriter jsonWriter, String[] list) throws IOException {
        jsonWriter.beginArray();
        for (int i = 0; i < list.length; i++) {
            OutJsonValue(jsonWriter, list[i]);
        }
        jsonWriter.endArray();
    }

    private static void OutJsonArray(JsonWriter jsonWriter, List list) throws IOException {
        jsonWriter.beginArray();
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            OutJsonValue(jsonWriter, iterator.next());
        }
        jsonWriter.endArray();
    }

    private static void OutJsonObject(JsonWriter jsonWriter, Map object) throws IOException {
        jsonWriter.beginObject();
        Iterator iterator = object.keySet().iterator();
        while (iterator.hasNext()) {
            String key = String.valueOf(iterator.next());
            jsonWriter.name(key);
            OutJsonValue(jsonWriter, object.get(key));
        }
        jsonWriter.endObject();
    }

    private static void OutJsonValue(JsonWriter jsonWriter, Object object) throws IOException {
        /*if (object instanceof Object vir) {
            object = vir.object;
        }*/
        if (object == null || object instanceof Nullable) {
            String nul = null;
            jsonWriter.value(nul);
        } else if (object instanceof Nson nson) {
            OutJsonValue(jsonWriter, nson.getIntern());
        } else if (object instanceof Map map) {
            OutJsonObject(jsonWriter, map);
        } else if (object instanceof List list) {
            OutJsonArray(jsonWriter, list);
        } else if (object instanceof String) {
            jsonWriter.value(String.valueOf(object));
        } else if (object instanceof String[] strings) {
            OutJsonArrayString(jsonWriter,strings);
        } else if (object instanceof Number num) {
            jsonWriter.value(num);
        } else if (object instanceof Boolean bool) {
            jsonWriter.value(bool.booleanValue());
        } else {
            jsonWriter.value(String.valueOf(object));
        }
    }

    public boolean containsKey(String key) {
        if (object instanceof Map map) {
            return map.containsKey(key);
        }
        return false;
    }

    public boolean containsValue(String value) {
        if (object instanceof Map) {
            Nson keys = getObjectKeys();
            for (int i = 0; i < keys.size(); i++) {
                if (get(keys.get(i).asString()).asString().equals(value)) {
                    return true;
                }
            }
        } else if (object instanceof List) {
            for (int i = 0; i < size(); i++) {
                if (get(i).asString().equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }
    public Nson getObjectKeys() {
        List list = new ArrayList();
        if ( object instanceof  Map map) {
            Iterator iterator = map.keySet().iterator();

            while (iterator.hasNext()) {
                String key = String.valueOf(iterator.next());
                list.add(key);
            }
        }
        return new Nson(list);
    }

    private boolean _isArray(Object obj) {
        return obj instanceof Object[] || obj instanceof boolean[] ||
                obj instanceof byte[] || obj instanceof short[] ||
                obj instanceof char[] || obj instanceof int[] ||
                obj instanceof long[] || obj instanceof float[] ||
                obj instanceof double[];
    }

    public boolean isNsonArray() {
        return object instanceof List  || _isArray(object);
    }

    public boolean isNsonObject() {
        return object instanceof Map;
    }

    public boolean isNull() {
        if (object == null) {
            return true;
        } else if (this.equals(Nullable.NULL) ) {
            return true;
       /* } else if (object instanceof Object nson) {
            return nson.object == null;*/
        }
        return false;
    }

    public boolean isNson() {
        return isNsonArray() || isNsonObject();
    }

    public boolean isNumeric() {
        if (object instanceof Number){
            return true;
        }else{
            return _isNumeric(String.valueOf(object));
        }
    }

    public boolean isNumber() {
        return object instanceof Number;
    }

    public boolean isBoolean() {
        return object instanceof Boolean;
    }

    public boolean isString() {
        return object instanceof String;
    }
    //private final JsonArray _JA =  (JsonArray) Nson.newArray();
    //private final JsonObject _JO = (JsonObject) Nson.newObject();;
    //private final Nson Nson.empty() =  Nson.empty();

    public void forEach(Consumer<Nson> consumer){
        if (object instanceof Map map){
            map.forEach((object1, object2) -> {
                consumer.accept(get(String.valueOf(object1)));
            });
        }else if (object instanceof List list){
            for (int i = 0; i < list.size(); i++) {
                consumer.accept(get(i));
            }
        }
    }
    public void forEachWithBreak(BiConsumer<Nson, AtomicReference<Boolean>> consumer){
        if (object instanceof Map map){
            AtomicReference<Boolean> next= new AtomicReference<>();
            Iterator iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                String key = String.valueOf(iterator.next());
                consumer.accept(get(key), next);
                if (next.get() != null){
                    break;
                }
            }
        }else if (object instanceof List list){
            AtomicReference<Boolean> next = new AtomicReference<>();
            for (int i = 0; i < list.size(); i++) {
                consumer.accept(get(i), next);
                if (next.get() != null){
                    break;
                }
            }
        }
    }


}
