package com.example.keyadministrator.util;

import android.widget.EditText;

import com.gg.reader.api.utils.BitBuffer;
import com.gg.reader.api.utils.HexUtils;

public class ComputedPc {
    //写入EPC长度
    public static int getEPCLength(EditText value) {
        String taEPCDataText = value.getText().toString().trim();
        int iLength = 0;
        if (taEPCDataText.length() % 4 == 0) {
            iLength = taEPCDataText.length() / 4;
        } else if (taEPCDataText.length() % 4 > 0) {
            iLength = taEPCDataText.length() / 4 + 1;
        }
        return iLength;
    }

    //PC值
    public static String getPc(int pcLen) {
        int iPc = pcLen << 11;
        BitBuffer buffer = BitBuffer.allocateDynamic();
        buffer.put(iPc);
        buffer.position(16);
        byte[] bTmp = new byte[2];
        buffer.get(bTmp);
        return HexUtils.bytes2HexString(bTmp);
    }

    //PC值 0100 0200
    public static String getGbPc(int pcLen) {
        int iPc = pcLen << 8;
        BitBuffer buffer = BitBuffer.allocateDynamic();
        buffer.put(iPc);
        buffer.position(16);
        byte[] bTmp = new byte[2];
        buffer.get(bTmp);
        return HexUtils.bytes2HexString(bTmp);
    }
}
