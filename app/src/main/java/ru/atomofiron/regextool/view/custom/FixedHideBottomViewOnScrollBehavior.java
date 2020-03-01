/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.atomofiron.regextool.view.custom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;

import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.bottomappbar.BottomAppBar;

/**
 * The {@link Behavior} for a View within a {@link CoordinatorLayout} to hide the view off the
 * bottom of the screen when scrolling down, and alpha_show it when scrolling up.
 */
public class FixedHideBottomViewOnScrollBehavior extends BottomAppBar.Behavior {

  protected static final int ENTER_ANIMATION_DURATION = 225;
  protected static final int EXIT_ANIMATION_DURATION = 175;

  private static final int STATE_SCROLLED_OFF = 1;
  private static final int STATE_SCROLLED_START = 2;

  private int height = 0;
  private int currentState = STATE_SCROLLED_START;
  private int additionalHiddenOffsetY = 0;
  @Nullable private ViewPropertyAnimator currentAnimator;

  public FixedHideBottomViewOnScrollBehavior() {}

  public FixedHideBottomViewOnScrollBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean onLayoutChild(
          @NonNull CoordinatorLayout parent, @NonNull BottomAppBar child, int layoutDirection) {
    ViewGroup.MarginLayoutParams paramsCompat =
            (ViewGroup.MarginLayoutParams) child.getLayoutParams();
    height = child.getMeasuredHeight() + paramsCompat.bottomMargin;
    return super.onLayoutChild(parent, child, layoutDirection);
  }

  /**
   * Sets an additional offset for the y position used to hide the view.
   *
   * @param child the child view that is hidden by this behavior
   * @param offset the additional offset in pixels that should be added when the view slides away
   */
  public void setAdditionalHiddenOffsetY(@NonNull BottomAppBar child, @Dimension int offset) {
    additionalHiddenOffsetY = offset;

    if (currentState == STATE_SCROLLED_OFF) {
      child.setTranslationY(height + additionalHiddenOffsetY);
    }
  }

  @Override
  public boolean onStartNestedScroll(
          @NonNull CoordinatorLayout coordinatorLayout,
          @NonNull BottomAppBar child,
          @NonNull View directTargetChild,
          @NonNull View target,
          int nestedScrollAxes,
          int type) {
    return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
  }

  @Override
  public void onNestedScroll(
          CoordinatorLayout coordinatorLayout,
          @NonNull BottomAppBar child,
          @NonNull View target,
          int dxConsumed,
          int dyConsumed,
          int dxUnconsumed,
          int dyUnconsumed/*,
          int type,
          @NonNull int[] consumed*/) {
    if (dyConsumed == 0 && dyUnconsumed == 0) {
      return;
    }
    boolean isTargetReversed = isTargetReversed(target);
    if (isTargetReversed) {
      dyConsumed *= -1;
      dyUnconsumed *= -1;
    }
    if (dyConsumed < 0) {
      slideStart(child);
    } if (dyConsumed > 0) {
      slideOff(child);
    } if (dyUnconsumed < 0) {
      slideStart(child);
    }
  }

  /**
   * Perform an animation that will slide the child from it's current position to be totally on the
   * screen.
   */
  public void slideStart(@NonNull BottomAppBar child) {
    if (currentState == STATE_SCROLLED_START) {
      return;
    }

    if (currentAnimator != null) {
      currentAnimator.cancel();
      child.clearAnimation();
    }
    currentState = STATE_SCROLLED_START;
    animateChildTo(
            child, 0, ENTER_ANIMATION_DURATION, AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
  }

  @Deprecated
  public void slideUp(@NonNull BottomAppBar child) {
    slideStart(child);
  }

  /**
   * Perform an animation that will slide the child from it's current position to be totally off the
   * screen.
   */
  public void slideOff(@NonNull BottomAppBar child) {
    if (currentState == STATE_SCROLLED_OFF) {
      return;
    }

    if (currentAnimator != null) {
      currentAnimator.cancel();
      child.clearAnimation();
    }
    currentState = STATE_SCROLLED_OFF;
    animateChildTo(
            child,
            height + additionalHiddenOffsetY,
            EXIT_ANIMATION_DURATION,
            AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR);
  }

  @Deprecated
  public void slideDown(@NonNull BottomAppBar child) {
    slideOff(child);
  }

  private void animateChildTo(
          @NonNull BottomAppBar child, int targetY, long duration, TimeInterpolator interpolator) {
    currentAnimator =
            child
                    .animate()
                    .translationY(targetY)
                    .setInterpolator(interpolator)
                    .setDuration(duration)
                    .setListener(
                            new AnimatorListenerAdapter() {
                              @Override
                              public void onAnimationEnd(Animator animation) {
                                currentAnimator = null;
                              }
                            });
  }

  private boolean isTargetReversed(View target) {
    if (target instanceof RecyclerView) {
      RecyclerView.LayoutManager layoutManager = ((RecyclerView) target).getLayoutManager();
      if (layoutManager instanceof LinearLayoutManager) {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
        return linearLayoutManager.getReverseLayout();
      }
    }
    return false;
  }
}