package com.punuo.pet.home.chart;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.punuo.pet.home.R;
import com.punuo.pet.home.R2;
import com.punuo.pet.home.device.model.ChartData;
import com.punuo.pet.home.device.model.ChartData2;
import com.punuo.pet.home.device.model.ChartData3;
import com.punuo.pet.home.device.request.ChartType;
import com.punuo.pet.home.device.request.GetFoodFrequencyRequest;
import com.punuo.pet.home.device.request.GetFoodNumberRequest;
import com.punuo.pet.home.device.request.GetSurplusFoodRequest;
import com.punuo.pet.router.HomeRouter;
import com.punuo.sys.sdk.account.AccountManager;
import com.punuo.sys.sdk.activity.BaseSwipeBackActivity;
import com.punuo.sys.sdk.httplib.HttpManager;
import com.punuo.sys.sdk.httplib.RequestListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by han.chen.
 * Date on 2020/5/9.
 **/
@Route(path = HomeRouter.ROUTER_CHART_ACTIVITY)
public class ChartActivity extends BaseSwipeBackActivity {

    @BindView(R2.id.left_text)
    TextView mLeftText;
    @BindView(R2.id.title)
    TextView mTitle;
    @BindView(R2.id.back)
    ImageView mBack;
    @BindView(R2.id.sub_title)
    TextView mSubTitle;
    @BindView(R2.id.chart_container_1)
    LinearLayout mChartContainer1;
    @BindView(R2.id.chart_container_2)
    LinearLayout mChartContainer2;
    @BindView(R2.id.chart_container_3)
    LinearLayout mChartContainer3;
    @BindView(R2.id.filter_1)
    Spinner mFilter1;
    @BindView(R2.id.filter_2)
    Spinner mFilter2;
    @BindView(R2.id.filter_3)
    Spinner mFilter3;

