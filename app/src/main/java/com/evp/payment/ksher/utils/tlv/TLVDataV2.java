package com.evp.payment.ksher.utils.tlv;

import com.evp.payment.ksher.utils.BytesUtil;
import com.evp.eos.utils.LogUtil;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class TLVDataV2 {
    private static final String TAG = "TLVDataV2";

    private byte[] data;
    private String tag;
    private int length = -1;
    private byte[] value;

    private TLVDataV2() {

    }

    public static TLVDataV2 fromRawData(byte[] tlData, int tlOffset, byte[] vData, int vOffset) {
        try {
            int tLen = getTLength(tlData, tlOffset);
            int lLen = getLLength(tlData, tlOffset + tLen);
            int vLen = calcValueLength(tlData, tlOffset + tLen, lLen);

            TLVDataV2 d = new TLVDataV2();
            d.data = BytesUtil.merge(BytesUtil.subBytes(tlData, tlOffset, tLen + lLen), BytesUtil.subBytes(vData, vOffset, vLen));
            d.getTag();
            d.getLength();
            d.getBytesValue();

            return d;
        } catch (Exception e) {
            LogUtil.e(TAG, e);
            return null;
        }
    }

    public static TLVDataV2 fromData(String tagName, byte[] value) {
        byte[] tag = BytesUtil.hexString2ByteArray(tagName);
        TLVDataV2 d = new TLVDataV2();
        d.data = BytesUtil.merge(tag, makeLengthData(value.length), value);
        d.tag = tagName;
        d.length = value.length;
        d.value = value;
        return d;
    }

    public static TLVDataV2 fromRawData(byte[] data, int offset) {
        try {
            int len = getDataLength(data, offset);
            TLVDataV2 d = new TLVDataV2();
            d.data = BytesUtil.subBytes(data, offset, len);
            d.getTag();
            d.getLength();
            d.getBytesValue();
            return d;
        } catch (Exception e) {
            LogUtil.e(TAG, e);
            return null;
        }
    }

    private static int getTLength(byte[] data, int offset) {
        if ((data[offset] & 0x1F) == 0x1F) {
            if ((data[offset + 1] & 0x80) == 0x80) {
                return 3;
            }
            return 2;
        }
        return 1;
    }

    private static int getLLength(byte[] data, int offset) {
        if ((data[offset] & 0x80) == 0) {
            return 1;
        }
        return (data[offset] & 0x7F) + 1;
    }

    private static int getDataLength(byte[] data, int offset) {
        int tLen = getTLength(data, offset);
        int lLen = getLLength(data, offset + tLen);
        int vLen = calcValueLength(data, offset + tLen, lLen);
        return tLen + lLen + vLen;
    }

    private static int calcValueLength(byte[] l, int offset, int lLen) {
        if (lLen == 1) {
            return l[offset];
        }

        int vLen = 0;
        for (int i = 1; i < lLen; i++) {
            vLen <<= 8;
            vLen |= (l[offset + i]) & 0xff;
        }
        return vLen;
    }

    private static byte[] makeLengthData(int len) {
        if (len > 127) {
            byte[] lenData = new byte[4];
            int validIndex = -1;
            for (int i = 0; i < lenData.length; i++) {
                lenData[i] = (byte) ((len >> (8 * (3 - i))) & 0xFF);
                if (lenData[i] != 0 && validIndex < 0) {
                    validIndex = i;
                }
            }

            lenData = BytesUtil.subBytes(lenData, validIndex, -1);
            lenData = BytesUtil.merge(new byte[]{(byte) (0x80 | lenData.length)}, lenData);
            return lenData;
        } else {
            return new byte[]{(byte) len};
        }
    }

    public String getTag() {
        if (tag != null) {
            return tag;
        }
        int tLen = getTLength(data, 0);
        return tag = BytesUtil.byteArray2HexString(BytesUtil.subBytes(data, 0, tLen));
    }

    public int getLength() {
        if (length > -1) {
            return length;
        }
        int offset = getTLength(data, 0);
        int l = getLLength(data, offset);
        if (l == 1) {
            return data[offset];
        }

        int afterLen = 0;
        for (int i = 1; i < l; i++) {
            afterLen <<= 8;
            afterLen |= (data[offset + i]) & 0xff;
        }
        return length = afterLen;
    }

    public int getTLLength() {
        if (data == null) {
            return -1;
        }
        return data.length - getBytesValue().length;
    }

    public String getValue() {
        byte[] result = getBytesValue();
        if (result == null) {
            return null;
        }
        return BytesUtil.byteArray2HexString(result);
    }

    public byte getByteValue() {
        return getBytesValue()[0];
    }

    public String getGBKValue() {
        try {
            byte[] result = getBytesValue();
            if (result == null) {
                return null;
            }
            return new String(result, "GBK");
        } catch (UnsupportedEncodingException e) {
            LogUtil.e(TAG, e);
        }
        return null;
    }

    public String getNumberValue() {
        String num = getValue();
        if (num == null) {
            return null;
        }
        return String.valueOf(Long.parseLong(num));
    }

    public byte[] getGBKNumberValue() {
        try {
            String result = getNumberValue();
            if (result == null) {
                return null;
            }
            return result.getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
        }
        return null;
    }

    public byte[] getBCDValue() {
        String result = getGBKValue();
        if (result == null) {
            return null;
        }
        return BytesUtil.hexString2ByteArray(result);
    }

    public byte[] getRawData() {
        return data;
    }

    public byte[] getBytesValue() {
        if (value != null) {
            return value;
        }
        int l = getLength();
        return value = BytesUtil.subBytes(data, data.length - l, l);
    }

    public int getIntValue() {
        return BytesUtil.bytes2Int(getBytesValue());
    }

    public boolean isValid() {
        return data != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof TLVDataV2)) {
            return false;
        }

        if (data == null || ((TLVDataV2) obj).data == null) {
            return false;
        }

        return Arrays.equals(data, ((TLVDataV2) obj).data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public String toString() {
        if (data == null) {
            return super.toString();
        }
        return BytesUtil.byteArray2HexString(data);
    }
}
