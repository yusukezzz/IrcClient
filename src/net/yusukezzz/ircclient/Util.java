package net.yusukezzz.ircclient;

import java.text.DecimalFormat;
import java.util.Calendar;

import android.util.Log;

public class Util {

    /**
     * hh:mm形式の現在時間文字列を返す
     * @return time hh:mm
     */
    public static String getTime() {
        // 2桁で表示するため
        DecimalFormat df = new DecimalFormat();
        df.applyLocalizedPattern("00");
        // 現在時刻の取得
        Calendar now = Calendar.getInstance();
        int h = now.get(Calendar.HOUR_OF_DAY);
        int m = now.get(Calendar.MINUTE);
        String time = df.format(h) + ":" + df.format(m);
        return time;
    }

    /**
     * デバッグメッセージを出力
     * @param str
     */
    public static void d(String str) {
        Log.d("IRC", str);
    }

    public static void d(StackTraceElement[] stackTrace) {
        for (StackTraceElement e: stackTrace) {
            Log.d("IRC", e.getFileName() + ":" + e.getLineNumber() + " " + e.getMethodName());
        }
    }
}
