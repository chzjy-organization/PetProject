package com.punuo.pet.home.module;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.makeramen.roundedimageview.RoundedImageView;
import com.punuo.pet.event.SelectDeviceEvent;
import com.punuo.pet.home.R;
import com.punuo.pet.home.R2;
import com.punuo.pet.home.device.model.ChartData;
import com.punuo.pet.home.device.model.ChartData2;
import com.punuo.pet.home.device.model.ChartData3;
import com.punuo.pet.home.device.request.GetFoodFrequencyRequest;
import com.punuo.pet.home.device.request.GetFoodNumberRequest;
import com.punuo.pet.home.device.request.GetSurplusFoodRequest;
import com.punuo.pet.home.view.PetLoopHolder;
import com.punuo.pet.home.view.request.GetRotationChart;
import com.punuo.pet.model.PetData;
import com.punuo.pet.model.PetModel;
import com.punuo.pet.router.DeviceType;
import com.punuo.pet.router.FeedRouter;
import com.punuo.pet.router.HomeRouter;
import com.punuo.pet.router.MemberRouter;
import com.punuo.sys.sdk.Constant;
import com.punuo.sys.sdk.account.AccountManager;
import com.punuo.sys.sdk.httplib.HttpManager;
import com.punuo.sys.sdk.httplib.RequestListener;
import com.punuo.sys.sdk.model.LoopModel;
import com.punuo.sys.sdk.util.MMKVUtil;
import com.punuo.sys.sdk.util.TimeUtils;
import com.punuo.sys.sdk.util.ToastUtils;
import com.punuo.sys.sdk.util.ViewUtil;
import com.punuo.sys.sdk.view.loopholder.LoopHolder;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by han.chen.
 * Date on 2019-08-15.
 **/
public class HomeHeadModule {

    @BindView(R2.id.header_container)
    LinearLayout mHeaderContainer;
    @BindView(R2.id.home_pet_info_container)
    LinearLayout mHomePetInfoContainer;
    @BindView(R2.id.home_add_pet)
    ImageView mHomeAddPet;
    @BindView(R2.id.home_pet_name)
    TextView mHomePetName;
    @BindView(R2.id.device_container)
    ViewGroup mDeviceContainer;

    @BindView(R2.id.feed_tools_container)
    View FeedToolsContainer;
    @BindView(R2.id.feed_pet)
    RoundedImageView mFeedPet;
    @BindView(R2.id.care_pet)
    RoundedImageView mCarePet;
    @BindView(R2.id.care_analyse)
    RoundedImageView mCareAnalyse;

    //    @BindView(R2.id.chart_container)
//    LinearLayout mChartContainer;
    @BindView(R2.id.home_ads_container)
    LinearLayout mHomeAdsContainer;

    @BindView(R2.id.device_tip)
    TextView mDeviceTip;

    @BindView(R2.id.device_display)
    LinearLayout mDeviceDisplay;
    @BindView(R2.id.device_feed)
    LinearLayout mDeviceFeed;
    @BindView(R2.id.device_maoce)
    LinearLayout mDeviceMaoce;

    @BindView(R2.id.device_selected_container)
    FrameLayout mDeviceSelectedContainer;
    @BindView(R2.id.device_choose)
    View deviceChoose;
    @BindView(R2.id.device_selected)
    ImageView mDeviceSelected;
    @BindView(R2.id.more_text)
    TextView mMoreText;


    private View mRootView;
    private PetLoopHolder mPetLoopHolder;
    private LoopHolder mLoopHolder;
    private Context mContext;
    private TextView mPetAge;
    private TextView mPetWeight;
    private PetData mCurrentPetData;

    private Spinner mSpinner;
    private String[] ChartX = new String[4]; //柱状图水平坐标
    private int[] ChartData = new int[4]; //柱状图值

