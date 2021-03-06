package com.punuo.pet.member.login.request;

import com.punuo.pet.member.login.model.LoginResult;
import com.punuo.sys.sdk.httplib.BaseRequest;

/**
 * Created by han.chen.
 * Date on 2019-06-15.
 * 账号密码登陆
 **/
public class AccountLoginRequest extends BaseRequest<LoginResult> {

    public AccountLoginRequest() {
        setRequestType(RequestType.POST);
        setRequestPath("/userLogin");
    }
}
