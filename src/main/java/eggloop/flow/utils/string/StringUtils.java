package eggloop.flow.utils.string;

public class StringUtils {

    public static String replace(String word, String[] keys, double[] values) {
        for (int i = 0; i < keys.length; i++) {
            word = word.replace(keys[i], String.valueOf(values[i]));
        }
        return word;
    }
}
