package com.mredrock.cyxbs.ui.widget;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Jay on 2017/8/3.
 */

public class FooterBehavior extends CoordinatorLayout.Behavior {
    public FooterBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        float translation = Math.abs(dependency.getTop());
        child.setTranslationY(translation);
        return true;
    }
}
