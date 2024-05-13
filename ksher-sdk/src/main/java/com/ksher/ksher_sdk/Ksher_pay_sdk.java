package com.ksher.ksher_sdk;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/***
 * 需要pkcs8格式的可以调用命令行转换:
 * openssl pkcs8 -topk8 -inform PEM -in private.key -outform pem -nocrypt -out pkcs8.pem
 * 1、PKCS1私钥生成
 * openssl genrsa -out private.pem 1024
 * 2、PKCS1私钥转换为PKCS8(该格式一般Java调用)
 * openssl pkcs8 -topk8 -inform PEM -in private.pem -outform pem -nocrypt -out pkcs8.pem
 */
public class Ksher_pay_sdk {

    private String appid;
    private String privateKey;
    private String PayDomain;
    private String GateDomain;
    private String publicKey;
    private String commuMode;
//    private String PayDomain = "https://api.mch.ksher.net/KsherPay";
//    private String GateDomain = "https://gateway.ksher.com/api";
    //定义加密方式
    private final String KEY_RSA = "RSA";
    //定义签名算法
    private final String KEY_RSA_SIGNATURE = "MD5withRSA";
    private final java.text.SimpleDateFormat timeStampFormat = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
//    private final String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAL7955OCuN4I8eYNL/mixZWIXIgCvIVEivlxqdpiHPcOLdQ2RPSx/pORpsUu/E9wz0mYS2PY7hNc2mBgBOQT+wUCAwEAAQ==";

    public Ksher_pay_sdk(String appid, String privateKey, String payDomain, String gateDomain, String publicKey, String commuMode) {
        this.appid = appid;
        this.privateKey = privateKey;
        PayDomain = payDomain;
        GateDomain = gateDomain;
        this.publicKey = publicKey;
        this.commuMode = commuMode;
    }

    public void UpdateAppId(String appid, String privateKey) {
        this.appid = appid;
        this.privateKey = privateKey;
    }

