

package com.evp.payment.ksher.parameter;

import android.text.TextUtils;

import com.pax.gl.utils.impl.Convert;


public enum Currency {
    /**
     * Chinese Yuan
     */
    RMB("156", "CNY", 2),

    /**
     * Hong Kong Dollars
     */
    HKD("344", "HKD", 2),

    /**
     * Macao Pataca
     */
    MOP("446", "MOP", 2),

    /**
     * Malaysian Ringgit
     */
    MYR("458", "MYR", 2),

    /**
     * Singapore Dollars
     */
    SGD("702", "SGD", 2),

    /**
     */
    TOP("776", "TOP", 2),

    /**
     */
    TTD("780", "TTD", 2),

    /**
     * Tunisian Dinar
     */
    TND("788", "TND", 3),

    /**
     * Uganda Shilling
     */
    UGX("800", "UGX", 2),

    /**
     * Denar
     */
    MKD("807", "MKD", 2),

    /**
     * Egytian Pound
     */
    EGP("818", "EGP", 2),

    /**
     * Tanzanian Shilling
     */
    TZS("834", "TZS", 2),

    /**
     * Peso Uruguayo
     */
    UYU("858", "UYU", 2),

    /**
     * Uzbekistan Sum
     */
    UZS("860", "UZS", 2),

    /**
     * Bolivar
     */
    VEB("862", "VEB", 2),

    /**
     * Tala
     */
    WST("882", "WST", 2),

    /**
     * 也门
     */
    YER("886", "YER", 2),

    /**
     * Serbian Dinar
     */
    CSD("891", "CSD", 2),

    /**
     * Peso Convertible
     */
    CUC("931", "CUC", 2),

    /**
     * Zimbabwe Dollar
     */
    ZWL("932", "ZWL", 2),

    /**
     * New Mana
     */
    TMT("934", "TMT", 2),

    GHS("936", "GHS", 2),

    /**
     * Sudanese Pound
     */
    SDG("938", "SDG", 2),

    /**
     * Serbian Dinar
     */
    RSD("941", "RSD", 2),

    /**
     * Metical
     */
    MZN("943", "MZN", 2),

    /**
     * Azerbaijanian Manat
     */
    AZN("944", "AZN", 2),

    /**
     * RON
     */
    RON("946", "RON", 2),

    /**
     * turkey
     */
    TRY("949", "TRY", 2),

    /**
     * CFA Franc BEAC
     */
    XAF("950", "XAF", 0),

    XCD("951", "XCD", 2),

    /**
     * CFA Franc BCEAO
     */
    XOF("952", "XOF", 0),

    /**
     * CFP Franc
     */
    XPF("953", "XPF", 0),

    /**
     * Kwacha
     */
    ZMW("967", "ZMW", 2),

    /**
     * Surinam Dollar
     */
    SRD("968", "SRD", 2),

    /**
     * Malagasy Ariary
     */
    MGA("969", "MGA", 2),

    /**
     * Unidad de Valor Real
     */
    COU("970", "COU", 2),

    /**
     * 阿富汗尼
     */
    AFN("971", "AFN", 2),

    /**
     * Somoni
     */
    TJS("972", "TJS", 2),

    /**
     * Angola Kwanza
     */
    AOA("973", "AOA", 2),

    /**
     * Belarussian Ruble
     */
    BYR("974", "BYR", 0),

    /**
     * New Bulgarian Lev
     */
    BGN("975", "BGN", 2),

    /**
     * Congolese Franc
     */
    CDF("976", "CDF", 2),

    /**
     * Convertible Mark
     */
    BAM("977", "BAM", 2),

    /**
     * Ukraine Hryvnia
     */
    UAH("980", "UAH", 2),

    /**
     * Lari
     */
    GEL("981", "GEL", 2),

    /**
     * Mvdol
     */
    BOV("984", "BOV", 2),

    /**
     * Brazilian Real
     */
    PLN("986", "PLN", 2),


    /**
     * Unidades de fomento
     */
    CLF("990", "CLF", 0),

    /**
     * Indonesia Rupiah
     */
    IDR("360", "IDR", 2),

    /**
     * Japanese Yen
     */
    JPY("392", "JPY", 0),

    /**
     * Euro
     */
    EUR("978", "EUR", 2),

