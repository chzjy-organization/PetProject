package com.punuo.pet.feed.plan;

import com.punuo.sys.sdk.httplib.BaseRequest;

public class GetPlanRequest extends BaseRequest {

    public GetPlanRequest(){
        setRequestPath("/feedplan/getFeedPlan");
        setRequestType(RequestType.GET);
    }
}
