package com.evp.payment.ksher.utils.tlv;

import com.evp.payment.ksher.utils.BytesUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class TLVDataListV2 {
    private List<TLVDataV2> data = new ArrayList<TLVDataV2>();

    public static TLVDataListV2 fromBinary(byte[] data) {
        TLVDataListV2 l = new TLVDataListV2();
        int offset = 0;
        while (offset < data.length) {
            TLVDataV2 d = TLVDataV2.fromRawData(data, offset);
            if (d == null) {
                break;
            } else {
                l.addTLV(d);
                offset += d.getRawData().length;
            }
        }
        return l;
    }

    public static TLVDataListV2 fromBinary(String data) {
        return fromBinary(BytesUtil.hexString2ByteArray(data));
    }

    public int size() {
        return data.size();
    }

    public byte[] toBinary() {
        byte[][] allData = new byte[data.size()][];
        for (int i = 0; i < data.size(); i++) {
            allData[i] = data.get(i).getRawData();
        }
        return BytesUtil.merge(allData);
    }

    public boolean contains(String tag) {
        return null != getTLV(tag);
    }

    public TLVDataV2 getTLV(String tag) {
        for (TLVDataV2 d : data) {
            if (d.getTag().equals(tag)) {
                return d;
            }
        }
        return null;
    }

    public TLVDataListV2 getTLVs(String... tags) {
        TLVDataListV2 list = new TLVDataListV2();
        for (String tag : tags) {
            TLVDataV2 data = getTLV(tag);
            if (data != null) {
                list.addTLV(data);
            }
        }
        if (list.size() == 0) {
            return null;
        }
        return list;
    }

    public TLVDataV2 getTLV(int index) {
        return data.get(index);
    }

    public void addTLV(TLVDataV2 tlv) {
        if (tlv.isValid()) {
            data.add(tlv);
        } else
            throw new IllegalArgumentException("tlv is not valid!");
    }

    public void retainAll(String... tags) {
        List<String> tagList = Arrays.asList(tags);
        for (int i = 0; i < data.size(); ) {
            if (!tagList.contains(data.get(i).getTag())) {
                data.remove(i);
            } else {
                i++;
            }
        }
    }

    public void remove(String tag) {
        for (int i = 0; i < data.size(); ) {
            if (tag.equals(data.get(i).getTag())) {
                data.remove(i);
            } else {
                i++;
            }
        }
    }

    public void removeAll(String... tags) {
        List<String> tagList = Arrays.asList(tags);
        for (int i = 0; i < data.size(); ) {
            if (tagList.contains(data.get(i).getTag())) {
                data.remove(i);
            } else {
                i++;
            }
        }
    }

    public List<TLVDataV2> asList() {
        return this.data;
    }

    public void addTLVList(TLVDataListV2 tlvs) {
        Iterator iterator = tlvs.asList().iterator();

        while (iterator.hasNext()) {
            TLVDataV2 tlv = (TLVDataV2) iterator.next();
            this.addTLV(tlv);
        }
    }

    @Override
    public String toString() {
        if (data.isEmpty()) {
            return super.toString();
        }
        return BytesUtil.byteArray2HexString(toBinary());
    }
}