    /**
     * Philippine Pesos
     */
    PHP("608", "PHP", "P", 2),

    /**
     * New Taiwanese Dollars
     */
    TWD("901", "TWD", 2),

    /**
     * US Dollars
     */
    USD("840", "USD", 2),

    /**
     * Vietnam DONG
     */
    VND("704", "VND", 0),

    /**
     * United Arab Durham
     */
    AED("784", "AED", 2),

    /**
     * Australian Dollars
     */
    AUD("036", "AUD", 2),

    /**
     * 巴哈马元
     */
    BSD("044", "BSD", 2),

    /**
     * Bahraini Dinar
     */
    BHD("048", "BHD", 3),

    BDT("050", "BDT", 2),

    AMD("051", "AMD", 2),

    BBD("052", "BBD", 2),

    /**
     * Bermudian Dollar
     */
    BMD("060", "BMD", 2),

    /**
     * Ngultrum
     */
    BTN("064", "BTN", 2),

    /**
     * 玻利维亚
     */
    BOB("068", "BOB", 2),

    /**
     * Pula
     */
    BWP("072", "BWP", 2),

    /**
     * 伯利兹
     */
    BZD("084", "BZD", 2),

    /**
     * Solomon Islands Dollar
     */
    SBD("090", "SBD", 2),

    /**
     * Brunei Dollar
     */
    BND("096", "BND", 2),

    /**
     * Kyat
     */
    MMK("104", "MMK", 2),

    /**
     * Burundi Franc
     */
    BIF("108", "BIF", 0),

    /**
     * Riel
     */
    KHR("116", "KHR", 2),

    /**
     * Cape Verde Escudo
     */
    CVE("132", "CVE", 2),

    /**
     * Canadian Dollars
     */
    CAD("124", "CAD", 2),

    /**
     * 开曼群岛
     */
    KYD("136", "KYD", 2),

    /**
     * Sri Lanka Rupee
     */
    LKR("144", "LKR", 2),

    /**
     * Chilean Peso
     */
    CLP("152", "CLP", 2),

    /**
     * Colombian Peso
     */
    COP("170", "COP", 2),

    /**
     * Comoro Franc
     */
    KMF("174", "KMF", 0),

    /**
     * 哥斯达黎加
     */
    CRC("188", "CRC", 2),

    /**
     * 克罗地亚币
     */
    HRK("191", "HRK", 2),

    /**
     * Cuban Peso
     */
    CUP("192", "CUP", 2),

    /**
     * Koruna
     */
    CZK("203", "CZK", 2),

    /**
     * 多米尼加比索
     */
    DOP("214", "DOP", 2),

    /**
     * 萨尔瓦多
     */
    SVC("222", "SVC", 2),

    /**
     * Ethiopian Birr
     */
    ETB("230", "ETB", 2),

    /**
     * Nafka
     */
    ERN("232", "ERN", 2),

    /**
     * 爱沙尼亚
     */
    EEK("233", "EEK", 2),

    /**
     * Falkland Islands Pound
     */
    FKP("238", "FKP", 2),

    /**
     * Fiji Dollar
     */
    FJD("242", "FJD", 2),

    /**
     * Djibouti Franc
     */
    DJF("262", "DJF", 0),

    /**
     * Dalasi
     */
    GMD("270", "GMD", 2),

    /**
     * GHANA CEDI
     */
    GHC("288", "GHC", 2),

    /**
     * Gibraltar Pound
     */
    GIP("292", "GIP", 2),

    /**
     * markka
     */
    FIM("318", "FIM", 2),

    /**
     * 危地马拉
     */
    GTQ("320", "GTQ", 2),

    /**
     * Guinea Franc
     */
    GNF("324", "GNF", 0),

    /**
     * Norwegian krone
     */
    NOK("326", "NOK", 2),

    /**
     * Guyana Dollar
     */
    GYD("328", "GYD", 2),

    /**
     * Gourde
     */
    HTG("332", "HTG", 2),

    /**
     * 洪图拉斯元
     */
    HNL("340", "HNL", 2),

    /**
     * Forint
     */
    HUF("348", "HUF", 2),

    /**
     * Iranian Rial
     */
    IRR("364", "IRR", 2),

    /**
     * Iraqi Dinar
     */
    IQD("368", "IQD", 3),

