package application.errors;

import org.json.JSONObject;

public class ValidationMessages {
    public static final String REGISTRATION_REQUIREMENTS_BLANK = "username, email, and password must not be blank";
    public static final String ARTICLE_REQUIREMENTS_BLANK = "title, description, and body must not be blank";
    public static final String DUPLICATE_USERNAME = "username is taken";
    public static final String DUPLICATE_EMAIL = "email is taken";
    public static final String LOGIN_REQUIREMENTS_BLANK = "email, and password must not be blank";
    public static final String EMAIL_NOT_FOUND = "provided email not found";
    public static final String LOGIN_FAIL = "matching email and password not found";
    public static final String PROFILE_NOT_FOUND = "profile not found";
    public static final String USER_NOT_FOUND = "user not found";
    public static final String ARTICLE_NOT_FOUND = "article not found";
    public static final String ARTICLE_SLUG_EXISTS = "article with simplified slug exists. please change your article title";
    public static final String ARTICLE_NOT_YOURS = "article does not belong to you";
    public static final String COMMENT_REQUIREMENTS_BLANK = "body must not be empty";
    public static final String COMMENT_NOT_FOUND = "comment not found";
    public static final String COMMENT_NOT_YOURS = "comment does not belong to you";
    public static final String UPDATING_DIFFERENT_USER = "shouldn't update user that's not you";
    public static String throwError(String message) {
        JSONObject body = new JSONObject();

        body.put("body", new String[]{message});
        return new JSONObject().put("errors", body).toString();
    }
}