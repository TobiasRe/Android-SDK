/*
 * Copyright (C) 2014 SchedJoules
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.dmfs.webcal.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class TabBarLayout extends HorizontalScrollView
{

	private static final int LABEL_MARGIN_LEFT = 24;
	private static final int TAB_PADDING = 16;
	private static final int LABEL_TEXT_SIZE = 12;

	private int mLabelMarginLeft;
	private TabBar mTabBar;

	private int mTabViewLayoutId;
	private int mTabViewTextViewId;

	private ViewPager mViewPager;
	private ViewPager.OnPageChangeListener mViewPagerPageChangeListener;
	private boolean mDistributeEvenly = true;
	private SparseArray<String> mContentDescriptions = new SparseArray<String>();


	public TabBarLayout(Context context)
	{
		this(context, null);
	}


	public TabBarLayout(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}


	public TabBarLayout(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		setFillViewport(true);
		setHorizontalScrollBarEnabled(false);

		mLabelMarginLeft = (int) (LABEL_MARGIN_LEFT * getResources().getDisplayMetrics().density);

		mTabBar = new TabBar(context);
		addView(mTabBar, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	}


	/**
	 * Sets the colors to be used for indicating the selected tab. These colors are treated as a circular array. Providing one color will mean that all tabs are
	 * indicated with the same color.
	 */
	public void setSelectedIndicatorColors(int... colors)
	{
		mTabBar.setSelectedIndicatorColors(colors);
	}


	/**
	 * Sets the colors to be used for tab dividers. These colors are treated as a circular array. Providing one color will mean that all tabs are indicated with
	 * the same color.
	 */
	public void setDividerColors(int... colors)
	{
		mTabBar.setDividerColors(colors);
	}


	/**
	 * Set the {@link ViewPager.OnPageChangeListener}. When using {@link SlidingTabLayout} you are required to set any {@link ViewPager.OnPageChangeListener}
	 * through this method. This is so that the layout can update it's scroll position correctly.
	 *
	 * @see ViewPager#setOnPageChangeListener(ViewPager.OnPageChangeListener)
	 */
	public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener)
	{
		mViewPagerPageChangeListener = listener;
	}


	/**
	 * Set the custom layout to be inflated for the tab views.
	 *
	 * @param layoutResId
	 *            Layout id to be inflated
	 * @param textViewId
	 *            id of the {@link android.widget.TextView} in the inflated view
	 */
	public void setCustomTabView(int layoutResId, int textViewId)
	{
		mTabViewLayoutId = layoutResId;
		mTabViewTextViewId = textViewId;
	}


	/**
	 * Sets the associated view pager. Note that the assumption here is that the pager content (number of tabs and tab titles) does not change after this call
	 * has been made.
	 */
	public void setViewPager(ViewPager viewPager)
	{
		mTabBar.removeAllViews();

		mViewPager = viewPager;
		if (viewPager != null)
		{
			viewPager.setOnPageChangeListener(new InternalViewPagerListener());
			populateTabStrip();
		}
	}


	/**
	 * Create a default view to be used for tabs. This is called if a custom tab view is not set via {@link #setCustomTabView(int, int)}.
	 */
	protected TextView createDefaultTabView(Context context)
	{
		TextView textView = new TextView(context);
		textView.setGravity(Gravity.CENTER);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, LABEL_TEXT_SIZE);
		textView.setTypeface(Typeface.DEFAULT_BOLD);
		textView.setTextColor(Color.WHITE);
		textView.setSingleLine();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			// If we're running on Honeycomb or newer, then we can use the Theme's
			// selectableItemBackground to ensure that the View has a pressed state
			TypedValue outValue = new TypedValue();
			getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
			textView.setBackgroundResource(outValue.resourceId);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			// If we're running on ICS or newer, enable all-caps to match the Action Bar tab style
			textView.setAllCaps(true);
		}

		int padding = (int) (TAB_PADDING * getResources().getDisplayMetrics().density);
		textView.setPadding(padding, padding, padding, padding);

		return textView;
	}


	private void populateTabStrip()
	{

		final PagerAdapter adapter = mViewPager.getAdapter();
		final View.OnClickListener tabClickListener = new TabClickListener();
		int count = adapter.getCount();
		if (count == 0)
		{
			setVisibility(View.GONE);
		}
		else
		{
			setVisibility(View.VISIBLE);
		}

		for (int i = 0; i < count; i++)
		{
			View tabView = null;
			TextView tabTitleView = null;
			if (mTabViewLayoutId != 0)
			{
				// If there is a custom tab view layout id set, try and inflate it
				tabView = LayoutInflater.from(getContext()).inflate(mTabViewLayoutId, mTabBar, false);
				tabTitleView = (TextView) tabView.findViewById(mTabViewTextViewId);
			}
			if (tabView == null)
			{
				tabView = createDefaultTabView(getContext());
			}
			if (tabTitleView == null && TextView.class.isInstance(tabView))
			{
				tabTitleView = (TextView) tabView;
			}

			tabTitleView.setText(adapter.getPageTitle(i));
			tabView.setOnClickListener(tabClickListener);
			String desc = mContentDescriptions.get(i, null);
			if (desc != null)
			{
				tabView.setContentDescription(desc);
			}
			mTabBar.addView(tabView);

			if (mDistributeEvenly)
			{
				LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tabView.getLayoutParams();
				if (lp != null)
				{
					lp.weight = 1;
				}

			}
		}
	}


	public void notifyDataSetChanged()
	{
		populateTabStrip();

	}


	@Override
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();

		if (mViewPager != null)
		{
			scrollToTab(mViewPager.getCurrentItem(), 0);
		}
	}


	private void scrollToTab(int tabIndex, int positionOffset)
	{
		final int tabStripChildCount = mTabBar.getChildCount();
		if (tabStripChildCount == 0 || tabIndex < 0 || tabIndex >= tabStripChildCount)
		{
			return;
		}

		View selectedChild = mTabBar.getChildAt(tabIndex);
		if (selectedChild != null)
		{
			int targetScrollX = selectedChild.getLeft() + positionOffset;

			if (tabIndex > 0 || positionOffset > 0)
			{
				// If we're not at the first child and are mid-scroll, make sure we obey the offset
				targetScrollX -= mLabelMarginLeft;
			}

			scrollTo(targetScrollX, 0);
		}
	}

	public interface Colorizer
	{
		int getIndicatorColor(int position);


		int getDividerColor(int position);

	}

	private class InternalViewPagerListener implements ViewPager.OnPageChangeListener
	{
		private int mScrollState;


		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
		{
			int tabStripChildCount = mTabBar.getChildCount();
			if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount))
			{
				return;
			}

			mTabBar.onViewPagerPageChanged(position, positionOffset);

			View selectedTitle = mTabBar.getChildAt(position);
			int extraOffset = (selectedTitle != null) ? (int) (positionOffset * selectedTitle.getWidth()) : 0;
			scrollToTab(position, extraOffset);

			if (mViewPagerPageChangeListener != null)
			{
				mViewPagerPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
			}
		}


		@Override
		public void onPageScrollStateChanged(int state)
		{
			mScrollState = state;

			if (mViewPagerPageChangeListener != null)
			{
				mViewPagerPageChangeListener.onPageScrollStateChanged(state);
			}
		}


		@Override
		public void onPageSelected(int position)
		{
			if (mScrollState == ViewPager.SCROLL_STATE_IDLE)
			{
				mTabBar.onViewPagerPageChanged(position, 0f);
				scrollToTab(position, 0);
			}

			if (mViewPagerPageChangeListener != null)
			{
				mViewPagerPageChangeListener.onPageSelected(position);
			}
		}

	}

	private class TabClickListener implements View.OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			for (int i = 0; i < mTabBar.getChildCount(); i++)
			{
				if (v == mTabBar.getChildAt(i))
				{
					mViewPager.setCurrentItem(i);
					return;
				}
			}
		}
	}

}
