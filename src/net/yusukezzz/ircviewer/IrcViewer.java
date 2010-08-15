package net.yusukezzz.ircviewer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.widget.TextView;

public class IrcViewer extends Activity {
    private String   HOST    = "irc.friend-chat.jp";
    private Integer  PORT    = 6667;
    private String   NICK    = "androzzz";
    private String   LOGIN   = "androzzz";
    private String   CHANNEL = "#yusukezzz_test";

    private TextView recieve;
    private Handler  handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.viewer);
        // TexxtView�̏���
        recieve = (TextView) this.findViewById(R.id.recieve);
        recieve.setText("irc viewer start...\n");
        // ��M�����e�L�X�g��TextView�ɏo�͂���handler
        this.handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                    	// �o��
                        recieve.setText(msg.obj.toString() + "\n" + recieve.getText()
                                + "\n");
                }
                super.handleMessage(msg);
            }
        };

        // �ʐM�pthread
        (new Thread() {
            public void run() {
                try {
                    connect(HOST, PORT);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }).start();
    }

    private void connect(String host, Integer port) {
        try {
        	// �ʐM�J�n
            Socket irc = new Socket(host, port);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    irc.getOutputStream()));
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    irc.getInputStream(), "ISO-2022-JP"));// �����R�[�h�͂Ƃ肠�������ߑł�
            // ���O�C��
            bw.write("NICK " + NICK + "\n");
            bw.write("USER " + LOGIN + " test by yusukezzz\n");
            bw.flush();
            // �`�����l���ɓ���
            bw.write("JOIN " + CHANNEL + "\n");
            bw.flush();
            // 1�s����M
            String current = null;
            while ((current = br.readLine()) != null) {
            	// PING PONG
                Pattern pingRegex = Pattern.compile("^PING",
                        Pattern.CASE_INSENSITIVE);
                Matcher ping = pingRegex.matcher(current);
                if (ping.find()) {
                    bw.write("PONG " + CHANNEL + "\n");
                    bw.write("PRIVMSG " + CHANNEL + " PONG!\n");
                    bw.flush();
                }
                // ����ȊO�̃e�L�X�g��handler��
                Message msg;
                msg = new Message();
                msg.obj = current;
                msg.what = 0;
                IrcViewer.this.handler.sendMessage(msg);
            }
        } catch (IOException e) {
            recieve.setText(e.getMessage().toString() + "\n" + recieve.getText() + "\n");
        }
    }
}