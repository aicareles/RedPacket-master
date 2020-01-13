package com.cxk.redpacket;

public class Config {

    public static long delay = 10L;
    public static String replayMsg = "谢谢老板"+new String(Character.toChars(Integer.parseInt("1F601", 16)));
    public static boolean replayEnable = true;



    public static final String KEY_REDPACKET_LIST = "key_redpacket_list";
    public static final String KEY_REPLAY_ENABLE = "key_replay_enable";
    public static final String KEY_REPLAY_TEXT = "key_replay_text";

    public static final String VERSION_UPDATE = "https://raw.githubusercontent.com/aicareles/RedPacket-master/master/app/release/update.json";
    public static final String DOWNLOAD_URL = "https://raw.githubusercontent.com/aicareles/RedPacket-master/master/app/release/Red-Packet.apk";

}
