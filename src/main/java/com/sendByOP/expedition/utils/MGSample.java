package com.sendByOP.expedition.utils;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import com.mailgun.model.message.Message;
import com.mailgun.model.message.MessageResponse;

public class MGSample {

    public static String  API_KEY = "d8e8c81d9bf54ec4d28589375c3601ef-bdb2c8b4-172e5fcc";

    public static void main(String[] args) {
        MGSample.sendSimpleMessage();
        MGSample.sendComplexMessage();
    }

    public static MessageResponse sendSimpleMessage() {
        MailgunMessagesApi mailgunMessagesApi = MailgunClient.config(API_KEY)
                .createApi(MailgunMessagesApi.class);

        Message message = Message.builder()
                .from("etiennenkot1@gmail.com")
                .to("etiennenkot1@gmail.com")
                .subject("Hello")
                .text("Testing out some Mailgun awesomeness!")
                .build();

        return mailgunMessagesApi.sendMessage("sandboxeb516ade3c384e09aeaa0ec6517f4eac.mailgun.org", message);
    }

    public static MessageResponse sendComplexMessage() {
        MailgunMessagesApi mailgunMessagesApi = MailgunClient.config(API_KEY)
                .createApi(MailgunMessagesApi.class);

        Message message = Message.builder()
                .from("etiennenkot1@gmail.com")
                .to("honore.nkot@institutsaintjean.org")
                .cc("bob@example.com")
                .bcc("joe@example.com")
                .subject("Hello")
                .html("<html>HTML version </html>")
                .build();

        return mailgunMessagesApi.sendMessage("sandboxeb516ade3c384e09aeaa0ec6517f4eac.mailgun.org", message);
    }


}
