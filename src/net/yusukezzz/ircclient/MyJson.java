package net.yusukezzz.ircclient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.util.Log;

public class MyJson {
    private Context context;

    public MyJson(Context context) {
        this.context = context;
    }

    public JSONArray readFile(String filename) {
        JSONArray json = null;
        try {
            FileInputStream fis = this.context.openFileInput(filename);
            byte[] readByte = new byte[fis.available()];
            fis.read(readByte);
            json = new JSONArray(new String(readByte));
        } catch (FileNotFoundException e) {
            // ファイルがなければ空のJSON
            return json = new JSONArray();
        } catch (IOException e) {
        } catch (JSONException e) {
        }
        return json;
    }

    public boolean writeFile(String filename, String src) {
        try {
            FileOutputStream fos = this.context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(src.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
