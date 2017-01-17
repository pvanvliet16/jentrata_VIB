package org.jentrata.ebms.messaging;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.support.ExpressionAdapter;
import org.jentrata.ebms.EbmsConstants;

import javax.activation.DataHandler;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A splitter that takes a list of attachments on a message and splits them out
 * into individual messages with attachment being set as the body on the new
 * message. The original message body is available as message header called
 * CamelOriginalBody
 *
 * @author aaronwalker
 */
public class SplitAttachmentsToBody extends ExpressionAdapter {

    public static final String ORIGINAL_MESSAGE_BODY = "CamelOriginalBody";
    private static final String[] DEFAULT_HEADERS = {
            EbmsConstants.CONTENT_ID,
            EbmsConstants.CONTENT_TYPE,
            EbmsConstants.MESSAGE_ID,
            EbmsConstants.EBMS_MESSAGE_MEP,
            EbmsConstants.MESSAGE_DIRECTION,
            EbmsConstants.EBMS_VERSION,
            EbmsConstants.MESSAGE_STATUS,
            EbmsConstants.MESSAGE_FROM,
            EbmsConstants.MESSAGE_TO,
            EbmsConstants.MESSAGE_CONVERSATION_ID,
            EbmsConstants.MESSAGE_SERVICE,
            EbmsConstants.MESSAGE_ACTION,

    };

    private final boolean copyOriginalMessage;
    private final boolean copyHeaders;
    private boolean includeOriginalMessage = false;
    private final String [] headersToCopy;

    public SplitAttachmentsToBody(){
        this(true,true,false);
    }

    public SplitAttachmentsToBody(boolean copyOriginalMessage) {
        this(copyOriginalMessage,true,false);
    }

    public SplitAttachmentsToBody(boolean copyOriginalMessage, boolean copyHeaders) {
        this(copyOriginalMessage,copyHeaders,false);
    }

    public SplitAttachmentsToBody(boolean copyOriginalMessage, boolean copyHeaders, boolean includeOriginalMessage) {
        this.copyOriginalMessage = copyOriginalMessage;
        this.copyHeaders = copyHeaders;
        this.includeOriginalMessage = includeOriginalMessage;
        this.headersToCopy = DEFAULT_HEADERS;
    }

    public SplitAttachmentsToBody(boolean copyOriginalMessage, boolean includeOriginalMessage, String...headersToCopy) {
        this.copyOriginalMessage = copyOriginalMessage;
        this.copyHeaders = false;
        this.includeOriginalMessage = includeOriginalMessage;
        this.headersToCopy = headersToCopy;
    }

    @Override
    public Object evaluate(Exchange exchange) {

        String originalBody = exchange.getIn().getBody(String.class);

        // must use getAttachments to ensure attachments is initial populated
        if (!includeOriginalMessage && exchange.getIn().getAttachments().isEmpty()) {
            return null;
        }

        // we want to provide a list of messages with 1 attachment per mail
        List<Message> answer = new ArrayList<>();

        if(includeOriginalMessage && originalBody != null && originalBody.length() > 0) {

            String contentId = exchange.getIn().getHeader(EbmsConstants.CONTENT_ID,EbmsConstants.SOAP_BODY_PAYLOAD_ID,String.class);
            String contentType = exchange.getIn().getHeader(EbmsConstants.CONTENT_ID,EbmsConstants.TEXT_XML_CONTENT_TYPE,String.class);

            Message soapBodyMessage = createNewMessage(contentId,
                    contentType,
                    exchange.getIn().getBody(InputStream.class),
                    exchange);
            copyOriginalMessage(exchange, originalBody, soapBodyMessage);
            answer.add(soapBodyMessage);
        }

        for (Map.Entry<String, DataHandler> entry : exchange.getIn().getAttachments().entrySet()) {
            try {
                Message copy = createNewMessage(entry.getKey(),
                        entry.getValue().getContentType(),
                        entry.getValue().getInputStream(),
                        exchange);
                copyOriginalMessage(exchange, originalBody, copy);
                answer.add(copy);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return answer;
    }

    private void copyOriginalMessage(Exchange exchange, String originalBody, Message copy) {
        if(copyOriginalMessage) {
            Object body = exchange.getIn().getHeader(ORIGINAL_MESSAGE_BODY);
            if(body != null) {
                copy.setHeader(ORIGINAL_MESSAGE_BODY,body);
            } else {
                copy.setHeader(ORIGINAL_MESSAGE_BODY,originalBody);
            }
        }
    }

    private Message createNewMessage(String contentId, String contentType, InputStream stream, Exchange exchange) {

        final Message copy = exchange.getIn().copy();
        copy.getAttachments().clear();
        if(!copyHeaders) {
            copy.removeHeaders("*");
        }

        if(!copyHeaders && headersToCopy != null && headersToCopy.length > 0) {
            extractHeaders(copy,exchange.getIn().getHeaders(), headersToCopy);
        }

        // get the content and convert it to byte[]
        byte[] data = exchange.getContext().getTypeConverter().convertTo(byte[].class, stream);
        copy.setBody(data);

        copy.setHeader(EbmsConstants.CONTENT_ID,contentId);
        copy.setHeader(EbmsConstants.CONTENT_TYPE,contentType);


        return copy;
    }

    private void extractHeaders(Message copy, Map<String, Object> headers, String[] headersToCopy) {
        for(String header : headersToCopy) {
            copy.setHeader(header,headers.get(header));
        }
    }
}