    /**
     * Iraqi Dinar
     */
    ILS("376", "ILS", 2),

    /**
     * Jamaican Dollar
     */
    JMD("388", "JMD", 2),

    /**
     * Tenge
     */
    KZT("398", "KZT", 2),

    /**
     * 约旦第纳尔
     */
    JOD("400", "JOD", 3),

    /**
     * Kenyan Shilling
     */
    KES("404", "KES", 2),

    /**
     * North Korean Won
     */
    KPW("408", "KPW", 2),

    /**
     * SOM
     */
    KGS("417", "KGS", 2),

    /**
     * Kip
     */
    LAK("418", "LAK", 2),

    /**
     * Lebanese Pound
     */
    LBP("422", "LBP", 2),

    /**
     * Loti
     */
    LSL("426", "LSL", 2),

    /**
     * 拉脱维亚币
     */
    LVL("428", "LVL", 2),

    /**
     * 利比里亚
     */
    LRD("430", "LRD", 2),

    /**
     * Libyan Dinar
     */
    LYD("434", "LYD", 3),

    /**
     * 立陶宛币
     */
    LTL("440", "LTL", 2),

    /**
     * 马拉维
     */
    MWK("454", "MWK", 2),

    /**
     * Rufiyaa
     */
    MVR("462", "MVR", 2),

    /**
     * Ouguiya
     */
    MRO("478", "MRO", 2),

    /**
     * Mauritius Rupee
     */
    MUR("480", "MUR", 2),

    /**
     * MSD
     */
    MXN("484", "MXN", 2),

    /**
     * 蒙古图格里克
     */
    MNT("496", "MNT", 2),

    /**
     * Moldovan Leu
     */
    MDL("498", "MDL", 2),

    /**
     * 摩洛哥
     */
    MAD("504", "MAD", 2),

    /**
     * Rial Omani
     */
    OMR("512", "OMR", 3),

    /**
     * Dollar
     */
    NAD("516", "NAD", 2),

    /**
     * Nepalese Rupee
     */
    NPR("524", "NPR", 2),

    /**
     * Netherlands Antillian Guilder
     */
    ANG("532", "ANG", 2),

    /**
     * 阿鲁巴盾
     */
    AWG("533", "AWG", 2),

    /**
     * Vatu
     */
    VUV("548", "VUV", 0),

    /**
     * Cordoba Oro
     */
    NIO("558", "NIO", 2),

    /**
     * 尼日利亚奈拉
     */
    NGN("566", "NGN", 2),

    /**
     * Pakistan Rupee
     */
    PKR("586", "PKR", 2),

    /**
     * 巴拿马
     */
    PAB("590", "PAB", 2),

    /**
     * Kina
     */
    PGK("598", "PGK", 2),

    /**
     * Guarani
     */
    PYG("600", "PYG", 0),

    /**
     * 秘鲁币
     */
    PEN("604", "PEN", 2),

    /**
     * Qatari Rial
     */
    QAR("634", "QAR", 2),

    /**
     * Rwanda Franc
     */
    RWF("646", "RWF", 0),

    /**
     * St. Helena Pound
     */
    SHP("654", "SHP", 2),

    /**
     * Dobra
     */
    STD("678", "STD", 2),

    /**
     * Seychelles Rupee
     */
    SCR("690", "SCR", 2),

    /**
     * Leone
     */
    SLL("694", "SLL", 2),

    /**
     * 斯洛伐克
     */
    SKK("703", "SKK", 2),

    /**
     * Somali Shilling
     */
    SOS("706", "SOS", 2),

    /**
     * South Sudanese Pound
     */
    SSP("728", "SSP", 2),

    /**
     * 苏丹第纳尔
     */
    SDN("736", "SDN", 2),

    /**
     * Lilangeni
     */
    SZL("748", "SZL", 2),

    /**
     * Syrian Pound
     */
    SYP("760", "SYP", 2),

    /**
     * 泰铢
     */
    THB("764", "THB", "฿", 2),

    /**
     * Cypriot Pounds
     */
    CYP("196", "CYP", 2),

    /**
     * Swiss Francs
     */
    CHF("756", "CHF", 2),

    /**
     * Danish Krone
     */
    DKK("208", "DKK", 2),

    /**
     * British Pounds Sterling
     */
    GBP("826", "GBP", 2),

