package com.zhouxiansheng.lazyloadfragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

/**
 * FileName: LazyLoadFragment
 * Author: ZhouXianSheng
 * Date: 2022/7/13
 * Description: 懒加载Fragment
 */
public abstract class LazyLoadFragment extends Fragment {
    private static final String TAG = "LazyLoadFragment";

    private View rootView = null;
    private boolean isViewCreated;
    private boolean isFirstVisible = true;
    private boolean isCurrentVisible;

    protected abstract int getLayoutRes();
    protected abstract void initView(View view);

    protected abstract void onFirstLoad();
    protected abstract void onLoad();
    protected abstract void onStopLoad();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //TODO 下面这行有待商榷
        super.onCreateView(inflater, container, savedInstanceState);
        if (rootView == null) {
            rootView = inflater.inflate(getLayoutRes(), container, false);
        }
        initView(rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isViewCreated = true;
        //TODO 这里需要多看看
        if(!isHidden() && getUserVisibleHint()) {
            setUserVisible(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!isFirstVisible) {
            if(!isHidden() && !isCurrentVisible && getUserVisibleHint()) {
                setUserVisible(true);
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.i(TAG, "setUserVisibleHint: "+isVisibleToUser);
        if(isViewCreated) {
            if(isVisibleToUser && !isCurrentVisible) {
                setUserVisible(true);
            }else if(!isVisibleToUser && isCurrentVisible) {
                setUserVisible(false);
            }
        }
    }

    private void setUserVisible(boolean isVisible) {
        if(isCurrentVisible == isVisible || (isVisible && !isParentVisible())){
            return;
        }
        isCurrentVisible = isVisible;
        if(isVisible) {
            if(isFirstVisible) {
                isFirstVisible = false;
                onFirstLoad();
            }else{
                onLoad();
            }
            setChildUserVisible(true);
        }else{
            onStopLoad();
            setChildUserVisible(false);
        }
    }

    private boolean isParentVisible() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof LazyLoadFragment) {
            LazyLoadFragment fragment = (LazyLoadFragment)parentFragment;
            return fragment.isCurrentVisible();
        }
        return true;
    }

    private boolean isCurrentVisible() {
        return isCurrentVisible;
    }

    private void setChildUserVisible(boolean visible) {
        FragmentManager fragmentManager = getChildFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment: fragments) {
            if (fragment instanceof LazyLoadFragment &&
                    !fragment.isHidden() &&
                    fragment.getUserVisibleHint()) {
                ((LazyLoadFragment)fragment).setUserVisible(visible);
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        setUserVisible(!hidden);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(isCurrentVisible && getUserVisibleHint()) {
            setUserVisible(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isViewCreated = false;
        isFirstVisible = false;
    }
}

