package com.punuo.sys.app.video;

import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.hengyi.fastvideoplayer.library.FastVideoPlayer;
import com.punuo.pet.home.device.model.BindDevidSuccess;
import com.punuo.pet.router.VideoRouter;
import com.punuo.sip.H264Config;
import com.punuo.sip.SipUserManager;
import com.punuo.sip.model.ResetData;
import com.punuo.sip.model.VideoData;
import com.punuo.sip.model.VolumeData;
import com.punuo.sip.request.SipByeRequest;
import com.punuo.sip.request.SipControlVolumeRequest;
import com.punuo.sip.request.SipResetRequest;
import com.punuo.sip.request.SipVideoRequest;
import com.punuo.sys.app.video.activity.model.deviddata;
import com.punuo.sys.app.video.activity.request.GetdevidRequest;
import com.punuo.sys.sdk.account.AccountManager;
import com.punuo.sys.sdk.fragment.BaseFragment;
import com.punuo.sys.sdk.httplib.HttpManager;
import com.punuo.sys.sdk.httplib.RequestListener;
import com.punuo.sys.sdk.util.BaseHandler;
import com.punuo.sys.sdk.util.CommonUtil;
import com.punuo.sys.sdk.util.StatusBarUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by han.chen.
 * Date on 2019-09-20.
 **/
@Route(path = VideoRouter.ROUTER_VIDEO_FRAGMENT)
public class VideoFragment extends BaseFragment implements BaseHandler.MessageHandler {

    @BindView(R2.id.title)
    TextView mTitle;
    @BindView(R2.id.sub_title)
    TextView mSubTitle;
    @BindView(R2.id.back)
    View mBack;
    @BindView(R2.id.play_status)
    ImageView mPlayStatus;
    @BindView(R2.id.play_music)
    View playMusic;
    @BindView(R2.id.play_video)
    View mPlayVideo;
    @BindView(R2.id.add_voice)
    View add_voice;
    @BindView(R2.id.down_voice)
    View down_voice;
    @BindView(R2.id.fast_video_play)
    FastVideoPlayer player;
    @BindView(R2.id.reset)
    View reset;

    private String devId;
    private boolean isPlaying = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFragmentView = inflater.inflate(R.layout.video_fragment_home, container, false);
        ButterKnife.bind(this, mFragmentView);
        //Toast.makeText(getActivity(),"请先确认已绑定设备再获取视频",Toast.LENGTH_LONG).show();
        getdevid();
        initView();
        View mStatusBar = mFragmentView.findViewById(R.id.status_bar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mStatusBar.getLayoutParams().height = StatusBarUtil.getStatusBarHeight(getActivity());
            mStatusBar.setVisibility(View.VISIBLE);
            mStatusBar.requestLayout();
        }
        EventBus.getDefault().register(this);
        return mFragmentView;
    }

    private void initView() {
        mBack.setVisibility(View.GONE);
        initTitle();
        initSubTitle();
        initTextureView();
        mPlayStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying) {
                    showLoadingDialogWithCancel("正在获取视频...", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            closeVideo();
                            player.stop();
                            player.release();
                            isPlaying = false;
                            dismissLoadingDialog();
                            mSubTitle.setEnabled(false);
                            mPlayStatus.setVisibility(View.VISIBLE);
                        }
                    });
                    startVideo(devId);
                }
            }
        });
        playMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ARouter.getInstance().build(VideoRouter.ROUTER_MUSIC_CHOOSE_ACTIVITY)
                        .navigation();
            }
        });
        mPlayVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ARouter.getInstance().build(VideoRouter.ROUTER_VIDEO_CHOOSE_ACTIVITY)
                        .navigation();
            }
        });
        down_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SipControlVolumeRequest sipControlVolumeRequest = new SipControlVolumeRequest("lower");
                SipUserManager.getInstance().addRequest(sipControlVolumeRequest);
            }
        });
        add_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SipControlVolumeRequest sipControlVolumeRequest = new SipControlVolumeRequest("raise");
                SipUserManager.getInstance().addRequest(sipControlVolumeRequest);
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SipResetRequest sipResetRequest=new SipResetRequest();
                SipUserManager.getInstance().addRequest(sipResetRequest);
            }
        });
    }

    private void initTextureView() {
        player.setLive(true);//是直播还是点播  false为点播
        player.setScaleType(FastVideoPlayer.SCALETYPE_FITXY);
        player.setTitle("梦视喂食器");
        player.setShowNavIcon(false);
        player.setHideControl(true);
        int width = CommonUtil.getWidth();
        player.getLayoutParams().height = H264Config.VIDEO_HEIGHT * width / H264Config.VIDEO_WIDTH;//H264Config.VIDEO_HEIGHT
    }

    private void initSubTitle() {
        mSubTitle.setVisibility(View.VISIBLE);
        mSubTitle.setText("停止播放");
        mSubTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeVideo();
                player.stop();
                player.release();
                isPlaying = false;
                mPlayStatus.setVisibility(View.VISIBLE);
                mSubTitle.setEnabled(false);
            }
        });
    }

    private void startVideo(String devId) {
        requestVideo(devId);
    }

    private void initTitle() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM月dd日", Locale.getDefault());
        String title = simpleDateFormat.format(new Date());
        mTitle.setText(title);
    }


    private void requestVideo(final String devId) {
        SipVideoRequest sipVideoRequest = new SipVideoRequest(devId);
        SipUserManager.getInstance().addRequest(sipVideoRequest);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (player != null) {
            player.onDestroy();
        }
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(VideoData event) {
        player.setUrl(event.mVideoUrl);
        player.play();
        isPlaying = true;
        mSubTitle.setEnabled(true);
        dismissLoadingDialog();
        mPlayStatus.setVisibility(View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ResetData result) {
        Toast.makeText(getActivity(), "开启成功", Toast.LENGTH_SHORT).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BindDevidSuccess result) {
        getdevid();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(VolumeData result) {
        if (TextUtils.equals(result.volume, "raise")) {
            Toast.makeText(getActivity(), "音量加", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.equals(result.volume, "lower")) {
            Toast.makeText(getActivity(), "音量减", Toast.LENGTH_SHORT).show();
        }
    }

    private GetdevidRequest mGetdevidRequest;

    public void getdevid() {
        if (mGetdevidRequest != null && !mGetdevidRequest.isFinish()) {
            return;
        }
        mGetdevidRequest = new GetdevidRequest();
        mGetdevidRequest.addUrlParam("userName", AccountManager.getUserName());
        mGetdevidRequest.setRequestListener(new RequestListener<deviddata>() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onSuccess(deviddata result) {
                if (result == null) {
                    return;
                }
                devId = result.devid;
            }

            @Override
            public void onError(Exception e) {

            }
        });
        HttpManager.addRequest(mGetdevidRequest);
    }

    private void closeVideo() {
        SipByeRequest sipByeRequest = new SipByeRequest(devId);
        SipUserManager.getInstance().addRequest(sipByeRequest);
    }

    @Override
    public void handleMessage(Message msg) {
    }
}
