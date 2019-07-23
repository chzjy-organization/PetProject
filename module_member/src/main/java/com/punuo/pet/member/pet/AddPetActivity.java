package com.punuo.pet.member.pet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.punuo.pet.member.R;
import com.punuo.pet.member.login.fragment.NormalLoginFragment;
import com.punuo.pet.member.pet.fragment.AddPetFragment;
import com.punuo.pet.member.pet.fragment.AddUserInfoFragment;
import com.punuo.pet.member.pet.model.RequestParam;
import com.punuo.pet.member.pet.request.AddPetRequest;
import com.punuo.pet.router.MemberRouter;
import com.punuo.sys.sdk.account.AccountManager;
import com.punuo.sys.sdk.activity.BaseSwipeBackActivity;
import com.punuo.sys.sdk.httplib.HttpManager;
import com.punuo.sys.sdk.httplib.RequestListener;
import com.punuo.sys.sdk.model.BaseModel;
import com.punuo.sys.sdk.util.HandlerExceptionUtils;
import com.punuo.sys.sdk.util.ToastUtils;


/**
 * Created by han.chen.
 * Date on 2019-06-26.
 **/
@Route(path = MemberRouter.ROUTER_ADD_PET_ACTIVITY)
public class AddPetActivity extends BaseSwipeBackActivity {
    private static final int TYPE_ADD_PET = 1;
    private static final int TYPE_ADD_USER = 2;
    private TextView mTitle;
    private ImageView mBack;
    private TextView mSubTitle;
    private AddPetFragment mAddPetFragment;
    private AddUserInfoFragment mAddUserInfoFragment;
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet);

        initView();
    }

    private void initView() {
        mBack = (ImageView) findViewById(R.id.back);
        mTitle = (TextView) findViewById(R.id.title);
        mSubTitle = (TextView) findViewById(R.id.sub_title);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mTitle.setText("添加宠物");
        mSubTitle.setText("下一步");
        mSubTitle.setVisibility(View.VISIBLE);
        mSubTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.equals(mSubTitle.getText(), "下一步")) {
                    addPetInfo(mAddPetFragment.getRequestParam());
                } else if (TextUtils.equals(mSubTitle.getText(), "完成")) {
                    //TODO 提交表单
                }
            }
        });
        mFragmentManager = getSupportFragmentManager();
        switchFragment(TYPE_ADD_PET);
    }

    private void switchFragment(int type) {
        Fragment fragment = mFragmentManager.findFragmentById(R.id.content_container);
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        switch (type) {
            case TYPE_ADD_PET:
                if (fragment instanceof AddPetFragment) {
                    return;
                }
                if (mAddPetFragment == null) {
                    mAddPetFragment = AddPetFragment.newInstance();
                    mAddPetFragment.setArguments(bundle);
                }
                fragmentTransaction.replace(R.id.content_container, mAddPetFragment);
                fragmentTransaction.commitAllowingStateLoss();
                break;
            case TYPE_ADD_USER:
                if (fragment instanceof NormalLoginFragment) {
                    return;
                }
                if (mAddUserInfoFragment == null) {
                    mAddUserInfoFragment = AddUserInfoFragment.newInstance();
                    mAddUserInfoFragment.setArguments(bundle);
                }
                String tag = "addUserInfoFragment";
                fragmentTransaction.addToBackStack(tag);
                fragmentTransaction.replace(R.id.content_container, mAddUserInfoFragment, tag);
                fragmentTransaction.commitAllowingStateLoss();
                break;
            default:
                break;
        }
    }

    private AddPetRequest mAddPetRequest;

    private void addPetInfo(RequestParam requestParams) {
        if (mAddPetRequest != null && !mAddPetRequest.isFinish()) {
            return;
        }
        if (requestParams == null) {
            return;
        }
        mAddPetRequest = new AddPetRequest();
        mAddPetRequest.addUrlParam("type", requestParams.type);
        mAddPetRequest.addUrlParam("photo", requestParams.photo);
        mAddPetRequest.addUrlParam("petName", requestParams.petName);
        mAddPetRequest.addUrlParam("breed", requestParams.breed);
        mAddPetRequest.addUrlParam("birth", requestParams.birth);
        mAddPetRequest.addUrlParam("weight", requestParams.weight);
        mAddPetRequest.addUrlParam("unit",requestParams.unit);
        Log.e("单位是", ""+requestParams.unit);
        mAddPetRequest.addUrlParam("userName", AccountManager.getUserInfo().userName);
        mAddPetRequest.addUrlParam("unit", requestParams.unit);
        mAddPetRequest.setRequestListener(new RequestListener<BaseModel>() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onSuccess(BaseModel result) {
                if (result == null) {
                    return;
                }
                if (result.success) {
                    switchFragment(TYPE_ADD_USER);
                    mTitle.setText("添加主人信息");
                    mSubTitle.setText("完成");
                }
                if (!TextUtils.isEmpty(result.message)) {
                    ToastUtils.showToast(result.message);
                }
            }

            @Override
            public void onError(Exception e) {
                HandlerExceptionUtils.handleException(e);
            }
        });
        HttpManager.addRequest(mAddPetRequest);
    }

    @Override
    public void onBackPressed() {
        mFragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                mTitle.setText("添加宠物");
                mSubTitle.setText("下一步");
                mFragmentManager.removeOnBackStackChangedListener(this);
            }
        });
        super.onBackPressed();
    }
}