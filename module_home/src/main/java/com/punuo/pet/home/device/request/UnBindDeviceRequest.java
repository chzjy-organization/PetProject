package com.punuo.pet.home.device.request;

import com.punuo.sys.sdk.httplib.BaseRequest;
import com.punuo.sys.sdk.model.BaseModel;

/**
 * Created by han.chen.
 * Date on 2019-08-15.
 **/
public class UnBindDeviceRequest extends BaseRequest<BaseModel> {
    public UnBindDeviceRequest() {
        setRequestType(RequestType.POST);
        setRequestPath("/groups/leaveGroup");
    }
}
