package com.evp.payment.ksher.utils.transactions;


import com.evp.payment.ksher.R;
import com.evp.payment.ksher.utils.StringUtils;

public enum ETransType {
    SALE(R.string.sale),
    VOID(R.string.voided) {
    },
    REFUND(R.string.refund) {
//    },
//    PRE_VOID("0200", "0400", "020000", R.string.pre_void, true) {
//        @Override
//        public PackIso8583 getPackager() {
//            return new PackDolfinPreVoid();
//        }
//
//        @Override
//        public PackIso8583 getReversePackager() {
//            return new PackDolfinPreVoidReverse();
//        }
//    },
//    VOID_ADVICE("0220", "", "020000", R.string.void_advice, false) {
//        @Override
//        public PackIso8583 getPackager() {
//            return new PackDolfinVoidAdvice();
//        }
//
//        @Override
//        public PackIso8583 getReversePackager() {
//            return null;
//        }
//    },
//    SETTLE("0500", "", "920000", R.string.settlement, false) {
//        @Override
//        public PackIso8583 getPackager() {
//            return new PackSettle();
//        }
//
//        @Override
//        public PackIso8583 getReversePackager() {
//            return null;
//        }
//    },
//    SETTLE_TRAILER("0500", "", "960000", R.string.settlement, false) {
//        @Override
//        public PackIso8583 getPackager() {
//            return new PackSettle();
//        }
//
//        @Override
//        public PackIso8583 getReversePackager() {
//            return null;
//        }
//    },
//    BATCH_UP("0320", "", "", R.string.batch_upload, false) {
//        @Override
//        public PackIso8583 getPackager() {
//            return new PackUpload();
//        }
//
//        @Override
//        public PackIso8583 getReversePackager() {
//            return null;
//        }
//    },
//    ECHO_TEST("0800", "", "990000", R.string.communication_test, false) {
//        @Override
//        public PackIso8583 getPackager() {
//            return new PackEchoTest();
//        }
//
//        @Override
//        public PackIso8583 getReversePackager() {
//            return null;
//        }
    };

    /**
     * Transaction message type
     */
    private String msgType;
    /**
     * Reversal message type
     */
    private String reverseMsgType;
    /**
     * Processing code 3 field
     */
    private String processingCode;
    /**
     * Transaction name resId
     */
    private int transNameResId;
    /**
     * Whether needs to reverse
     */
    private boolean isReverseSend;

    ETransType(int transNameResId) {
        this.transNameResId = transNameResId;
    }

    public String getMsgType() {
        return msgType;
    }

    public String getReverseMsgType() {
        return reverseMsgType;
    }

    public String getProcessingCode() {
        return processingCode;
    }

    public int getTransNameResId() {
        return transNameResId;
    }

    public boolean isReverseSend() {
        return isReverseSend;
    }

    public String getTransName() {
        return StringUtils.INSTANCE.getString(this.transNameResId);
    }

}
