package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.MessageStatusType;
import org.jentrata.ebms.MessageType;
import org.jentrata.ebms.cpa.InvalidPartnerAgreementException;
import org.jentrata.ebms.cpa.PartnerAgreement;
import org.jentrata.ebms.cpa.pmode.PayloadService;
import org.jentrata.ebms.messaging.MessageStore;
import org.jentrata.ebms.soap.SoapMessageDataFormat;
import org.jentrata.ebms.utils.EbmsUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jentrata.ebms.utils.ExpressionHelper.headerWithDefault;

/**
 * Pickup outbound messages generates the ebMS envelope
 *
 * @author aaronwalker
 */
public class  EbmsOutboundMessageRouteBuilder extends RouteBuilder {

    private String deliveryQueue = "activemq:queue:jentrata_as4_outbound";
    private String errorQueue = "activemq:queue:jentrata_as4_outbound_error";
    private String outboundEbmsQueue = "activemq:queue:jentrata_internal_ebms_outbound";
    private String messgeStoreEndpoint = MessageStore.DEFAULT_MESSAGE_STORE_ENDPOINT;
    private String messageInsertEndpoint = MessageStore.DEFAULT_MESSAGE_INSERT_ENDPOINT;
    private String wsseSecurityAddEndpoint = "direct:wsseAddSecurityToHeader";
    private String defaultCPAId = null;

