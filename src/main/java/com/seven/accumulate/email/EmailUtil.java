package com.seven.accumulate.email;


/**
 * @classDesc: ()
 * @Author:
 * @createTime: Created in 13:34 2018/8/13
 */


public class EmailUtil {
    public static void main(String[] args) {

        try {


            EmailInfo emailInfo = new EmailInfo.Builder()
                    .setHost("smtp.choicesoft.com.cn")
                    .setFrom("ops-no-reply@choicesoft.com.cn")
                    .setPassowrd("")
                    .setContent("好")
                    .addTo("")
                    .build();
            emailInfo.sendEmail();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}


