package com.em_projects.utils;

import java.io.UnsupportedEncodingException;

/**
 * Created by eyal muchtar on 15/03/2017.
 */

public class AuthenticationUtils {

    public static final String getShaCode(String mobile, String nonce, String deviceIMSI, String otp) throws UnsupportedEncodingException {
        return StringUtil.toBase64(StringUtil.toSha256Str(mobile + nonce + deviceIMSI + otp));
    }

}