    @Override
    public void configure() throws Exception {

        from(deliveryQueue)
            .onException(Exception.class)
                .log(LoggingLevel.DEBUG, "headers:${headers}\nbody:\n${in.body}")
                .log(LoggingLevel.ERROR, "${exception.message}\n${exception.stacktrace}")
                .setHeader(EbmsConstants.MESSAGE_STATUS, constant(MessageStatusType.FAILED.name()))
                .setHeader(EbmsConstants.MESSAGE_STATUS_DESCRIPTION,simple("${exception.message}"))
                .to(errorQueue)
                .inOnly(EventNotificationRouteBuilder.SEND_NOTIFICATION_ENDPOINT)
                .handled(true)
            .end()
            .setHeader(EbmsConstants.DEFAULT_CPA_ID,constant(defaultCPAId))
            .to("direct:lookupCpaId")
            .choice()
                .when(header(EbmsConstants.CPA_ID).isEqualTo(EbmsConstants.CPA_ID_UNKNOWN))
                    .throwException(new InvalidPartnerAgreementException("unable to find matching partner agreement"))
                .otherwise()
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {

                            PartnerAgreement agreement = exchange.getIn().getHeader(EbmsConstants.CPA, PartnerAgreement.class);
                            if (agreement == null) {
                                throw new InvalidPartnerAgreementException("unable to find matching partner agreement");
                            }

                            String body = exchange.getIn().getBody(String.class);
                            String contentType = exchange.getIn().getHeader(EbmsConstants.CONTENT_TYPE, String.class);
                            String contentCharset = exchange.getIn().getHeader(EbmsConstants.CONTENT_CHAR_SET, "UTF-8", String.class);
                            String payloadId = exchange.getIn().getHeader(EbmsConstants.PAYLOAD_ID, String.class);
                            PayloadService payloadService = PayloadService.DEFAULT_PAYLOAD_SERVICE;
                            if (payloadId != null) {
                                payloadService = agreement.getPayloadProfile(payloadId);
                            } else {
                                payloadId = payloadService.getPayloadId();
                            }

                            if (contentType == null) {
                                contentType = payloadService.getContentType();
                            }

                            String schema = exchange.getIn().getHeader(EbmsConstants.MESSAGE_PAYLOAD_SCHEMA, String.class);
                            String compressionType = exchange.getIn().getHeader(EbmsConstants.PAYLOAD_COMPRESSION, payloadService.getCompressionType().getType(), String.class);
                            Map<String, String> mimeHeaders = extractMimeHeaders(contentType, exchange.getIn());
                            List<Map<String, Object>> partProperties = EbmsUtils.extractPartProperties(exchange.getIn().getHeader(EbmsConstants.MESSAGE_PART_PROPERTIES, String.class));

                            List<Map<String, Object>> payloads = new ArrayList<>();
                            Map<String, Object> payloadMap = new HashMap<>();
                            payloadMap.put("payloadId", payloadId);
                            payloadMap.put("contentType", contentType);
                            payloadMap.put("charset", contentCharset);
                            payloadMap.put("partProperties", partProperties);
                            payloadMap.put("schema", schema);
                            payloadMap.put("compressionType", compressionType);
                            if (compressionType != null && compressionType.length() > 0) {
                                payloadMap.put("content", EbmsUtils.compress(compressionType, body.getBytes(contentCharset)));
                            } else {
                                payloadMap.put("content", body.getBytes(contentCharset));
                            }
                            payloadMap.put("mimeHeaders", mimeHeaders);
                            payloads.add(payloadMap);
                            exchange.getIn().setBody(payloads);
                        }
                    })
                    .to("direct:setMessageDefaults")
                    .setHeader(EbmsConstants.MESSAGE_TYPE, constant(MessageType.USER_MESSAGE.name()))
                    .setHeader(EbmsConstants.MESSAGE_DIRECTION, constant(EbmsConstants.MESSAGE_DIRECTION_OUTBOUND))
                    .setHeader(EbmsConstants.MESSAGE_PAYLOADS, body())
                    .to("freemarker:templates/soap-envelope.ftl")
                    .to(wsseSecurityAddEndpoint)
                    .convertBodyTo(String.class)
                    .marshal(new SoapMessageDataFormat())
                    .to(messgeStoreEndpoint)
                    .to(messageInsertEndpoint)
                    .to(outboundEbmsQueue)
        .routeId("_jentrataEbmsGenerateMessage");

        from("direct:setMessageDefaults")
            .setHeader(EbmsConstants.MESSAGE_ID, headerWithDefault(EbmsConstants.MESSAGE_ID, simple("${bean:uuidGenerator.generateId}")))
            .setHeader(EbmsConstants.MESSAGE_FROM, headerWithDefault(EbmsConstants.MESSAGE_FROM, simple("${headers.JentrataCPA?.initiator?.partyId}")))
            .setHeader(EbmsConstants.MESSAGE_FROM_TYPE, headerWithDefault(EbmsConstants.MESSAGE_FROM_TYPE, simple("${headers.JentrataCPA?.initiator?.partyIdType}")))
            .setHeader(EbmsConstants.MESSAGE_FROM_ROLE, headerWithDefault(EbmsConstants.MESSAGE_FROM_ROLE, simple("${headers.JentrataCPA?.initiator?.role}")))
            .setHeader(EbmsConstants.MESSAGE_TO, headerWithDefault(EbmsConstants.MESSAGE_TO, simple("${headers.JentrataCPA?.responder?.partyId}")))
            .setHeader(EbmsConstants.MESSAGE_TO_TYPE, headerWithDefault(EbmsConstants.MESSAGE_TO_TYPE, simple("${headers.JentrataCPA?.responder?.partyIdType}")))
            .setHeader(EbmsConstants.MESSAGE_TO_ROLE, headerWithDefault(EbmsConstants.MESSAGE_TO_ROLE, simple("${headers.JentrataCPA?.responder?.role}")))
            .setHeader(EbmsConstants.MESSAGE_AGREEMENT_REF, headerWithDefault(EbmsConstants.MESSAGE_AGREEMENT_REF, simple("${headers.JentrataCPA?.agreementRef}")))
        .routeId("_jentrataSetMessageDefaults");

    }

    private Map<String, String> extractMimeHeaders(String contentType, Message message) {
        Map<String,String> mimeHeaders = new HashMap<>();
        mimeHeaders.put(EbmsConstants.CONTENT_TRANSFER_ENCODING,message.getHeader(EbmsConstants.CONTENT_TRANSFER_ENCODING,"binary",String.class));
        String filename = message.getHeader(EbmsConstants.PAYLOAD_FILENAME,generateFilename(contentType),String.class);
        mimeHeaders.put(EbmsConstants.CONTENT_DISPOSITION,"attachment; filename=" + filename);
        return mimeHeaders;

    }

    private String generateFilename(String contentType) {
        StringBuilder filename = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        filename.append(sdf.format(new Date()));
        if(contentType == null || contentType.isEmpty()) {
            filename.append(".txt");
        } else if(contentType.toLowerCase().contains("xml")) {
            filename.append(".xml");
        } else if(contentType.toLowerCase().contains("text")) {
            filename.append(".txt");
        }
        return filename.toString();
    }

    public String getDeliveryQueue() {
        return deliveryQueue;
    }

    public void setDeliveryQueue(String deliveryQueue) {
        this.deliveryQueue = deliveryQueue;
    }

    public String getErrorQueue() {
        return errorQueue;
    }

    public void setErrorQueue(String errorQueue) {
        this.errorQueue = errorQueue;
    }

    public String getOutboundEbmsQueue() {
        return outboundEbmsQueue;
    }

    public void setOutboundEbmsQueue(String outboundEbmsQueue) {
        this.outboundEbmsQueue = outboundEbmsQueue;
    }

    public String getMessgeStoreEndpoint() {
        return messgeStoreEndpoint;
    }

    public void setMessgeStoreEndpoint(String messgeStoreEndpoint) {
        this.messgeStoreEndpoint = messgeStoreEndpoint;
    }

    public String getMessageInsertEndpoint() {
        return messageInsertEndpoint;
    }

    public void setMessageInsertEndpoint(String messageInsertEndpoint) {
        this.messageInsertEndpoint = messageInsertEndpoint;
    }

    public String getWsseSecurityAddEndpoint() {
        return wsseSecurityAddEndpoint;
    }

    public void setWsseSecurityAddEndpoint(String wsseSecurityAddEndpoint) {
        this.wsseSecurityAddEndpoint = wsseSecurityAddEndpoint;
    }

    public String getDefaultCPAId() {
        return defaultCPAId;
    }

    public void setDefaultCPAId(String defaultCPAId) {
        this.defaultCPAId = defaultCPAId;
    }
}