    /**
     * Indian Rupee
     */
    INR("356", "INR", 2),

    /**
     * Icelandic krone
     */
    ISK("352", "ISK", 2),

    /**
     * South Korean Won
     */
    KRW("410", "KRW", 0),
//
//    /**
//     * Sri-Lanka Rupee
//     */
//    LKR("144", "LKR", 2),

    /**
     * Maltese Lira
     */
    MTL("470", "MTL", 2),

    /**
     * Norwegian Krone
     */
    NOK2("578", "NOK", 2),

    /**
     * New Zealand Dollars
     */
    NZD("554", "NZD", 2),

    /**
     * Russian Ruble
     */
    RUB("643", "RUB", 2),

    /**
     * Saudi Riyal
     */
    SAR("682", "SAR", 2),

    /**
     * Swedish krone
     */
    SEK("752", "SEK", 2),

    /**
     * Swedish krone
     */
    SEK2("330", "SEK", 2),

    /**
     * Turkey Lira
     */
    TRL("792", "TRL", 2),

    /**
     * Bolivar Fuerte (Venezuela)
     */
    VEF("937", "VEF", 2),

    /**
     * South African Rand
     */
    ZAR("710", "ZAR", 2),

    /**
     * Kuwaiti Dinar
     */
    KWD("414", "KWD", 3),

    /**
     * 阿尔巴尼亚
     */
    ALL("008", "ALL", 2),

    /**
     * 阿根廷比索
     */
    ARS("032", "ARS", 2),

    /**
     * Algerian Dinar
     */
    DZD("012", "DZD", 2);

    /**
     * Currency code.
     */
    private String code;
    /**
     * Currency name.
     */
    private String name;
    /**
     * Currency short name.
     */
    private String shortName;
    /**
     * Currency decimals, 0-3.
     */
    private int decimals;

    Currency(String code, String name, int decimals) {
        this(code, name, "", decimals);
    }

    /**
     * @param code     Currency code.
     * @param name     Currency name.
     * @param decimals Currency decimals must be between 0 and 3.
     */
    Currency(String code, String name, String shortName, int decimals) {
        if (decimals < 0 || decimals > 3) {
            throw new IllegalArgumentException("decimals must be 0-3.");
        }
        this.code = code;
        this.name = name;
        this.shortName = shortName;
        this.decimals = decimals;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public int getDecimals() {
        return decimals;
    }

    /**
     * Return the currency display format, there are 4 types of format according to decimals
     * (0-3):0,0.0, 0.00,0.000
     *
     * @return The currency display format.
     */
    public String getFormat() {
        String format = null;
        switch (decimals) {
            case 0:
                format = "0";
                break;
            case 1:
                format = "0.0";
                break;
            case 2:
                format = "0.00";
                break;
            case 3:
                format = "0.000";
                break;
            default:
                break;
        }
        return format;
    }

    /**
     * Get the BCD code of currency code.
     *
     * @return
     */
    public byte[] getCodeBcdBytes() {
        if (!TextUtils.isEmpty(code)) {
            return Convert.strToBcd(code, Convert.EPaddingPosition.PADDING_LEFT);
        }

        return null;
    }

    /**
     * Get the BCD code of currency decimals.
     *
     * @return
     */
    public byte[] getDecimalBcdBytes() {
        return Convert.strToBcd(String.valueOf(decimals), Convert.EPaddingPosition.PADDING_LEFT);
    }

    /**
     * Get currency by currency code.
     *
     * @param code         currency code
     * @param defaultValue The value to be returned when the code does not exist.
     * @return
     */
    public static Currency queryCurrency(String code, Currency defaultValue) {
        if (TextUtils.isEmpty(code)) {
            return defaultValue;
        }

        for (Currency currency : Currency.values()) {
            if (code.equals(currency.code)) {
                return currency;
            }
        }

        return defaultValue;
    }

    public static Currency queryCurrencyByName(String currencyName, Currency defaultValue) {
        if (TextUtils.isEmpty(currencyName)) {
            return defaultValue;
        }

        for (Currency currency : Currency.values()) {
            if (currencyName.equals(currency.name)) {
                return currency;
            }
        }

        return defaultValue;
    }

    public Convert.ECurrencyExponent getCurrencyExponent() {
        return Convert.ECurrencyExponent.values()[decimals];
    }
}
