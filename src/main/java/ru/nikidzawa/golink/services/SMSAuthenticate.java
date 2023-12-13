package ru.nikidzawa.golink.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SMSAuthenticate {
    public static final String ACCOUNT_SID = "AC71edb2199d5c5305ec03450085aec6e5";
    public static final String AUTH_TOKEN = "2ad8dbffa625cf57d90b2fa9e98199cc";
    public static final String TWILIO_NUMBER = "+18887025305";

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public static void sendMessage (String code, String number) {
        Message.creator(
                new PhoneNumber(number),
                new PhoneNumber(TWILIO_NUMBER),
                code
        ).create();
    }
}
