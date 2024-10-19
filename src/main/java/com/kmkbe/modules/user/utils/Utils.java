package com.kmkbe.modules.user.utils;



import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class Utils {
    public static final char SPACE =' ';
    public static final char DOUBLE_QUOTE ='"';
    public static final String ENTER ="\r\n";

    private static String escapeStringForMySQL(String s) {
        return s.replace("\\", "\\\\")
                .replace("\b", "\\b")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace(new String(Character.toChars(26)), "\\Z")
                .replace(new String(Character.toChars(0)), "\\0")
                .replace("'", "\\'")
                .replace("\"", "\\\"");
    }

    private static String escapeWildcardsForMySQL(String s) {
        return escapeStringForMySQL(s)
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
    public static String trims(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String>  splits = Utils.splitList(str, String.valueOf(SPACE));
        splits.forEach(s -> {
            stringBuilder.append(s).append(s.equals("")?"":" ");
        });

        return stringBuilder.toString().trim();
    }
    public static String titleCase(String str) {
        StringBuilder stringBuilder = new StringBuilder(str.length()>=1?str.substring(0,1).toUpperCase():"");
        for (int i = 1; i < str.length(); i++) {
            if (str.charAt(i-1) == SPACE){
                stringBuilder.append(Character.toTitleCase(str.charAt(i)));
            }else{
                stringBuilder.append(str.charAt(i));
            }
        }
        return stringBuilder.toString() ;
    }

    public static String formatNumber(String original) {
        return original.replace(",", "");
    }

    public static String formatCurrency(String original) {
        if (original.contains(".")) {
            StringBuilder stringBuilder = new StringBuilder();
            int il = original.indexOf(".");
            stringBuilder.append(insertStringRev(original.substring(0, il), ",", 3));
            stringBuilder.append(original.substring(il));
            return stringBuilder.toString();
        }
        return insertStringRev(original, ",", 3);
    }

    public static String insertStringRev(String original, String sInsert, int igroup) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < original.length(); i++) {

            if (((original.length() - i) % igroup) == 0 && igroup != 0 && i != 0) {
                sb.append(sInsert + original.substring(i, i + 1));
            } else {
                sb.append(original.substring(i, i + 1));
            }
        }
        return sb.toString();
    }

    public static String getNumberOnly(String s) {
        final String number = "01234567890";
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (number.indexOf(s.charAt(i)) != -1) {
                buf.append(s.charAt(i));
            }
        }
        return buf.toString();
    }

    public static String repeat(String text, int repeat) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < repeat; i++)
            sb.append(text);
        return sb.toString();
    }
    public static String mid(String _text, int _searchStr, int _replacementStr) {
        return _text;
    }
    public static String left(String _text, int _searchStr ) {
        return _text;
    }
    public static String right(String _text, int _searchStr ) {
        return _text;
    }
    public static String substring(String _text, int _searchStr, int _replacementStr) {
        try {
            if (_replacementStr == 0){
                return _text.substring(_searchStr);
            }else{
                return _text.substring(_searchStr, _replacementStr);
            }

        }catch (Exception exception){}
        return "";
    }
    public static String rsubstring(String _text, int _searchStr, int _replacementStr) {
        return _text;
    }
    public static String revstring(String _text) {
        return _text;
    }
    public static String replace(String _text, String _searchStr, String _replacementStr) {
        StringBuilder sb = new StringBuilder();
        int searchStringPos = _text.indexOf(_searchStr);
        int startPos = 0;
        int searchStringLength = _searchStr.length();
        while (searchStringPos != -1) {
            sb.append(_text.substring(startPos, searchStringPos)).append(_replacementStr);
            startPos = searchStringPos + searchStringLength;
            searchStringPos = _text.indexOf(_searchStr, startPos);
        }
        sb.append(_text.substring(startPos, _text.length()));
        return sb.toString();
    }

    /*public static List<String> splits(String original, String space) {
        List<String> nodes = new ArrayList<>();
        char ch = 0;
        int pos = 0;
        StringBuilder sb = new StringBuilder();
        while (pos < original.length()) {
            char str = original.charAt(pos);
            pos++;
            if (str == '\r' || str == '\n') {
                String s = sb.toString();
                if (s.length() >= 1)
                    nodes.add(s);
                nodes.clear();
            } else {
                sb.append(str);
            }
        }
        String s = sb.toString();
        if (s.length() >= 1)
            nodes.add(s);
        return nodes;
    }*/

    public static List<String> splitList(String original, String separator) {
        List<String> nodes = new ArrayList<>();
        int index = original.indexOf(separator);
        while (index >= 0) {
            nodes.add(original.substring(0, index));
            original = original.substring(index + separator.length());
            index = original.indexOf(separator);
        }
        nodes.add(original);
        return nodes;
    }
    public static String NowTz() {
        Calendar calendar = Calendar.getInstance();
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ssXXX").format(calendar.getTime());
    }
    public static String Now() {
        Calendar calendar = Calendar.getInstance();
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
    }
    public static long NowTime() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTimeInMillis();
    }
    public static Date NowDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }
    public static String getTimeZone() {
        return TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT, Locale.US);
    }
    public static final String hashNikita(String _key,  String...s) {
        StringBuilder stringBuilder = new StringBuilder(_key);
        for (int i = 0; i < s.length; i++) {
            stringBuilder.append(s[i]);
        }
        stringBuilder.append(_key);
        return MD5(stringBuilder.toString());
    }
    public static final String hashNikita(final String s) {
        return MD5(s);
    }
    public static final String MD5(final String s) {
        if (!s.equals("")) {
            try {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                digest.update(s.getBytes());
                byte messageDigest[] = digest.digest();

                // Create Hex String
                StringBuilder hexString = new StringBuilder();
                for (int i = 0; i < messageDigest.length; i++) {
                    String h = Integer.toHexString(0xFF & messageDigest[i]);
                    while (h.length() < 2)
                        h = "0" + h;
                    hexString.append(h);
                }
                return hexString.toString();
            } catch (NoSuchAlgorithmException e) {
            }

        }
        return "";
    }

    public static final String RND() {
        Random rand = new Random();

        return String.valueOf(Math.abs(rand.nextLong()));//+ System.nanoTime()
    }
    public static final String SHA1(final String s) {
        if (!s.equals("")) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA1");
                digest.update(s.getBytes());
                byte messageDigest[] = digest.digest();

                StringBuilder hexString = new StringBuilder();
                for (int i = 0; i < messageDigest.length; i++) {
                    hexString.append(Integer.toString((messageDigest[i] & 0xff) + 0x100, 16).substring(1));
                }
                return hexString.toString();
            } catch (NoSuchAlgorithmException e) {
            }
        }
        return "";
    }
    public static final String SHA256(final String s) {
        if (!s.equals("")) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                digest.update(s.getBytes());
                byte messageDigest[] = digest.digest();

                StringBuilder hexString = new StringBuilder();
                for (int i = 0; i < messageDigest.length; i++) {
                    hexString.append(Integer.toString((messageDigest[i] & 0xff) + 0x100, 16).substring(1));
                }
                return hexString.toString();
            } catch (NoSuchAlgorithmException e) {
            }
        }
        return "";
    }

    public static final String UUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    private static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "KMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static List<String> getKeys(Map maps) {
        List<String> list = new ArrayList<>();
        Iterator iterator = maps.keySet().iterator();
        while (iterator.hasNext()) {
            list.add(String.valueOf(iterator.next()));
        }
        return list;
    }

    public static String[] getKeysAsString(Map maps) {
        List<String> list = getKeys(maps);
        String[] strings = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            strings[i] = list.get(i);
        }
        return strings;
    }

    public boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }

    public static InputStream readInputStream(String inputStreamPath) {
        try {
            return new FileInputStream(inputStreamPath);
        } catch (Exception e) { }
        return null;
    }
    public static String readInputStreamAsString(String file) {
        try {
            return readInputStreamAsString(new FileInputStream(file));
        } catch (Exception e) {  }
        return "";
    }
    public static String readInputStreamAsString(InputStream inputStream) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            inputStream.close();
        } catch (Exception e) {
        }
         return baos.toString( );//StandardCharsets.UTF_8
    }
    public static void copyStream(InputStream inputStream, OutputStream baos) {
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            inputStream.close();
        } catch (Exception e) {

        }
    }

    public static int getInt(String s) {
        return getNumber(s).intValue();
    }

    public static long getLong(String s) {
        return getNumber(s).longValue();
    }

    public static double getDouble(Object n) {
        return getNumber(n).doubleValue();
    }

    public static float getFloat(String s) {
        return getNumber(s).floatValue();
    }

    public static int getNumberAsInt(String s) {
        return getNumber(s).intValue();
    }

    public static long getNumberAsLong(String s) {
        return getNumber(s).longValue();
    }

    public static double getNumberAsDouble(Object n) {
        return getNumber(n).doubleValue();
    }

    public static float getNumberAsFloat(String s) {
        return getNumber(s).floatValue();
    }

    public static Number getNumber(Object n) {
        if (n instanceof Number) {
            return ((Number) n);
        } else if (isNumeric(String.valueOf(n))) {
            return Double.valueOf(String.valueOf(n));
        }
        return 0;
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    private static boolean isDecimalNumber(String str) {
        return str.matches("^[-+]?[0-9]*.?[0-9]+([eE][-+]?[0-9]+)?$");
    }

    public static boolean isLongIntegerNumber(String str) {
        return str.matches("-?\\d+");
    }

    public static String lTrim(String str) {

        return str.replaceAll("^\\s+", "");// Pattern.compile("^\\s+").matcher(s).replaceAll("");;
    }

    public static String rTrim(String str) {
        return str.replaceAll("\\s+$", "");
    }
    public static boolean isEmptyOrNull(String data){
        if (data != null) {
            return data.equalsIgnoreCase("")||data.equalsIgnoreCase("null");
        }
        return true;
    }
    public static Date dateAdd(Date date, int calenderfield, int inc) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(calenderfield, inc); //minus number would decrement the days
        return cal.getTime();
    }


    public static String valueOf(Object obj) {
        return (obj == null) ? "" : obj.toString();
    }


    public static String base64Encode(String str) {
        try {
            return Base64.getEncoder().encodeToString(str.getBytes());
        }catch (Exception e){}
        return "";
    }
    public static String base64Decode(String str) {
        try {
            return new String(Base64.getDecoder().decode(str));
        }catch (Exception e){}
        return "";
    }
    public static String toBase36(String str) {
        try {
            return Long.toString(Long.valueOf(str), 36).toUpperCase();
        } catch (NumberFormatException | NullPointerException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    public static String fromBase36(String b36) {
        try {
            BigInteger base = new BigInteger( b36, 36);
            return base.toString(10);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static String charFixLength(String original, int length, int orientation) {
        if (original.length() < length){
            StringBuilder sb = new StringBuilder();
            if (orientation == 1){ //right
                for (int i = original.length(); i < length; i++) {
                    sb.append(SPACE);
                }
                sb.append(original);
            }else if (orientation == -1){ //left
                sb.append(original);
                for (int i = sb.length(); i < length; i++) {
                    sb.append(SPACE);
                }
            }else{
                int length1 = (length-original.length())/2;
                for (int i = sb.length(); i < length1; i++) {
                    sb.append(SPACE);
                }
                sb.append(original);
                for (int i = sb.length(); i < length; i++) {
                    sb.append(SPACE);
                }
            }


            return sb.toString();
        }else{
            return original.substring(0,length);
        }
    }
    public static void main(String[] args) {

        System.out.println(SHA256( "uij aa aa   ss   d d"));

    }

    public static void each(List l, Consumer consumer){
        l.forEach(o -> {
            consumer.accept(o);
        });
    }
    public static <T> void forEach(List<T> list, BiConsumer<T, List<T>> each, Consumer<T> eachDelete){
        List<T> deleted = new ArrayList<>();
        list.forEach(object -> each.accept(object, deleted));
        deleted.forEach(object -> eachDelete.accept(object));
    }
    public static <T> void forEach(List<T> list, Function<T, T> each, Consumer<T> eachDelete){
        List<T> deleted = new ArrayList<>();
        list.forEach(object -> each.apply(object));
        deleted.forEach(object -> eachDelete.accept(object));
    }
    public static Object proc(Object ... args){
        return args[0];
    }
    public static String[] listArrays(List<String> list){
        return list.toArray(new String[list.size()]);
    }

    public static List listFromArrays(String[] strings){
        return List.of(strings);
    }
}