    private String[] ChartX1 = new String[4]; //柱状图水平坐标
    private String[] ChartX2 = new String[4]; //柱状图水平坐标
    private String[] ChartX3 = new String[4]; //柱状图水平坐标
    private int[] ChartData1 = new int[4]; //柱状图值
    private int[] ChartData2 = new int[4]; //柱状图值
    private int[] ChartData3 = new int[4]; //柱状图值

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_chart_activity);
        ButterKnife.bind(this);
        mTitle.setText(R.string.string_health_text);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollToFinishActivity();
            }
        });
        initData();
    }

    private void initData() {
        getFoodFrequency(ChartType.DAY);
        getFoodNumber(ChartType.DAY);
        getSurplusFood(ChartType.DAY);
        mFilter1.setSelection(0);
        mFilter1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String str = mFilter1.getSelectedItem().toString();
                mChartContainer1.removeAllViews();
                ChartType chartType = ChartType.DAY;
                if (getString(R.string.text_day).equals(str)) {
                    chartType = ChartType.DAY;
                }
                if (getString(R.string.text_week).equals(str)) {
                    chartType = ChartType.WEEK;
                }
                if (getString(R.string.text_month).equals(str)) {
                    chartType = ChartType.MONTH;
                }
                getFoodFrequency(chartType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mFilter2.setSelection(0);
        mFilter2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String str = mFilter2.getSelectedItem().toString();
                mChartContainer2.removeAllViews();
                ChartType chartType = ChartType.DAY;
                if (getString(R.string.text_day).equals(str)) {
                    chartType = ChartType.DAY;
                }
                if (getString(R.string.text_week).equals(str)) {
                    chartType = ChartType.WEEK;
                }
                if (getString(R.string.text_month).equals(str)) {
                    chartType = ChartType.MONTH;
                }
                getFoodNumber(chartType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mFilter3.setSelection(0);
        mFilter3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String str = mFilter3.getSelectedItem().toString();
                mChartContainer3.removeAllViews();
                ChartType chartType = ChartType.DAY;
                if (getString(R.string.text_day).equals(str)) {
                    chartType = ChartType.DAY;
                }
                if (getString(R.string.text_week).equals(str)) {
                    chartType = ChartType.WEEK;
                }
                if (getString(R.string.text_month).equals(str)) {
                    chartType = ChartType.MONTH;
                }
                getSurplusFood(chartType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private GetFoodFrequencyRequest mGetFoodFrequencyRequest;

    private void getFoodFrequency(ChartType chartType) {
        if (mGetFoodFrequencyRequest != null && !mGetFoodFrequencyRequest.isFinish()) {
            return;
        }
        mGetFoodFrequencyRequest = new GetFoodFrequencyRequest(chartType);
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
                ChartX1[0] = result.frequencyData.time1;
                ChartData1[0] = result.frequencyData.frequency1;
                ChartX1[1] = result.frequencyData.time2;
                ChartData1[1] = result.frequencyData.frequency2;
                ChartX1[2] = result.frequencyData.time3;
                ChartData1[2] = result.frequencyData.frequency3;
                ChartX1[3] = result.frequencyData.time4;
                ChartData1[3] = result.frequencyData.frequency4;
                barChart1();
            }

            @Override
            public void onError(Exception e) {

            }
        });
        HttpManager.addRequest(mGetFoodFrequencyRequest);
    }

    private GetFoodNumberRequest mGetFoodNumberRequest;

    private void getFoodNumber(ChartType chartType) {
        if (mGetFoodNumberRequest != null && !mGetFoodNumberRequest.isFinish()) {
            return;
        }
        mGetFoodNumberRequest = new GetFoodNumberRequest(chartType);
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
                ChartX2[0] = result.eatdata.time1;
                ChartData2[0] = result.eatdata.eat1;
                ChartX2[1] = result.eatdata.time2;
                ChartData2[1] = result.eatdata.eat2;
                ChartX2[2] = result.eatdata.time3;
                ChartData2[2] = result.eatdata.eat3;
                ChartX2[3] = result.eatdata.time4;
                ChartData2[3] = result.eatdata.eat4;
                barChart2();
            }

            @Override
            public void onError(Exception e) {

            }
        });
        HttpManager.addRequest(mGetFoodNumberRequest);
    }

    private GetSurplusFoodRequest mGetSurplusFoodRequest;

    private void getSurplusFood(ChartType chartType) {
        if (mGetSurplusFoodRequest != null && !mGetSurplusFoodRequest.isFinish()) {
            return;
        }
        mGetSurplusFoodRequest = new GetSurplusFoodRequest(chartType);
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
                ChartX3[0] = result.leftData.time1;
                ChartData3[0] = Integer.parseInt(result.leftData.lefted1);
                ChartX3[1] = result.leftData.time2;
                ChartData3[1] = Integer.parseInt(result.leftData.lefted2);
                ChartX3[2] = result.leftData.time3;
                ChartData3[2] = Integer.parseInt(result.leftData.lefted3);
                ChartX3[3] = result.leftData.time4;
                ChartData3[3] = Integer.parseInt(result.leftData.lefted4);
                barChart3();
            }

            @Override
            public void onError(Exception e) {

            }
        });
        HttpManager.addRequest(mGetSurplusFoodRequest);
    }

    private String[] handleYLabel(int[] data) {
        int step = getStep(data);
        if (step <= 0) {
            return null;
        } else {
            return new String[]{"0",
                    String.valueOf(step),
                    String.valueOf(step * 2),
                    String.valueOf(step * 3),
                    String.valueOf(step * 4),
                    String.valueOf(step * 5),
                    String.valueOf(step * 6),
                    String.valueOf(step * 7),
                    String.valueOf(step * 8),
                    String.valueOf(step * 9)};
        }
    }

    private int getStep(int[] data) {
        int max = 0;
        for (int datum : data) {
            if (max < datum) {
                max = datum;
            }
        }
        return (int) Math.ceil((double) (max / 9f));
    }


    //绘制柱状图函数
    private void barChart1() {
        String[] xLabel = {"", ChartX1[3], ChartX1[2], ChartX1[1], ChartX1[0]};
        String[] yLabel = handleYLabel(ChartData1);
        int step = getStep(ChartData1);
        if (yLabel == null) {
            yLabel = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
            step = 1;
        }
        int[] data1 = {ChartData1[3], ChartData1[2], ChartData1[1], ChartData1[0]};
        List<int[]> data = new ArrayList<>();
        data.add(data1);
        List<Integer> color = new ArrayList<>();
        color.add(R.color.colorAccent);
        color.add(R.color.colorPrimary);
        color.add(R.color.blue);
        mChartContainer1.addView(new CustomBarChart1(this, xLabel, yLabel, data, color, step));
    }

    private void barChart2() {
        String[] xLabel = {"", ChartX2[3], ChartX2[2], ChartX2[1], ChartX2[0]};
        String[] yLabel = handleYLabel(ChartData2);
        int step = getStep(ChartData2);
        if (yLabel == null) {
            yLabel = new String[]{"0", "100", "200", "300", "400", "500", "600", "700", "800", "900"};
            step = 100;
        }
        int[] data1 = {ChartData2[3], ChartData2[2], ChartData2[1], ChartData2[0]};
        List<int[]> data = new ArrayList<>();
        data.add(data1);
        List<Integer> color = new ArrayList<>();
        color.add(R.color.colorAccent);
        color.add(R.color.colorPrimary);
        color.add(R.color.blue);
        mChartContainer2.addView(new CustomBarChart(this, xLabel, yLabel, data, color, step));
    }

    private void barChart3() {
        String[] xLabel = {"", ChartX3[3], ChartX3[2], ChartX3[1], ChartX3[0]};
        String[] yLabel = handleYLabel(ChartData3);
        int step = getStep(ChartData3);
        if (yLabel == null) {
            yLabel = new String[]{"0", "100", "200", "300", "400", "500", "600", "700", "800", "900"};
            step = 100;
        }
        int[] data1 = {ChartData3[3], ChartData3[2], ChartData3[1], ChartData3[0]};
        List<int[]> data = new ArrayList<>();
        data.add(data1);
        List<Integer> color = new ArrayList<>();
        color.add(R.color.colorAccent);
        color.add(R.color.colorPrimary);
        color.add(R.color.blue);
        mChartContainer3.addView(new CustomBarChart(this, xLabel, yLabel, data, color, step));
    }
}
