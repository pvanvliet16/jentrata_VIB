package org.jentrata.ebms;

import org.apache.camel.Exchange;

public class EbmsConstants {

    public static final String JENTRATA_VERSION = "JentrataVersion";

    //Jentrata Message Header keys
    public static final String SOAP_VERSION = "JentrataSOAPVersion";
    public static final String EBMS_VERSION = "JentrataEBMSVersion";
    public static final String VALID_PARTNER_AGREEMENT = "JentrataIsValidPartnerAgreement";
    public static final String EBMS_MESSAGE_MEP = "JentrataMEP";

    public static final String SOAP_VERSION_1_1 = "1.1";
    public static final String SOAP_VERSION_1_2 = "1.2";

    public static final String SOAP_1_1_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";

    public static final String SOAP_1_2_NAMESPACE = "http://www.w3.org/2003/05/soap-envelope";
    public static final String EBXML_V2_NAMESPACE = "http://www.oasis-open.org/committees/ebxml-msg/schema/msg-header-2_0.xsd";

    public static final String EBXML_V3_NAMESPACE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/";

    public static final String EBMS_V2 = "ebMSV2";
    public static final String EBMS_V3 = "ebMSV3";

    public static final String EBMS_V3_MEP_ONE_WAY = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/oneWay";
    public static final String EBMS_V3_MEP_TWO_WAY = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/twoWay";

    public static final String EBMS_V3_MEP_BINDING_PUSH = "ttp://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/push";

    public static final String EBMS_RECEIPT_REQUIRED = "JentrataReceiptRequired";

    public static final String CONTENT_ID = "Content-ID";
    public static final String CONTENT_LENGTH = Exchange.CONTENT_LENGTH;
    public static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
    public static final String CONTENT_DISPOSITION = "Content-Disposition";
    public static final String CONTENT_TYPE = Exchange.CONTENT_TYPE;

    public static final String SOAP_XML_CONTENT_TYPE = "application/soap+xml";
    public static final String TEXT_XML_CONTENT_TYPE = "text/xml";
    public static final String MESSAGE_ID = "JentrataMessageID";
    public static final String REF_TO_MESSAGE_ID = "JentrataRefMessageID";
    public static final String MESSAGE_FROM = "JentrataFrom";
    public static final String MESSAGE_FROM_TYPE = "JentrataFromPartyIdType";
    public static final String MESSAGE_FROM_ROLE = "JentrataPartyFromRole";
    public static final String MESSAGE_TO = "JentrataTo";
    public static final String MESSAGE_TO_TYPE = "JentrataToPartyIdType";
    public static final String MESSAGE_TO_ROLE = "JentrataPartyToRole";
    public static final String MESSAGE_AGREEMENT_REF = "JentrataAgreementRef";
    public static final String MESSAGE_PART_PROPERTIES = "JentraraPartProperties";
    public static final String MESSAGE_PAYLOAD_SCHEMA = "JentrataPayloadSchema";
    public static final String MESSAGE_DIRECTION = "JentrataMessageDirection";
    public static final String MESSAGE_DIRECTION_INBOUND = "inbox";
    public static final String MESSAGE_DIRECTION_OUTBOUND = "outbox";
    public static final String MESSAGE_STATUS = "JentrataMessageStatus";
    public static final String MESSAGE_STATUS_DESCRIPTION = "JentrataMessageStatusDesc";
    public static final String MESSAGE_TYPE = "JentrataMessageType";
    public static final String MESSAGE_SERVICE = "JentrataService";
    public static final String MESSAGE_ACTION = "JentrataAction";
    public static final String MESSAGE_CONVERSATION_ID = "JentrataConversationId";
    public static final String CPA = "JentrataCPA";
    public static final String CPA_ID = "JentrataCPAId";
    public static final String PAYLOAD_ID = "JentrataPayloadId";
    public static final String PAYLOAD_FILENAME = "JentrataPayloadFilename";
    public static final String PAYLOAD_COMPRESSION = "JentrataPayloadCompression";
    public static final String CPA_ID_UNKNOWN = "UNKNOWN";
    public static final String CONTENT_CHAR_SET = "JentrataCharSet";
    public static final String SOAP_BODY_PAYLOAD_ID = "soapbodypart";
    public static final String SECURITY_CHECK = "JentrataSecurityCheck";
    public static final String SECURITY_RESULTS = "JentrataSecurityResults";

    public static final String SECURITY_ERROR_CODE = "JentrataSecurityErrorCode";
    public static final String EBMS_ERROR = "JentrataEbmsError";
    public static final String EBMS_ERROR_CODE = "JentrataEbmsErrorCode";
    public static final String EBMS_ERROR_DESCRIPTION = "JentrataEbmsErrorDesc";
    public static final String MESSAGE_PAYLOADS = "JentrataPayloads";
    public static final String COMPRESSION_TYPE = "CompressionType";
    public static final String GZIP = "application/gzip";
    public static final String MESSAGE_DATE = "JentrataMessageDate";
    public static final String MESSAGE_RECEIPT_PATTERN = "JentrataMessageReceipt";
    public static final String MESSAGE_DUP_DETECTION = "JentrataDupDetection";
    public static final String DUPLICATE_MESSAGE = "JentrataIsDuplicateMessage";
    public static final String DUPLICATE_MESSAGE_ID = "JentrataDuplicateMessageId";
    public static final String VALIDATION_ERROR_DESC = "JentrataValidationErrorDesc";
    public static final String EBMS_VALIDATION_ERROR = "JentrataValidationError";

    public static final String DEFAULT_CPA_ID = "JentrataDefaultCPAId";
    public static final String DELIVERY_ERROR = "JentrataDeliveryError";
}