    public HomeHeadModule(Context context, ViewGroup parent) {
        mContext = context;
        mRootView = LayoutInflater.from(context)
                .inflate(R.layout.home_head_module_layout, parent, false);
        ButterKnife.bind(this, mRootView);
        mPetLoopHolder = PetLoopHolder.newInstance(context, mHeaderContainer);
        mHeaderContainer.addView(mPetLoopHolder.getRootView());
//        mSpinner = mRootView.findViewById(R.id.space1);
        initPetInfo();
        initAdLoop();
        initView();
        getRotationChart();
        initDeviceSelect();
//        getFoodFrequency();
        ToastUtils.showToast("添加完宠物后请下拉刷新");
    }

    private void initDeviceSelect() {
        int deviceType = MMKVUtil.getInt("deviceType", DeviceType.UNKNOWN);
        selectDevice(deviceType);
        deviceChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ARouter.getInstance().build(HomeRouter.ROUTER_SELECT_DEVICE_ACTIVITY).navigation();
            }
        });
        mDeviceFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDevice(DeviceType.FEED);
                MMKVUtil.setInt("deviceType", DeviceType.FEED);
                EventBus.getDefault().post(new SelectDeviceEvent(DeviceType.FEED));
            }
        });

        mDeviceMaoce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.showToast("设备即将上线，敬请期待");
            }
        });
    }

    public void selectDevice(int deviceType) {
        switch (deviceType) {
            case DeviceType.FEED:
                FeedToolsContainer.setVisibility(View.VISIBLE);
                mDeviceDisplay.setVisibility(View.GONE);
                deviceChoose.setVisibility(View.VISIBLE);
                mDeviceTip.setText("当前设备：梦视智能宠物喂食器");
                mDeviceSelectedContainer.setVisibility(View.VISIBLE);
                mDeviceSelected.setImageDrawable(mContext.getResources().getDrawable(R.drawable.home_device_feed));
                mMoreText.setVisibility(View.GONE);
                break;
            case DeviceType.MAOCE:
                FeedToolsContainer.setVisibility(View.GONE);
                mDeviceDisplay.setVisibility(View.GONE);
                deviceChoose.setVisibility(View.VISIBLE);
                mDeviceTip.setText("当前设备：梦视智能宠物猫砂盆");
                mDeviceSelectedContainer.setVisibility(View.VISIBLE);
                mDeviceSelected.setImageDrawable(mContext.getResources().getDrawable(R.drawable.home_device_maoce));
                mMoreText.setVisibility(View.GONE);
                break;
            case DeviceType.UNKNOWN:
            default:
                mDeviceSelectedContainer.setVisibility(View.GONE);
                FeedToolsContainer.setVisibility(View.GONE);
                mDeviceDisplay.setVisibility(View.VISIBLE);
                deviceChoose.setVisibility(View.GONE);
                mMoreText.setVisibility(View.VISIBLE);
                mDeviceTip.setText("请选择设备以便获得更多功能");
                break;
        }
    }

    private void initView() {
//        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                String str = mSpinner.getSelectedItem().toString();
//                if ("出粮频率".equals(str)) {
//                    mChartContainer.removeAllViews();
//                    getFoodFrequency();
//                }
//                if ("出粮克数".equals(str)) {
//                    mChartContainer.removeAllViews();
//                    getFoodNumber();
//                }
//                if ("剩余克数".equals(str)) {
//                    mChartContainer.removeAllViews();
//                    getSurplusFood();
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });

        mHomeAddPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ARouter.getInstance().build(MemberRouter.ROUTER_ADD_PET_ACTIVITY)
                        .navigation();
            }
        });

        mFeedPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentPetData == null) {
                    ToastUtils.showToast("暂无宠物信息，请添加宠物");
                } else {
                    ARouter.getInstance().build(FeedRouter.ROUTER_FEED_HEALTH_ACTIVITY)
                            .withParcelable("petData", mCurrentPetData)
                            .navigation();
                }
            }
        });
        mCarePet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentPetData == null) {
                    ToastUtils.showToast("暂无宠物信息，请添加宠物");
                } else {
                    ARouter.getInstance().build(HomeRouter.ROUTER_CARE_ACTIVITY)
                            .withParcelable("petData", mCurrentPetData)
                            .navigation();
                }
            }
        });
    }

    /**
     * 初始化轮播广告
     */
    private void initAdLoop() {
        mLoopHolder = LoopHolder.newInstance(mContext, mHomeAdsContainer);
        mHomeAdsContainer.addView(mLoopHolder.mViewRoot);
    }

    /**
     * 刷新
     */
    public void refresh() {
        getRotationChart();
//        getFoodFrequency();
    }

    public View getRootView() {
        return mRootView;
    }

    /**
     * 初始化宠物信息
     */
    private void initPetInfo() {
        View mPetInfo = LayoutInflater.from(mContext).inflate(R.layout.home_pet_info_layout,
                mHomePetInfoContainer, false);
        mHomePetInfoContainer.addView(mPetInfo);
        mPetAge = mPetInfo.findViewById(R.id.pet_age);
        mPetWeight = mPetInfo.findViewById(R.id.pet_weight);
    }

    /**
     * 刷新宠物信息
     *
     * @param petModel
     */
    public void updateView(final PetModel petModel) {
        mPetLoopHolder.updateView(petModel);
        updatePet(0, petModel);
        mPetLoopHolder.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updatePet(position, petModel);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void updatePet(int position, PetModel model) {
        if (model.mPets.isEmpty()) {
            mHomePetInfoContainer.setVisibility(View.GONE);
            return;
        }
        mHomePetInfoContainer.setVisibility(View.VISIBLE);
        mCurrentPetData = model.mPets.get(position);
        Constant.petData = mCurrentPetData;
        ViewUtil.setText(mHomePetName, mCurrentPetData.petname);
        String age = TimeUtils.formatAge(TimeUtils.calAgeMonth(mCurrentPetData.birth));
        ViewUtil.setText(mPetAge, age);
        ViewUtil.setText(mPetWeight, String.valueOf(mCurrentPetData.weight));
    }

    private GetFoodFrequencyRequest mGetFoodFrequencyRequest;

    private void getFoodFrequency() {
        if (mGetFoodFrequencyRequest != null && !mGetFoodFrequencyRequest.isFinish()) {
            return;
        }
        mGetFoodFrequencyRequest = new GetFoodFrequencyRequest();
        mGetFoodFrequencyRequest.addUrlParam("userName", AccountManager.getUserName());
        mGetFoodFrequencyRequest.setRequestListener(new RequestListener<ChartData>() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onSuccess(ChartData result) {
                if (result == null) {
                    return;
                }
                if (result != null) {
                    ChartX[0] = result.frequencyData.time1;
                    ChartData[0] = result.frequencyData.frequency1;
                    ChartX[1] = result.frequencyData.time2;
                    ChartData[1] = result.frequencyData.frequency2;
                    ChartX[2] = result.frequencyData.time3;
                    ChartData[2] = result.frequencyData.frequency3;
                    ChartX[3] = result.frequencyData.time4;
                    ChartData[3] = result.frequencyData.frequency4;
                    barChart1();
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
        HttpManager.addRequest(mGetFoodFrequencyRequest);
    }

    private GetFoodNumberRequest mGetFoodNumberRequest;

    private void getFoodNumber() {
        if (mGetFoodNumberRequest != null && !mGetFoodNumberRequest.isFinish()) {
            return;
        }
        mGetFoodNumberRequest = new GetFoodNumberRequest();
        mGetFoodNumberRequest.addUrlParam("userName", AccountManager.getUserName());
        mGetFoodNumberRequest.setRequestListener(new RequestListener<ChartData2>() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onSuccess(ChartData2 result) {
                if (result == null) {
                    return;
                }
                if (result != null) {
                    ChartX[0] = result.eatdata.time1;
                    ChartData[0] = result.eatdata.eat1;
                    ChartX[1] = result.eatdata.time2;
                    ChartData[1] = result.eatdata.eat2;
                    ChartX[2] = result.eatdata.time3;
                    ChartData[2] = result.eatdata.eat3;
                    ChartX[3] = result.eatdata.time4;
                    ChartData[3] = result.eatdata.eat4;
                    barChart2();
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
        HttpManager.addRequest(mGetFoodNumberRequest);
    }

    private GetSurplusFoodRequest mGetSurplusFoodRequest;

    private void getSurplusFood() {
        if (mGetSurplusFoodRequest != null && !mGetSurplusFoodRequest.isFinish()) {
            return;
        }
        mGetSurplusFoodRequest = new GetSurplusFoodRequest();
        mGetSurplusFoodRequest.addUrlParam("userName", AccountManager.getUserName());
        mGetSurplusFoodRequest.setRequestListener(new RequestListener<ChartData3>() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onSuccess(ChartData3 result) {
                if (result == null) {
                    return;
                }
                if (result != null) {
                    ChartX[0] = result.leftData.time1;
                    int i1 = Integer.parseInt(result.leftData.lefted1);
                    ChartData[0] = i1;
                    ChartX[1] = result.leftData.time2;
                    int i2 = Integer.parseInt(result.leftData.lefted2);
                    ChartData[1] = i2;
                    ChartX[2] = result.leftData.time3;
                    int i3 = Integer.parseInt(result.leftData.lefted3);
                    ChartData[2] = i3;
                    ChartX[3] = result.leftData.time4;
                    int i4 = Integer.parseInt(result.leftData.lefted4);
                    ChartData[3] = i4;
                    barChart3();
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
        HttpManager.addRequest(mGetSurplusFoodRequest);
    }

    private GetRotationChart mGetRotationChart;

    private void getRotationChart() {
        if (mGetRotationChart != null && !mGetRotationChart.isFinish()) {
            return;
        }
        mGetRotationChart = new GetRotationChart();
        mGetRotationChart.addUrlParam("userName", AccountManager.getUserName());
        mGetRotationChart.addUrlParam("shop_id", 1);
        mGetRotationChart.setRequestListener(new RequestListener<JsonElement>() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onSuccess(JsonElement result) {
                if (result instanceof JsonArray) {
                    List<String> images = new ArrayList<>();
                    JsonArray jsonArray = result.getAsJsonArray();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        images.add("http://feeder.qinqingonline.com:8080/" + jsonArray.get(i).getAsString());
                    }
                    LoopModel loopModel = new LoopModel(images);
                    mLoopHolder.bindData(loopModel);
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
        HttpManager.addRequest(mGetRotationChart);
    }

    //绘制柱状图函数
    private void barChart1() {
        String[] xLabel = {"", ChartX[3], ChartX[2], ChartX[1], ChartX[0]};
        String[] yLabel = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        int[] data1 = {ChartData[3], ChartData[2], ChartData[1], ChartData[0]};
        List<int[]> data = new ArrayList<>();
        data.add(data1);
        List<Integer> color = new ArrayList<>();
        color.add(R.color.colorAccent);
        color.add(R.color.colorPrimary);
        color.add(R.color.blue);
//        mChartContainer.addView(new CustomBarChart1(mContext, xLabel, yLabel, data, color));
    }

    private void barChart2() {
        String[] xLabel = {"", ChartX[3], ChartX[2], ChartX[1], ChartX[0]};
        String[] yLabel = {"0", "100", "200", "300", "400", "500", "600", "700", "800", "900"};
        int[] data1 = {ChartData[3], ChartData[2], ChartData[1], ChartData[0]};
        List<int[]> data = new ArrayList<>();
        data.add(data1);
        List<Integer> color = new ArrayList<>();
        color.add(R.color.colorAccent);
        color.add(R.color.colorPrimary);
        color.add(R.color.blue);
//        mChartContainer.addView(new CustomBarChart(mContext, xLabel, yLabel, data, color));
    }

    private void barChart3() {
        String[] xLabel = {"", ChartX[3], ChartX[2], ChartX[1], ChartX[0]};
        String[] yLabel = {"0", "100", "200", "300", "400", "500", "600", "700", "800", "900"};
        int[] data1 = {ChartData[3], ChartData[2], ChartData[1], ChartData[0]};
        List<int[]> data = new ArrayList<>();
        data.add(data1);
        List<Integer> color = new ArrayList<>();
        color.add(R.color.colorAccent);
        color.add(R.color.colorPrimary);
        color.add(R.color.blue);
//        mChartContainer.addView(new CustomBarChart(mContext, xLabel, yLabel, data, color));
    }
}
