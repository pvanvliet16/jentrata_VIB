package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.builder.RouteBuilder;
import org.jentrata.ebms.messaging.MessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Proivdes support for persisting a message into a store and providing a
 * reference to the message in the store
 *
 * @author aaronwalker
 */
public class MessageStoreRouteBuilder extends RouteBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(MessageStoreRouteBuilder.class);

    private String messageStoreEndpoint = MessageStore.DEFAULT_MESSAGE_STORE_ENDPOINT;
    private String insertMessageEndpoint = MessageStore.DEFAULT_MESSAGE_INSERT_ENDPOINT;
    private String updateMessageEndpoint = MessageStore.DEFAULT_MESSAGE_UPDATE_ENDPOINT;
    private MessageStore messageStore;

    @Override
    public void configure() throws Exception {

        LOG.info("Using message store " + messageStore.toString());

        from(messageStoreEndpoint)
            .bean(messageStore,"store")
        .routeId("_jentrataMessageStore");

        from(insertMessageEndpoint)
            .bean(messageStore, "storeMessage")
        .routeId("_jentrataInsertMessageStore");

        from(updateMessageEndpoint)
            .bean(messageStore,"updateMessage")
        .routeId("_jentrataUpdateMessageStore");
    }

    public String getMessageStoreEndpoint() {
        return messageStoreEndpoint;
    }

    public void setMessageStoreEndpoint(String messageStoreEndpoint) {
        this.messageStoreEndpoint = messageStoreEndpoint;
    }

    public String getInsertMessageEndpoint() {
        return insertMessageEndpoint;
    }

    public void setInsertMessageEndpoint(String insertMessageEndpoint) {
        this.insertMessageEndpoint = insertMessageEndpoint;
    }

    public String getUpdateMessageEndpoint() {
        return updateMessageEndpoint;
    }

    public void setUpdateMessageEndpoint(String updateMessageEndpoint) {
        this.updateMessageEndpoint = updateMessageEndpoint;
    }

    public MessageStore getMessageStore() {
        return messageStore;
    }

    public void setMessageStore(MessageStore messageStore) {
        this.messageStore = messageStore;
    }
}