    /**
     * sign byte to hex
     *
     * @param bytes
     * @return
     */
    public String bytesToHex(byte[] bytes) {
        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) { // 使用String的format方法进行转换
            buf.append(String.format("%02x", new Integer(b & 0xff)));
        }
        return buf.toString();
    }

    /**
     * hex string to byte
     *
     * @param sign
     * @return
     */
    public byte[] unHexVerify(String sign) {
        try {
            int length = sign.length();
            byte[] result = new byte[length / 2];
            for (int i = 0; i < length; i += 2)
                result[i / 2] = (byte) ((Character.digit(sign.charAt(i), 16) << 4) + Character.digit(sign.charAt(i + 1), 16));
            return result;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return new byte[0];
    }

    /**
     * 请求参数排序
     *
     * @param params
     * @return
     */
    public byte[] getParamsSort(Map params) {
        TreeMap<String, String> sortParas = new TreeMap<String, String>();
        sortParas.putAll(params);
        Iterator<String> it = sortParas.keySet().iterator();
        StringBuilder encryptedStr = new StringBuilder();
        while (it.hasNext()) {
            String key = it.next();
            encryptedStr.append(key).append("=").append(params.get(key));
        }
        return encryptedStr.toString().getBytes();
    }

    /**
     * 签名
     *
     * @param params
     * @return
     */
    public String KsherSign(Map params) throws Exception {
        //将私钥加密数据字符串转换为字节数组
        byte[] data = getParamsSort(params);
        // 解密由base64编码的私钥
        byte[] privateKeyBytes = Base64.decodeBase64(this.privateKey.getBytes());
        // 构造PKCS8EncodedKeySpec对象
        PKCS8EncodedKeySpec pkcs = new PKCS8EncodedKeySpec(privateKeyBytes);
        // 指定的加密算法
        KeyFactory factory = KeyFactory.getInstance(KEY_RSA);
        // 取私钥对象
        PrivateKey key = factory.generatePrivate(pkcs);
        // 用私钥对信息生成数字签名
        Signature signature = Signature.getInstance(KEY_RSA_SIGNATURE);
        signature.initSign(key);
        signature.update(data);
        byte[] sign_byte = signature.sign();
        //String sing_str = new String(Base64.encodeBase64(signature.sign()));
        return bytesToHex(sign_byte);
    }

    /**
     * 校验数字签名
     *
     * @param data
     * @param sign
     * @return 校验成功返回true，失败返回false
     */
    public boolean KsherVerify(Map data, String sign) throws Exception {
        boolean flag = false;
        //将私钥加密数据字符串转换为字节数组
        byte[] dataByte = getParamsSort(data);
        // 解密由base64编码的公钥
        byte[] publicKeyBytes = Base64.decodeBase64(publicKey.getBytes());
        // 构造X509EncodedKeySpec对象
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        // 指定的加密算法
        KeyFactory factory = KeyFactory.getInstance(KEY_RSA);
        // 取公钥对象
        PublicKey key = factory.generatePublic(keySpec);
        // 用公钥验证数字签名
        Signature signature = Signature.getInstance(KEY_RSA_SIGNATURE);
        signature.initVerify(key);
        signature.update(dataByte);
        return signature.verify(unHexVerify(sign));
    }

    /**
     * post请求(用于key-value格式的参数)
     *
     * @param url
     * @param params
     * @return
     */
    public String KsherPost(String url, Map params) throws Exception {
        try {
                HttpParams httpParameters = new BasicHttpParams();
                int timeoutConnection = 30000;
                HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
                int timeoutSocket = 30000;
                HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
                HttpClient client = new DefaultHttpClient(httpParameters);
                HttpPost post = new HttpPost(url);
                //设置公共参数
                params.put("appid", this.appid);
                params.put("nonce_str", RandomStringUtils.randomAlphanumeric(4));
                params.put("time_stamp", timeStampFormat.format(new Date()));

                List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                for (Iterator iter = params.keySet().iterator(); iter.hasNext(); ) {
                    String name = (String) iter.next();
                    String value = String.valueOf(params.get(name));
                    urlParameters.add(new BasicNameValuePair(name, value));
                }
                Log.d(Ksher_pay_sdk.class.getSimpleName(), "----------------START KsherPost----------------");
                Log.d(Ksher_pay_sdk.class.getSimpleName(), "KsherPost REQUEST URL : " + url);
                Log.d(Ksher_pay_sdk.class.getSimpleName(), "KsherPost REQUEST PARAMS : " + params);
                String sign = KsherSign(params);
                urlParameters.add(new BasicNameValuePair("sign", sign));
                post.setEntity(new UrlEncodedFormEntity(urlParameters));
                HttpResponse response = client.execute(post);
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuffer result = new StringBuffer();
                String line = "";
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                rd.close();
                System.out.println(result.toString());

                JSONObject json = JSONObject.parseObject(result.toString());
                boolean isVerify = KsherVerify(json.getJSONObject("data"), json.getString("sign"));
                if (isVerify) {
                    Log.d(Ksher_pay_sdk.class.getSimpleName(), "KsherPost RESPONSE : " + result.toString());
                    Log.d(Ksher_pay_sdk.class.getSimpleName(), "----------------KsherPost END----------------");
                    return result.toString();
                } else {
                    return result.toString();
//            throw new Exception("verify signature failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    /**
     * 商户扫用户(B扫C)
     *
     * @param mchOrderNo 商户订单号
     * @param feeType    支付币种 'THB'
     * @param authCode   支付条码
     * @param channel    支付通道 wechat aplipay
     * @param operatorId 操作员编号
     * @param totalFee   支付金额
     * @return
     */
    public String QuickPay(String mchOrderNo, String feeType, String authCode, String channel, String operatorId, Integer totalFee) {
        try {
            Map<String, String> paras = new HashMap<String, String>();
            paras.put("mch_order_no", mchOrderNo);
            paras.put("total_fee", totalFee.toString());
            paras.put("fee_type", feeType);
            paras.put("auth_code", authCode);
            paras.put("channel", channel);
            paras.put("operator_id", operatorId);
            if("demo".equalsIgnoreCase(commuMode)){
                return mockQuickPay();
            }else {
                return KsherPost(PayDomain + "/quick_pay", paras);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return ex.getMessage();
        }
    }

    /**
     * C扫B支付
     * 必传参数
     * mch_order_no
     * total_fee
     * fee_type
     * channel
     * 选传参数
     * redirect_url
     * notify_url
     * paypage_title
     * operator_id
     *
     * @return
     */
    public String JsApiPay(String mchOrderNo, String feeType, String channel, Integer totalFee) {
        try {
            Map<String, String> paras = new HashMap<String, String>();
            paras.put("mch_order_no", mchOrderNo);
            paras.put("total_fee", totalFee.toString());
            paras.put("fee_type", feeType);
            paras.put("channel", channel);
            return KsherPost(PayDomain + "/jsapi_pay", paras);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ex.getMessage();
        }
    }

    /**
     * 动态码支付
     * :param kwargs:
     * 必传参数
     * mch_order_no
     * total_fee
     * fee_type
     * channel
     * 选传参数
     * redirect_url
     * notify_url
     * paypage_title
     * product
     * attach
     * operator_id
     * device_id
     * img_type
     * :return:
     **/
    public String NativePay(String mchOrderNo, String feeType, String channel, Long totalFee) throws Exception {
        Map<String, String> paras = new HashMap<String, String>();
        paras.put("mch_order_no", mchOrderNo);
        paras.put("total_fee", totalFee.toString());
        paras.put("fee_type", feeType);
        paras.put("channel", channel);
        Log.d("NativePay", "request : " + paras);
        if("demo".equalsIgnoreCase(commuMode)){
            return mockNativePay();
        }else {
            return KsherPost(PayDomain + "/native_pay", paras);
        }
    }

    public String NativePayForTrueMoney(String mchOrderNo, String feeType, String channel, Long totalFee, String timeout) throws Exception {
        Map<String, String> paras = new HashMap<String, String>();
        paras.put("mch_order_no", mchOrderNo);
        paras.put("total_fee", totalFee.toString());
        paras.put("fee_type", feeType);
        paras.put("channel", channel);
        paras.put("tmn_expire_time", timeout);
        Log.d("NativePay", "request : " + paras);
        if("demo".equalsIgnoreCase(commuMode)){
            return mockNativePay();
        }else {
            return KsherPost(PayDomain + "/native_pay", paras);
        }
    }

    public String NativePayForPromptPay(String mchOrderNo, String feeType, String channel, Long totalFee, String timeout) throws Exception {
        Map<String, String> paras = new HashMap<String, String>();
        paras.put("mch_order_no", mchOrderNo);
        paras.put("total_fee", totalFee.toString());
        paras.put("fee_type", feeType);
        paras.put("channel", channel);
        paras.put("expire_time", timeout);
        Log.d("NativePay", "request : " + paras);
        if("demo".equalsIgnoreCase(commuMode)){
            return mockNativePay();
        }else {
            return KsherPost(PayDomain + "/native_pay", paras);
        }
    }

    /**
     * 小程序支付
     * :param kwargs:
     * 必传参数
     * mch_order_no
     * total_fee
     * fee_type
     * channel
     * sub_openid
     * channel_sub_appid
     * 选传参数
     * redirect_url
     * notify_url
     * paypage_title
     * product
     * operator_id
     * :return:
     **/
    public String MiniproPay(String mchOrderNo, String feeType, String channel, String subOpenid, String channelSubAppId, Integer totalFee) throws Exception {
        Map<String, String> paras = new HashMap<String, String>();
        paras.put("mch_order_no", mchOrderNo);
        paras.put("total_fee", totalFee.toString());
        paras.put("fee_type", feeType);
        paras.put("channel", channel);
        paras.put("sub_openid", subOpenid);
        paras.put("channel_sub_appid", channelSubAppId);
        return KsherPost(PayDomain + "/mini_program_pay", paras);
    }

    /**
     * app支付
     * :param kwargs:
     * 必传参数
     * mch_order_no
     * total_fee
     * fee_type
     * channel
     * sub_openid
     * channel_sub_appid
     * 选传参数
     * redirect_url
     * notify_url
     * paypage_title
     * product
     * attach
     * operator_id
     * refer_url 仅当channel为alipay时需要
     * :return:
     **/
    public String AppPay(String mchOrderNo, String feeType, String channel, String subOpenid, String channelSubAppId, Integer totalFee) throws Exception {
        Map<String, String> paras = new HashMap<String, String>();
        paras.put("mch_order_no", mchOrderNo);
        paras.put("total_fee", totalFee.toString());
        paras.put("fee_type", feeType);
        paras.put("channel", channel);
        paras.put("sub_openid", subOpenid);
        paras.put("channel_sub_appid", channelSubAppId);
        return KsherPost(PayDomain + "/app_pay", paras);
    }

    /**
     * H5支付，仅支持channel=alipay
     * :param kwargs:
     * 必传参数
     * mch_order_no
     * total_fee
     * fee_type
     * channel
     * 选传参数
     * redirect_url
     * notify_url
     * paypage_title
     * product
     * attach
     * operator_id
     * device_id
     * refer_url
     * :return:
     **/
    public String WapPay(String mchOrderNo, String feeType, String channel, Integer totalFee) throws Exception {
        Map<String, String> paras = new HashMap<String, String>();
        paras.put("mch_order_no", mchOrderNo);
        paras.put("total_fee", totalFee.toString());
        paras.put("fee_type", feeType);
        paras.put("channel", channel);
        return KsherPost(PayDomain + "/wap_pay", paras);
    }

    /**
     * PC网站支付，仅支持channel=alipay
     * :param kwargs:
     * 必传参数
     * mch_order_no
     * total_fee
     * fee_type
     * channel
     * 选传参数
     * redirect_url
     * notify_url
     * paypage_title
     * product
     * attach
     * operator_id
     * device_id
     * refer_url
     * :return:
     **/
    public String WepPay(String mchOrderNo, String feeType, String channel, Integer totalFee) throws Exception {
        Map<String, String> paras = new HashMap<String, String>();
        paras.put("mch_order_no", mchOrderNo);
        paras.put("total_fee", totalFee.toString());
        paras.put("fee_type", feeType);
        paras.put("channel", channel);
        return KsherPost(PayDomain + "/wap_pay", paras);
    }

    /**
     * 订单查询
     * :param kwargs:
     * 必传参数
     * mch_order_no、ksher_order_no、channel_order_no三选一
     * :return:
     **/
    public String OrderQuery(String mchOrderNo) throws Exception {
        Map<String, String> paras = new HashMap<String, String>();
        paras.put("mch_order_no", mchOrderNo);
        if("demo".equalsIgnoreCase(commuMode)){
            return mockOrderQuery();
        }else {
            return KsherPost(PayDomain + "/order_query", paras);
        }
    }

    /**
     * 订单关闭
     * :param kwargs:
     * 必传参数
     * mch_order_no、ksher_order_no、channel_order_no三选一
     * 选传参数
     * operator_id
     * :return:
     **/
    public String OrderClose(String mchOrderNo) throws Exception {
        Map<String, String> paras = new HashMap<String, String>();
        paras.put("mch_order_no", mchOrderNo);
        if("demo".equalsIgnoreCase(commuMode)){
            return mockOrderClose();
        }else {
            return KsherPost(PayDomain + "/order_close", paras);
        }
    }

    /**
     * 订单撤销
     * :param kwargs:
     * 必传参数
     * mch_order_no、ksher_order_no、channel_order_no三选一
     * 选传参数
     * operator_id
     * :return:
     **/
    public String OrderReverse(String mchOrderNo) throws Exception {
        Map<String, String> paras = new HashMap<String, String>();
        paras.put("mch_order_no", mchOrderNo);
        return KsherPost(PayDomain + "/order_reverse", paras);
    }

    /**
     * 订单退款
     * :param kwargs:
     * 必传参数
     * total_fee
     * fee_type
     * refund_fee
     * mch_refund_no
     * mch_order_no、ksher_order_no、channel_order_no三选一
     * 选传参数
     * operator_id
     * :return:
     **/
    public String OrderRefund(String mchRefundNo, String feeType, String mchOrderNo, Integer refundFee, Integer totalFee) throws Exception {
        Map<String, String> paras = new HashMap<String, String>();
        paras.put("mch_refund_no", mchRefundNo);
        paras.put("fee_type", feeType);
        paras.put("mch_order_no", mchOrderNo);
        paras.put("refund_fee", refundFee.toString());
        paras.put("total_fee", totalFee.toString());
        if("demo".equalsIgnoreCase(commuMode)){
            return mockOrderRefund();
        }else {
            return KsherPost(PayDomain + "/order_refund", paras);
        }
    }

    /**
     * 退款查询
     * :param kwargs:
     * 必传参数
     * mch_refund_no、ksher_refund_no、channel_refund_no三选一
     * mch_order_no、ksher_order_no、channel_order_no三选一
     **/
    public String RefundQuery(String mchRefundNo, String mchOrderNo) throws Exception {
        Map<String, String> paras = new HashMap<String, String>();
        paras.put("mch_refund_no", mchRefundNo);
        paras.put("mch_order_no", mchOrderNo);
        return KsherPost(PayDomain + "/refund_query", paras);
    }

    /**
     * 汇率查询
     * :param kwargs:
     * 必传参数
     * channel
     * fee_type
     * date
     * :return:
     **/
    public String RateQuery(String channel, String feeType, String date) throws Exception {
        Map<String, String> paras = new HashMap<String, String>();
        paras.put("channel", channel);
        paras.put("fee_type", feeType);
        paras.put("date", date);
        return KsherPost(PayDomain + "/rate_query", paras);
    }

    /**
     * 聚合支付商户查询订单支付状态
     * :param kwargs:
     * 必传参数
     * mch_order_no
     * :return:
     **/
    public String GatewayOrderQuery(String mch_order_no) throws Exception {
        Map<String, String> paras = new HashMap<String, String>();
        paras.put("mch_order_no", mch_order_no);
        if("demo".equalsIgnoreCase(commuMode)){
            return mockGatewayOrderQuery();
        }else {
            return KsherPost(GateDomain + "/gateway_order_query", paras);
        }
    }

    /**
     * 聚合支付商户通过API提交数据
     * :param kwargs:
     * 必传参数
     * mch_order_no: 商户订单号 str
     * total_fee: 金额(分) int
     * fee_type: 货币种类 str
     * channel_list: 支付通道 str
     * mch_code: 商户订单code str
     * mch_redirect_url: 商户通知url str
     * mch_redirect_url_fail: 失败回调网址 str
     * product_name: 商品描述 str
     * refer_url: 商家refer str
     * device: 设备名称(PC or H5) str
     * 选传参数
     * color: 横幅颜色 str
     * background: 横幅背景图片 str
     * payment_color: 支付按钮颜色 str
     * ksher_explain: 最下方文案 str
     * hide_explain: 是否显示最下方文案(1显示 0不显示) int
     * expire_time: 订单过期时间(min) int
     * hide_exp_time: 是否显示过期时间(1显示 0不显示) int
     * logo: 横幅logo str
     * lang: 语言(en,cn,th) str
     * shop_name: logo旁文案 str
     * attach: 商户附加信息 str
     * :return:
     * {'pay_content': 'https://gateway.ksher.com/mindex?order_uuid=订单uuid'}
     **/
    public String GatewayPay(String mch_order_no, String fee_type, String channel_list, String mch_code, String mch_redirect_url,
                             String mch_redirect_url_fail, String product_name, String refer_url, String device, Integer total_fee) throws Exception {
        Map<String, String> paras = new HashMap<String, String>();
        paras.put("mch_order_no", mch_order_no);
        paras.put("fee_type", fee_type);
        paras.put("channel_list", channel_list);
        paras.put("mch_code", mch_code);
        paras.put("mch_redirect_url", mch_redirect_url);
        paras.put("mch_redirect_url_fail", mch_redirect_url_fail);
        paras.put("product_name", product_name);
        paras.put("refer_url", refer_url);
        paras.put("device", device);
        paras.put("total_fee", total_fee.toString());
        return KsherPost(GateDomain + "/gateway_pay", paras);
    }

    public String mockNativePay(){
        return "{\n" +
                "    \"code\": 0,\n" +
                "    \"data\": {\n" +
                "        \"appid\": \"mch20163\",\n" +
                "        \"code_url\": \"weixin://wxpay/bizpayurl?pr=n8F22H4\",\n" +
                "        \"device_id\": \"\",\n" +
                "        \"imgdat\": \"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAXIAAAFyAQAAAADAX2ykAAACoElEQVR4nO2aTWrkMBBGX40FWaohB+ijqG82zJFyA/soOcCAtWyQ+WYhqdOdMJMOOKY9VC0MRm/xQVE/KpWJr9j040s4OO+8884777zzf+OtWaB/zJgOALmfnTbU4/zKfJIkzbdn2QwYJEm65b9bj/Mr87lHaJqX6lXSDHaCGtMb63F+HT68+9d0LIH0cjCIAvK2epxfl3/vX0szYjqWILLx4Xb8aPqd/7d1//ZYbQ7NTzKisPTCjZMfTb/zd/GTmZkdwE45YD9nIL0GgKW2z9vqcX4lvsbvW4RqOhZEXkzkpRfh7fQ4vy5Pvfwklfaf+l1II4MgqnbS9Qo1Ppp+5+/hNeYnaYwS5ABpHgTxbNK8GMSCxg31OL8ub3Y8Wx1oTMcCsFh1chtiPfl8Y8+8RsBOsSC9tl7K7AA1nKfj2eeT++Rr/dUYr+eTNRcnlZqpNcbSuUfT7/wn1vwGtDIbJUmlN1T1QPL+apf8pX+WaqxqBtIM0kx1dzt1/+6Qb/ffyUBwNhFL0HQASy9DMQhFk134R9Pv/CfW629BmodLai43hfliHr8746/yc7dy5fM+7hjcv/vkm3/fxlTE3lVdTa28v9or39+PcihMRwEsQcTfQeShWCu9lwL8aPqdv4+PZ4McMDsMshOX+Qb0ceWmepxfk79qrcgBYJDGeLZWf+PZfP68Y/5tf7K2ziNLz8jd3ZvqcX4tvtXfNALkZ4AlQBxkRIBYnxuCttHj/Pfwl/3J9la0tIQ89U3odvqo+p2/l4/lspozqM6k5f3V/8Lr1wHaI3DuL4Wn7PtXO+Xf709WS+OCpfm5QA7Um/A2epxfl2/+bWOMAUuvobRkHM8GDKUm6W30OL8u/2F/Egq0C9HwYb394fQ777zzzjvv/B75P230m3hnLMGVAAAAAElFTkSuQmCC\",\n" +
                "        \"ksher_order_no\": \"60020170526123947574894\",\n" +
                "        \"mch_order_no\": \"1495773587\",\n" +
                "        \"nonce_str\": \"sQ8gfSpeeV5Ld8ulW9q7JxUnXSOiZ90Y\",\n" +
                "        \"result\": \"SUCCESS\",\n" +
                "        \"trade_type\": \"NATIVE\"\n" +
                "    },\n" +
                "    \"msg\": \"ok\",\n" +
                "    \"sign\": \"65126acc40a48761751eb9fbabf41ca6d08a44be2a175edcaa95cc7e119c224d76d82d5776be6f80ce9a4e535469a7514d318faf5619151266532557ac0f8bd2\",\n" +
                "    \"status_code\": \"\",\n" +
                "    \"status_msg\": \"\",\n" +
                "    \"time_stamp\": \"\",\n" +
                "    \"version\": \"2.0.0\"\n" +
                "}";
    }
    public String mockQuickPay(){
        return "{\n" +
                "    \"code\": 0,\n" +
                "    \"data\":\n" +
                "    {\n" +
                "        \"appid\":\"mch20163\",\n" +
                "        \"attach\": \"\",\n" +
                "        \"cash_fee\": 1,\n" +
                "        \"cash_fee_type\": \"CNY\",\n" +
                "        \"channel_order_no\": \"4001432001201706013662559711\",\n" +
                "        \"device_id\": \"\",\n" +
                "        \"fee_type\": \"THB\",\n" +
                "        \"ksher_order_no\": \"60020170526123947574894\",\n" +
                "        \"mch_order_no\": \"1495773587\",\n" +
                "        \"nonce_str\": \"sQ8gfSpeeV5Ld8ulW9q7JxUnXSOiZ90Y\",\n" +
                "        \"openid\": \"o5x64wG48fnZyqWOxqJl-MPSkNJ4\",\n" +
                "        \"operation\": \"QUICK-PAY\",\n" +
                "        \"operator_id\": \"\",\n" +
                "        \"rate\": \"0.200212\",\n" +
                "        \"result\": \"SUCCESS\",\n" +
                "        \"time_end\": \"2017-06-01 10:24:37\",\n" +
                "        \"total_fee\": 100\n" +
                "    },\n" +
                "    \"msg\": \"ok\",\n" +
                "    \"sign\": \"65126acc40a48761751eb9fbabf41ca6d08a44be2a175edcaa95cc7e119c224d76d82d5776be6f80ce9a4e535469a7514d318faf5619151266532557ac0f8bd2\",\n" +
                "    \"status_code\": \"\",\n" +
                "    \"status_msg\": \"\",\n" +
                "    \"time_stamp\": \"\",\n" +
                "    \"version\": \"3.0.0\"\n" +
                "}";
    }

    public String mockOrderQuery(){
        return "{\n" +
                "    \"code\": 0,\n" +
                "    \"data\": {\n" +
                "        \"appid\": \"mch32148\",\n" +
                "        \"attach\": \"\",\n" +
                "        \"cash_fee\": 7,\n" +
                "        \"cash_fee_type\": \"CNY\",\n" +
                "        \"channel\": \"wechat\",\n" +
                "        \"channel_order_no\": \"4200000607202006237838347168\",\n" +
                "        \"consumer_remark\": \"\",\n" +
                "        \"device_id\": \"\",\n" +
                "        \"fee_type\": \"THB\",\n" +
                "        \"ksher_order_no\": \"90020200623192741778187\",\n" +
                "        \"mch_order_no\": \"1592911660\",\n" +
                "        \"merchant_remark\": \"\\u6d4b\\u8bd5\",\n" +
                "        \"nonce_str\": \"f4d6aefa63f67ed4abdd83c164191b63\",\n" +
                "        \"openid\": \"o2G4c04tmsU-wsCG7jN_ORL5Vh14\",\n" +
                "        \"operation\": \"ORDER-QUERY\",\n" +
                "        \"operator_id\": \"25382\",\n" +
                "        \"rate\": \"0.223410\",\n" +
                "        \"raw_total_fee\": 1,\n" +
                "        \"result\": \"SUCCESS\",\n" +
                "        \"time_end\": \"2020-06-23 18:28:31\",\n" +
                "        \"total_fee\": 1\n" +
                "    },\n" +
                "    \"msg\": \"ok\",\n" +
                "    \"sign\": \"1ce187f310e73b26f91e76501bb5d360798d22dba67b1b6120209784ea4c6c6f0318650858a575b43c9ded8e5f2931cdecfa8e110af6ec4f93639011c97b07fe\",\n" +
                "    \"status_code\": \"\",\n" +
                "    \"status_msg\": \"\",\n" +
                "    \"time_stamp\": \"2020-06-23T19:28:34.761046+08:00\",\n" +
                "    \"version\": \"3.0.0\"\n" +
                "}";
    }

    public String mockOrderRefund(){
        return "{\n" +
                "    \"code\": 0,\n" +
                "    \"data\": {\n" +
                "        \"appid\": \"mch35005\",\n" +
                "        \"attach\": \"\",\n" +
                "        \"cash_refund_fee\": 100,\n" +
                "        \"channel\": \"airpay\",\n" +
                "        \"channel_order_no\": \"1207919130\",\n" +
                "        \"channel_refund_no\": \"1207919130\",\n" +
                "        \"device_id\": \"\",\n" +
                "        \"fee_type\": \"THB\",\n" +
                "        \"ksher_order_no\": \"90020210330180132318847\",\n" +
                "        \"ksher_refund_no\": \"90020210330180400878914\",\n" +
                "        \"mch_order_no\": \"2103301701291052\",\n" +
                "        \"mch_refund_no\": \"refund_2103301701291052\",\n" +
                "        \"nonce_str\": \"9c75d11e7572f887dbbfe374f205d5eb\",\n" +
                "        \"operator_id\": \"\",\n" +
                "        \"refund_fee\": 100,\n" +
                "        \"refund_time\": \"2021-03-30 17:04:01\",\n" +
                "        \"result\": \"SUCCESS\",\n" +
                "        \"total_fee\": 100\n" +
                "    },\n" +
                "    \"msg\": \"ok\",\n" +
                "    \"sign\": \"4e8defe365f08a66b28f40843117268c49c772807196ac0b55591f992a9dcdff5d68f8fec01f029d0922df6795b72e211a3fc2de8056e4b19dc6ceaad86894f1\",\n" +
                "    \"status_code\": \"\",\n" +
                "    \"status_msg\": \"\",\n" +
                "    \"time_stamp\": \"2021-03-30T18:04:01.525337+08:00\",\n" +
                "    \"version\": \"3.0.0\"\n" +
                "}";
    }

    public String mockOrderClose(){
        return "{\n" +
                "    \"code\": 0,\n" +
                "    \"data\": {\n" +
                "        \"appid\": \"mch36591\",\n" +
                "        \"mch_order_no\": \"test3\",\n" +
                "        \"nonce_str\": \"7a57e84495bfe7abff4ff23591516511\",\n" +
                "        \"result\": \"SUCCESS\"\n" +
                "    },\n" +
                "    \"msg\": \"ok\",\n" +
                "    \"sign\": \"61805f39c7c484d936cbf40c864b11b9fec0b93205fdbc4efe5b71c9c7dfd98152e1ebc9af5866dd54cf58008418b52ea6122e7994f1d3fe5e6d559a8b022e5f\",\n" +
                "    \"status_code\": \"\",\n" +
                "    \"status_msg\": \"\",\n" +
                "    \"time_stamp\": \"2020-12-25T17:44:54.226116+08:00\",\n" +
                "    \"version\": \"3.0.0\"\n" +
                "}";
    }

    public String mockGatewayOrderQuery(){
        return "{\n" +
                "    \"code\": 0,\n" +
                "    \"data\": {\n" +
                "        \"appid\": \"mch32625\",\n" +
                "        \"attach\": \"\",\n" +
                "        \"cash_fee\": 21,\n" +
                "        \"cash_fee_type\": \"CNY\",\n" +
                "        \"channel\": \"wechat\",\n" +
                "        \"channel_order_no\": \"4200000553202004150354623931\",\n" +
                "        \"fee_type\": \"THB\",\n" +
                "        \"pay_mch_order_no\": \"2004150948529843\",\n" +
                "        \"ksher_order_no\": \"90020200415104909426008\",\n" +
                "        \"mch_order_no\": \"77713\",\n" +
                "        \"nonce_str\": \"UHl8XNywZjdMrpsWCNy00gOYbspYzH7Z\",\n" +
                "        \"openid\": \"o2G4c04tmsU-wsCG7jN_ORL5Vh14\",\n" +
                "        \"rate\": \"0.222657\",\n" +
                "        \"result\": \"SUCCESS\",\n" +
                "        \"time_end\": \"2020-04-15 09:49:19\",\n" +
                "        \"total_fee\": 100\n" +
                "    },\n" +
                "    \"lang\": \"\",\n" +
                "    \"message\": \"操作成功\",\n" +
                "    \"msg\": \"操作成功\",\n" +
                "    \"sign\": \"a19fb42985cbf2ce81e74a1e2aa7fdaf42d33234b3f5d9a3ce9230d1cabc2796abd05c6b00951c6c33f557410d029853bf70d99af32cae858f1484412309f242\"\n" +
                "}";
    }

}
