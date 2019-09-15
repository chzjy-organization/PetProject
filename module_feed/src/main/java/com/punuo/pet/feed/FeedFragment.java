package com.punuo.pet.feed;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.loonggg.weekcalendar.view.WeekCalendar;
import com.punuo.pet.PetManager;
import com.punuo.pet.feed.request.GetWeightInfoRequest;
import com.punuo.pet.model.PetData;
import com.punuo.pet.model.PetModel;
import com.punuo.pet.router.FeedRouter;
import com.punuo.sip.SipUserManager;
import com.punuo.sip.model.MediaData;
import com.punuo.sip.model.QueryData;
import com.punuo.sip.request.SipControlDeviceRequest;
import com.punuo.sip.request.SipMediaRequest;
import com.punuo.sip.request.SipQueryRequest;
import com.punuo.sip.request.SipRequestListener;
import com.punuo.sip.video.VideoInfoManager;
import com.punuo.sys.sdk.account.AccountManager;
import com.punuo.sys.sdk.fragment.BaseFragment;
import com.punuo.sys.sdk.httplib.HttpManager;
import com.punuo.sys.sdk.httplib.RequestListener;
import com.punuo.sys.sdk.model.BaseModel;
import com.punuo.sys.sdk.util.HandlerExceptionUtils;
import com.punuo.sys.sdk.util.StatusBarUtil;
import com.punuo.sys.sdk.util.ToastUtils;
import com.punuo.sys.sdk.util.ViewUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.zoolu.sip.message.Message;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by han.chen.
 * Date on 2019-09-12.
 **/
@Route(path = FeedRouter.ROUTER_FEED_HOME_FRAGMENT)
public class FeedFragment extends BaseFragment {
    @BindView(R2.id.title)
    TextView mTitle;
    @BindView(R2.id.back)
    ImageView mBack;
    @BindView(R2.id.sub_title)
    TextView mSubTitle;
    @BindView(R2.id.pet_container)
    LinearLayout mPetContainer;
    @BindView(R2.id.calendar_week)
    WeekCalendar mWeekCalendar;
    @BindView(R2.id.edit_feed_plan)
    View mEditFeedPlan;
    @BindView(R2.id.feed_right_now)
    View mFeedRightNow;

    @Autowired(name = "devId")
    String devId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFragmentView = inflater.inflate(R.layout.feed_fragment_home, container, false);
        ARouter.getInstance().inject(this);
        ButterKnife.bind(this, mFragmentView);
        initView();
        View mStatusBar = mFragmentView.findViewById(R.id.status_bar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mStatusBar.getLayoutParams().height = StatusBarUtil.getStatusBarHeight(getActivity());
            mStatusBar.setVisibility(View.VISIBLE);
            mStatusBar.requestLayout();
        }
        EventBus.getDefault().register(this);
        PetManager.getPetInfo();
        devId = "310023001139940001";
        return mFragmentView;
    }

    private void initView() {
        mTitle.setText("梦视科技喂食器");
        mBack.setVisibility(View.GONE);
        mWeekCalendar.setOnDateClickListener(new WeekCalendar.OnDateClickListener() {
            @Override
            public void onDateClick(String s) {
                ToastUtils.showToast(s);
            }
        });
        mEditFeedPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 跳转编辑喂食计划页面
            }
        });
        mFeedRightNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 调用云台旋转
            }
        });
    }

    private void startVideo(String devId) {
        queryMediaInfo(devId);
    }

    private void queryMediaInfo(final String devId) {
        SipQueryRequest sipQueryRequest = new SipQueryRequest(devId);
        sipQueryRequest.setSipRequestListener(new SipRequestListener<QueryData>() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onSuccess(QueryData result, Message message) {
                if (result == null) {
                    return;
                }
                VideoInfoManager.init(result);
                inviteMedia(devId);

            }

            @Override
            public void onError(Exception e) {
                HandlerExceptionUtils.handleException(e);
            }
        });
        SipUserManager.getInstance().addRequest(sipQueryRequest);
    }

    private void inviteMedia(String devId) {
        SipMediaRequest sipMediaRequest = new SipMediaRequest(devId);
        sipMediaRequest.setSipRequestListener(new SipRequestListener<MediaData>() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onSuccess(MediaData result, Message message) {
                if (result == null) {
                    return;
                }
                VideoInfoManager.init(result);
                //TODO 开启接收视频
            }

            @Override
            public void onError(Exception e) {
                HandlerExceptionUtils.handleException(e);
            }
        });
        SipUserManager.getInstance().addRequest(sipMediaRequest);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PetModel petModel) {
        initPetInfo(petModel);
    }

    private void initPetInfo(PetModel petModel) {
        if (petModel == null || petModel.mPets == null) {
            return;
        }
        mPetContainer.removeAllViews();
        for (int i = 0; i < petModel.mPets.size(); i++) {
            PetData petData = petModel.mPets.get(i);
            View view = LayoutInflater.from(getActivity()).
                    inflate(R.layout.feed_pet_info_item, mPetContainer, false);
            ImageView avatar = view.findViewById(R.id.pet_avatar);
            TextView petName = view.findViewById(R.id.pet_name);
            Glide.with(this).load(petData.avatar).into(avatar);
            ViewUtil.setText(petName, petData.petname);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            mPetContainer.addView(view);
        }
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.feed_add_item, mPetContainer, false);
        mPetContainer.addView(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO add
            }
        });
    }

    private void operateControl(String operate) {
        SipControlDeviceRequest sipControlDeviceRequest = new SipControlDeviceRequest(operate, devId);
        SipUserManager.getInstance().addRequest(sipControlDeviceRequest);
    }

    private GetWeightInfoRequest mGetWeightInfoRequest;

    private void getWeightInfo(String devId) {
        if (mGetWeightInfoRequest != null && !mGetWeightInfoRequest.isFinish()) {
            return;
        }
        mGetWeightInfoRequest = new GetWeightInfoRequest();
        mGetWeightInfoRequest.addUrlParam("username", AccountManager.getUserName());
        mGetWeightInfoRequest.addUrlParam("devid", devId);
        mGetWeightInfoRequest.setRequestListener(new RequestListener<BaseModel>() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onSuccess(BaseModel result) {

            }

            @Override
            public void onError(Exception e) {

            }
        });
        HttpManager.addRequest(mGetWeightInfoRequest);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}