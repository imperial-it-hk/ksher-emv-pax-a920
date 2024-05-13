package com.evp.payment.ksher.config


import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigModel(@SerializedName("data") var data: Data?) : Parcelable {

    @Parcelize
    data class Data(
        @SerializedName("button") var button: Button?,
        @SerializedName("config") var config: Button.Config?,
        @SerializedName("qrLogo") var qrLogo: List<QrLogo?>?,
        @SerializedName("menusSale") var menusSale: MenusSale?,
        @SerializedName("menusMain") var menusMain: List<Button.MenusMain?>?,
        @SerializedName("menusMore") var menusMore: List<MenusMore?>?,
        @SerializedName("menusOther") var menusOther: List<MenusOther?>?,
        @SerializedName("menusPassword") var menusPassword: List<MenusPassword?>?,
        @SerializedName("menusSetting") var menusSetting: List<MenusSetting?>?,
        @SerializedName("footer") var footer: Footer?,
        @SerializedName("language") var language: List<String?>?,
        @SerializedName("password") var password: Password?,
        @SerializedName("setting") var setting: Setting?,
        @SerializedName("stringFile") var stringFile: StringFile?
    ) : Parcelable {
        @Parcelize
        data class Button(
            @SerializedName("buttonAcquirer") var buttonAcquirer: ButtonAcquirer?,
            @SerializedName("buttonAllPayment") var buttonAllPayment: ButtonAllPayment?,
            @SerializedName("buttonAnyTransaction") var buttonAnyTransaction: ButtonAnyTransaction?,
            @SerializedName("buttonAuditReport") var buttonAuditReport: ButtonAuditReport?,
            @SerializedName("buttonCancel") var buttonCancel: ButtonCancel?,
            @SerializedName("buttonCommDetail") var buttonCommDetail: ButtonCommDetail?,
            @SerializedName("buttonCommunication") var buttonCommunication: ButtonCommunication?,
            @SerializedName("buttonDelete") var buttonDelete: ButtonDelete?,
            @SerializedName("buttonDetailByDate") var buttonDetailByDate: ButtonDetailByDate?,
            @SerializedName("buttonDetailPayment") var buttonDetailPayment: ButtonDetailPayment?,
            @SerializedName("buttonLastSettlement") var buttonLastSettlement: ButtonLastSettlement?,
            @SerializedName("buttonLastTransaction") var buttonLastTransaction: ButtonLastTransaction?,
            @SerializedName("buttonOk") var buttonOk: ButtonOk?,
            @SerializedName("buttonOther") var buttonOther: ButtonOther?,
            @SerializedName("buttonPassword") var buttonPassword: ButtonPassword?,
            @SerializedName("buttonPay") var buttonPay: ButtonPay?,
            @SerializedName("buttonPostpone") var buttonPostpone: ButtonPostpone?,
            @SerializedName("buttonPrint") var buttonPrint: ButtonPrint?,
            @SerializedName("buttonSelectPayment") var buttonSelectPayment: ButtonSelectPayment?,
            @SerializedName("buttonShow") var buttonShow: ButtonShow?,
            @SerializedName("buttonSummaryByDate") var buttonSummaryByDate: ButtonSummaryByDate?,
            @SerializedName("buttonSummaryPayment") var buttonSummaryPayment: ButtonSummaryPayment?,
            @SerializedName("buttonSummaryReport") var buttonSummaryReport: ButtonSummaryReport?,
            @SerializedName("buttonSuspendedQR") var buttonSuspendedQR: ButtonSuspendedQR?,
            @SerializedName("buttonTransactionSetting") var buttonTransactionSetting: ButtonTransactionSetting?,
            @SerializedName("buttonConfirm") var buttonConfirm: ButtonConfirm?

        ) : Parcelable {
            @Parcelize
            data class ButtonAcquirer(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("icon") var icon: String?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonAllPayment(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonAnyTransaction(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonAuditReport(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonCancel(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonCommDetail(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("icon") var icon: String?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonCommunication(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("icon") var icon: String?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonDelete(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonDetailByDate(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonDetailPayment(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonLastSettlement(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonLastTransaction(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonOk(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonOther(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("icon") var icon: String?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonPassword(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("icon") var icon: String?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonPay(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonPostpone(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonPrint(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonSelectPayment(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonShow(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonSummaryByDate(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonSummaryPayment(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonSummaryReport(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonSuspendedQR(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ButtonTransactionSetting(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("icon") var icon: String?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class Config(
                @SerializedName("acquirer") var acquirer: String?,
                @SerializedName("apn") var apn: String?,
                @SerializedName("communicationType") var communicationType: String?,
                @SerializedName("connectionTimeout") var connectionTimeout: String?,
                @SerializedName("trueMoneyQRTime") var trueMoneyQRTime: String?,
                @SerializedName("prpmptPayQRTime") var prpmptPayQRTime: String?,
                @SerializedName("merchantId") var merchantId: String?,
                @SerializedName("terminalId") var terminalId: String?,
                @SerializedName("transactionTimeout") var transactionTimeout: String?,
                @SerializedName("defaultCurrencyUnit") var defaultCurrencyUnit: String?,
            ) : Parcelable

            @Parcelize
            data class MenusMain(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("icon") var icon: String?,
                @SerializedName("index") var index: Int?,
                @SerializedName("label") var label: List<String?>?,
                @SerializedName("payment") var payment: List<Payment?>?
            ) : Parcelable

            @Parcelize
            data class Payment(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("icon") var icon: String?,
                @SerializedName("index") var index: Int?,
                @SerializedName("label") var label: String?,
                @SerializedName("paymentType") var paymentType: String?
            ) : Parcelable

            @Parcelize
            data class ButtonConfirm(
                @SerializedName("display") var display: Boolean?,
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable
        }

        @Parcelize
        data class QrLogo(
            @SerializedName("index") var index: Int?,
            @SerializedName("display") var display: Boolean?,
            @SerializedName("icon") var icon: String?,
            @SerializedName("banner") var banner: String?,
            @SerializedName("paymentType") var paymentType: String?
        ) : Parcelable

        @Parcelize
        data class MenusSale(
            @SerializedName("display") var display: Boolean?,
            @SerializedName("label") var label: List<String?>?,
            @SerializedName("payment") var payment: List<Button.Payment?>?
        ) : Parcelable

        @Parcelize
        data class MenusMore(
            @SerializedName("display") var display: Boolean?,
            @SerializedName("icon") var icon: String?,
            @SerializedName("index") var index: Int?,
            @SerializedName("label") var label: List<String?>?
        ) : Parcelable

        @Parcelize
        data class MenusOther(
            @SerializedName("display") var display: Boolean?,
            @SerializedName("icon") var icon: String?,
            @SerializedName("index") var index: Int?,
            @SerializedName("label") var label: List<String?>?
        ) : Parcelable

        @Parcelize
        data class MenusPassword(
            @SerializedName("display") var display: Boolean?,
            @SerializedName("icon") var icon: String?,
            @SerializedName("index") var index: Int?,
            @SerializedName("label") var label: List<String?>?
        ) : Parcelable

        @Parcelize
        data class MenusSetting(
            @SerializedName("display") var display: Boolean?,
            @SerializedName("icon") var icon: String?,
            @SerializedName("index") var index: Int?,
            @SerializedName("label") var label: List<String?>?
        ) : Parcelable

        @Parcelize
        data class Password(
            @SerializedName("passwordAdmin") var passwordAdmin: String?,
            @SerializedName("passwordMerchant") var passwordMerchant: String?,
            @SerializedName("passwordSettlement") var passwordSettlement: String?,
            @SerializedName("passwordVoidAndRefund") var passwordVoidAndRefund: String?
        ) : Parcelable

        @Parcelize
        data class Footer(
            @SerializedName("display") var display: Boolean?,
            @SerializedName("disclaimerTxt") var disclaimerTxt: String?,
            @SerializedName("labelFooter") var labelFooter: String?,
            @SerializedName("icon") var icon: String?,
        ) : Parcelable

        @Parcelize
        data class Setting(
            @SerializedName("appIdOffline") var appIdOffline: String?,
            @SerializedName("appIdOnline") var appIdOnline: String?,
            @SerializedName("appName") var appName: AppNameLabel?,
            @SerializedName("configVersion") var configVersion: String?,
            @SerializedName("language") var language: String?,
            @SerializedName("logo") var logo: String?,
            @SerializedName("systemMaxTransNumberDefault") var systemMaxTransNumberDefault: String?,
            @SerializedName("systemPrintGrayDefault") var systemPrintGrayDefault: String?,
            @SerializedName("tokenOffline") var tokenOffline: String?,
            @SerializedName("tokenOnline") var tokenOnline: String?,
            @SerializedName("colorPrimary") var colorPrimary: String?,
            @SerializedName("colorSecondary") var colorSecondary: String?,
            @SerializedName("address") var address: String?,
            @SerializedName("storeId") var storeId: String?,
            @SerializedName("autoSettlementTime") var autoSettlementTime: String?,
            @SerializedName("paymentDomain") var paymentDomain: String?,
            @SerializedName("gateWayDomain") var gateWayDomain: String?,
            @SerializedName("publicKey") var publicKey: String?

        ) : Parcelable{
            @Parcelize
            data class AppNameLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable
        }

        @Parcelize
        data class StringFile(
            @SerializedName("aboutLabel") var aboutLabel: AboutLabel?,
            @SerializedName("acquirerLabel") var acquirerLabel: AcquirerLabel?,
            @SerializedName("amountConfirmationLabel") var amountConfirmationLabel: AmountConfirmationLabel?,
            @SerializedName("amountLabel") var amountLabel: AmountLabel?,
            @SerializedName("AnyTransactionLabel") var anyTransactionLabel: AnyTransactionLabel?,
            @SerializedName("apnLabel") var apnLabel: ApnLabel?,
            @SerializedName("auditReportAllPaymentLabel") var auditReportAllPaymentLabel: AuditReportAllPaymentLabel?,
            @SerializedName("auditReportLabel") var auditReportLabel: AuditReportLabel?,
            @SerializedName("batchNoLabel") var batchNoLabel: BatchNoLabel?,
            @SerializedName("checkSuspendedQrLabel") var checkSuspendedQrLabel: CheckSuspendedQrLabel?,
            @SerializedName("clickAgainToExitLabel") var clickAgainToExitLabel: ClickAgainToExitLabel?,
            @SerializedName("commLabel") var commLabel: CommLabel?,
            @SerializedName("communicationLabel") var communicationLabel: CommunicationLabel?,
            @SerializedName("configLabel") var configLabel: ConfigLabel?,
            @SerializedName("configurationLabel") var configurationLabel: ConfigurationLabel?,
            @SerializedName("connectionTimeoutLabel") var connectionTimeoutLabel: ConnectionTimeoutLabel?,
            @SerializedName("dataNotFoundLabel") var dataNotFoundLabel: DataNotFoundLabel?,
            @SerializedName("declinedLabel") var declinedLabel: DeclinedLabel?,
            @SerializedName("detailLabel") var detailLabel: DetailLabel?,
            @SerializedName("forceSettlementLabel") var forceSettlementLabel: ForceSettlementLabel?,
            @SerializedName("inputPasswordForSettlementLabel") var inputPasswordForSettlementLabel: InputPasswordForSettlementLabel?,
            @SerializedName("inputPasswordForVoidLabel") var inputPasswordForVoidLabel: InputPasswordForVoidLabel?,
            @SerializedName("inputTraceLabel") var inputTraceLabel: InputTraceLabel?,
            @SerializedName("inputTraceNumberLabel") var inputTraceNumberLabel: InputTraceNumberLabel?,
            @SerializedName("inquirySuspendedQrLabel") var inquirySuspendedQrLabel: InquirySuspendedQrLabel?,
            @SerializedName("lastSettlementLabel") var lastSettlementLabel: LastSettlementLabel?,
            @SerializedName("lastTransactionLabel") var lastTransactionLabel: LastTransactionLabel?,
            @SerializedName("merchantIDLabel") var merchantIDLabel: MerchantIDLabel?,
            @SerializedName("merchantLabel") var merchantLabel: MerchantLabel?,
            @SerializedName("messageInquirySuspendedQrLabel") var messageInquirySuspendedQrLabel: MessageInquirySuspendedQrLabel?,
            @SerializedName("messageSettlementSuspendedQrLabel") var messageSettlementSuspendedQrLabel: MessageSettlementSuspendedQrLabel?,
            @SerializedName("messageTransactionFoundLabel") var messageTransactionFoundLabel: MessageTransactionFoundLabel?,
            @SerializedName("moreLabel") var moreLabel: MoreLabel?,
            @SerializedName("noTransactionLabel") var noTransactionLabel: NoTransactionLabel?,
            @SerializedName("openWifiLabel") var openWifiLabel: OpenWifiLabel?,
            @SerializedName("otherLabel") var otherLabel: OtherLabel?,
            @SerializedName("passwordEnterAdminLabel") var passwordEnterAdminLabel: PasswordEnterAdminLabel?,
            @SerializedName("passwordEnterMerchantLabel") var passwordEnterMerchantLabel: PasswordEnterMerchantLabel?,
            @SerializedName("passwordFailLabel") var passwordFailLabel: PasswordFailLabel?,
            @SerializedName("passwordLabel") var passwordLabel: PasswordLabel?,
            @SerializedName("passwordNewLabel") var passwordNewLabel: PasswordNewLabel?,
            @SerializedName("passwordOldLabel") var passwordOldLabel: PasswordOldLabel?,
            @SerializedName("passwordSuccessLabel") var passwordSuccessLabel: PasswordSuccessLabel?,
            @SerializedName("passwordVerifyNewLabel") var passwordVerifyNewLabel: PasswordVerifyNewLabel?,
            @SerializedName("pleaseDoSettlementLabel") var pleaseDoSettlementLabel: PleaseDoSettlementLabel?,
            @SerializedName("pleaseInputLabel") var pleaseInputLabel: PleaseInputLabel?,
            @SerializedName("pressOkToPrintSlipLabel") var pressOkToPrintSlipLabel: PressOkToPrintSlipLabel?,
            @SerializedName("printLabel") var printLabel: PrintLabel?,
            @SerializedName("processLabel") var processLabel: ProcessLabel?,
            @SerializedName("qrInquiryAnyTransactionLabel") var qrInquiryAnyTransactionLabel: QrInquiryAnyTransactionLabel?,
            @SerializedName("qrInquiryLabel") var qrInquiryLabel: QrInquiryLabel?,
            @SerializedName("qrInquiryLastTranLabel") var qrInquiryLastTranLabel: QrInquiryLastTranLabel?,
            @SerializedName("qrInquirySuspendedLabel") var qrInquirySuspendedLabel: QrInquirySuspendedLabel?,
            @SerializedName("queryTransactionLabel") var queryTransactionLabel: QueryTransactionLabel?,
            @SerializedName("resourceLabel") var resourceLabel: ResourceLabel?,
            @SerializedName("selectChoiceToPrintLabel") var selectChoiceToPrintLabel: SelectChoiceToPrintLabel?,
            @SerializedName("selectPaymentTypeLabel") var selectPaymentTypeLabel: SelectPaymentTypeLabel?,
            @SerializedName("selectReportTypeLabel") var selectReportTypeLabel: SelectReportTypeLabel?,
            @SerializedName("settingLabel") var settingLabel: SettingLabel?,
            @SerializedName("settlementFailLabel") var settlementFailLabel: SettlementFailLabel?,
            @SerializedName("settlementLabel") var settlementLabel: SettlementLabel?,
            @SerializedName("settlementStartLabel") var settlementStartLabel: SettlementStartLabel?,
            @SerializedName("settlementSuccessLabel") var settlementSuccessLabel: SettlementSuccessLabel?,
            @SerializedName("softwareVersionLabel") var softwareVersionLabel: SoftwareVersionLabel?,
            @SerializedName("summaryLabel") var summaryLabel: SummaryLabel?,
            @SerializedName("summaryReportAllPaymentLabel") var summaryReportAllPaymentLabel: SummaryReportAllPaymentLabel?,
            @SerializedName("summaryReportLabel") var summaryReportLabel: SummaryReportLabel?,
            @SerializedName("terminalIDLabel") var terminalIDLabel: TerminalIDLabel?,
            @SerializedName("traceNoLabel") var traceNoLabel: TraceNoLabel?,
            @SerializedName("tranSummaryByDateLabel") var tranSummaryByDateLabel: TranSummaryByDateLabel?,
            @SerializedName("tranSummaryByPaymentLabel") var tranSummaryByPaymentLabel: TranSummaryByPaymentLabel?,
            @SerializedName("transTypeLabel") var transTypeLabel: TransTypeLabel?,
            @SerializedName("transactionFailLabel") var transactionFailLabel: TransactionFailLabel?,
            @SerializedName("transactionHistoryLabel") var transactionHistoryLabel: TransactionHistoryLabel?,
            @SerializedName("TransactionSettingLabel") var transactionSettingLabel: TransactionSettingLabel?,
            @SerializedName("transactionTimeoutLabel") var transactionTimeoutLabel: TransactionTimeoutLabel?,
            @SerializedName("updateVersionLabel") var updateVersionLabel: UpdateVersionLabel?,
            @SerializedName("voidAndRefundLabel") var voidAndRefundLabel: VoidAndRefundLabel?,
            @SerializedName("voidLabel") var voidLabel: VoidLabel?,
            @SerializedName("walletLabel") var walletLabel: WalletLabel?,
            @SerializedName("selectFunctionLabel") var selectFunctionLabel: SelectFunctionLabel?,
            @SerializedName("printCustomerCopyLabel") var printCustomerCopyLabel: PrintCustomerCopyLabel?,
            @SerializedName("inputPasswordLabel") var inputPasswordLabel: InputPasswordLabel?,
            @SerializedName("transactionNotFoundLabel") var transactionNotFoundLabel: TransactionNotFoundLabel?,
            @SerializedName("transactionAlreadyVoidLabel") var transactionAlreadyVoidLabel: TransactionAlreadyVoidLabel?,
            @SerializedName("transactionAlreadyRefundLabel") var transactionAlreadyRefundLabel: TransactionAlreadyRefundLabel?,
            @SerializedName("approvedLabel") var approvedLabel: ApprovedLabel?,
            @SerializedName("secondsLabel") var secondsLabel: SecondsLabel?,
            @SerializedName("checkTransactionLabel") var checkTransactionLabel: CheckTransactionLabel?,
            @SerializedName("queryHistoryLabel") var queryHistoryLabel: QueryHistoryLabel?,
            @SerializedName("noTransactionFoundLabel") var noTransactionFoundLabel: NoTransactionFoundLabel?,
            @SerializedName("deleteSuspendQRTracNoLabel") var deleteSuspendQRTracNoLabel: DeleteSuspendQRTracNoLabel?,
            @SerializedName("inquiryLabel") var inquiryLabel: InquiryLabel?,
            @SerializedName("querySuspendedQRLabel") var querySuspendedQRLabel: QuerySuspendedQRLabel?,
            @SerializedName("noSuspendedQRFoundLabel") var noSuspendedQRFoundLabel: NoSuspendedQRFoundLabel?,
            @SerializedName("unknownErrorLabel") var unknownErrorLabel: UnknownErrorLabel?,
            @SerializedName("suspendedLabel") var suspendedLabel: SuspendedLabel?,
            @SerializedName("pleaseContactAdminLabel") var pleaseContactAdminLabel: PleaseContactAdminLabel?,
            @SerializedName("generateQRLabel") var generateQRLabel: GenerateQRLabel?,
            @SerializedName("processErrorLabel") var processErrorLabel: ProcessErrorLabel?,
            @SerializedName("noQRImageFromHostLabel") var noQRImageFromHostLabel: NoQRImageFromHostLabel?,
            @SerializedName("passwordIsIncorrectLabel") var passwordIsIncorrectLabel: PasswordIsIncorrectLabel?,
            @SerializedName("languageLabel") var languageLabel: LanguageLabel?,
            @SerializedName("updateSuccessLabel") var updateSuccessLabel: UpdateSuccessLabel?,
            @SerializedName("loadingLabel") var loadingLabel: LoadingLabel?,
            @SerializedName("allTransactionNeedSettledLabel") var allTransactionNeedSettledLabel: AllTransactionNeedSettledLabel?,
            @SerializedName("messageSettlementSuspendedQrInquiryNowLabel") var messageSettlementSuspendedQrInquiryNowLabel: MessageSettlementSuspendedQrInquiryNowLabel?,
            @SerializedName("autoSettlementLabel") var autoSettlementLabel: AutoSettlementLabel?


        ) : Parcelable {
            @Parcelize
            data class AboutLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class AcquirerLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class AmountConfirmationLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class AmountLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class AnyTransactionLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ApnLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class AuditReportAllPaymentLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class AuditReportLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class BatchNoLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class CheckSuspendedQrLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ClickAgainToExitLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class CommLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class CommunicationLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ConfigLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ConfigurationLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ConnectionTimeoutLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class DataNotFoundLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class DeclinedLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class DetailLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ForceSettlementLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class InputPasswordForSettlementLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class InputPasswordForVoidLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class InputTraceLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class InputTraceNumberLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class InquirySuspendedQrLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class LastSettlementLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class LastTransactionLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class MerchantIDLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class MerchantLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class MessageInquirySuspendedQrLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class MessageSettlementSuspendedQrLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class MessageTransactionFoundLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class MoreLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class NoTransactionLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class OpenWifiLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class OtherLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class PasswordEnterAdminLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class PasswordEnterMerchantLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class PasswordFailLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class PasswordLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class PasswordNewLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class PasswordOldLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class PasswordSuccessLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class PasswordVerifyNewLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class PleaseDoSettlementLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class PleaseInputLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class PressOkToPrintSlipLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class PrintLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ProcessLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class QrInquiryAnyTransactionLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class QrInquiryLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class QrInquiryLastTranLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class QrInquirySuspendedLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class QueryTransactionLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ResourceLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class SelectChoiceToPrintLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class SelectPaymentTypeLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class SelectReportTypeLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class SettingLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class SettlementFailLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class SettlementLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class SettlementStartLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class SettlementSuccessLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class SoftwareVersionLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class SummaryLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class SummaryReportAllPaymentLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class SummaryReportLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class TerminalIDLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class TraceNoLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class TranSummaryByDateLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class TranSummaryByPaymentLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class TransTypeLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class TransactionFailLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class TransactionHistoryLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class TransactionSettingLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class TransactionTimeoutLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class UpdateVersionLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class VoidAndRefundLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class VoidLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class WalletLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class SelectFunctionLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class PrintCustomerCopyLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class InputPasswordLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class TransactionNotFoundLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class TransactionAlreadyVoidLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class TransactionAlreadyRefundLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ApprovedLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class SecondsLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class CheckTransactionLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class QueryHistoryLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class NoTransactionFoundLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class DeleteSuspendQRTracNoLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class InquiryLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class QuerySuspendedQRLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class NoSuspendedQRFoundLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class UnknownErrorLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class SuspendedLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class PleaseContactAdminLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class GenerateQRLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class ProcessErrorLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class NoQRImageFromHostLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class PasswordIsIncorrectLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class LanguageLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class UpdateSuccessLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class LoadingLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class AllTransactionNeedSettledLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class MessageSettlementSuspendedQrInquiryNowLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

            @Parcelize
            data class AutoSettlementLabel(
                @SerializedName("label") var label: List<String?>?
            ) : Parcelable

        }

//        @Parcelize
//        data class Label(
//            @SerializedName("en") var en: String?, @SerializedName("th") var th: String?
//        ) : Parcelable

    }
}