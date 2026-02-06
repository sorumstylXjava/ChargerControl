package com.chargercontrol.utils;

import java.io.DataOutputStream;

public class SystemTweaks {
    public static boolean resetBatteryStats() {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("rm /data/system/batterystats.bin\n");
            os.writeBytes("exit\n");
            os.flush();
            p.waitFor();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
