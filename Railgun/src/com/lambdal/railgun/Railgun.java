/**
  Railgun is a launcher for Google Glass. It lets you browse through the 
  installed non-system APKs on your device.

  This software has been modified from sample code and re-released under the
  Apache License 2.0.

  Modifications are
    by Stephen A. Balaban <s@lambdal.com>
    Copyright (c) 2013 Lambda Labs, Inc.

  Railgun modifies HorizontalPaging code provided by the AOSP.
  	https://developer.android.com/samples/HorizontalPaging/index.html

  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
 */
package com.lambdal.railgun;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;

/**
 * PackageInfo Type see:
 * http://www.androidsnippets.com/get-installed-applications-with
 * -name-package-name-version-and-icon
 */
class PInfo {
	public static Activity activity;
	public String appname = "";
	public String pname = "";
	public String versionName = "";
	public int versionCode = 0;
	public Drawable icon;

	public String toString() {
		return appname;
	}

	private void prettyPrint() {
		Log.v(this.toString(), "");
	}

	public static Activity getActivity() {
		return activity;
	}

	public static ArrayList<PInfo> getPackages() {
		ArrayList<PInfo> apps = getInstalledApps(false);
		final int max = apps.size();
		for (int i = 0; i < max; i++) {
			apps.get(i).prettyPrint();
		}
		return apps;
	}
	
	static private boolean isSystemPackage(PackageInfo pkgInfo) {
	    return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
	}

	public static ArrayList<PInfo> getInstalledApps(boolean getSysPackages) {
		ArrayList<PInfo> res = new ArrayList<PInfo>();
		List<PackageInfo> packs = getActivity().getPackageManager()
				.getInstalledPackages(0);
		for (int i = 0; i < packs.size(); i++) {
			PackageInfo p = packs.get(i);
			if ((!getSysPackages) && isSystemPackage(p)) {
				continue;
			}
			PInfo newInfo = new PInfo();
			newInfo.appname = p.applicationInfo.loadLabel(
					getActivity().getPackageManager()).toString();
			newInfo.pname = p.packageName;
			newInfo.versionName = p.versionName;
			newInfo.versionCode = p.versionCode;
			newInfo.icon = p.applicationInfo.loadIcon(getActivity()
					.getPackageManager());
			res.add(newInfo);
		}
		return res;
	}
}

public class Railgun extends FragmentActivity implements
		ActionBar.TabListener {

	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	
	private Integer currentSelectedTabIndex = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Set the activity
		PInfo.activity = this;
		int packageCount = PInfo.getPackages().size();
		
		// Hide the status bar.
		Window w = getWindow();
		w.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		w.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

		setContentView(R.layout.activity_main);
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.hide();
		// getActionBar().setBackgroundDrawable(new
		// ColorDrawable(Color.argb(128, 0, 0, 0)));
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());
		mSectionsPagerAdapter.pageCount = packageCount;

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});
	
		 mViewPager.setOnClickListener(
		            new ViewPager.OnClickListener() {
		                @Override
		                public void onClick(View view) {
		                    // When swiping between pages, select the
		                    // corresponding tab.
		                	int currentAppIndex = DummySectionFragment.currentAppIndex;
		                	ArrayList<PInfo> appList = PInfo.getInstalledApps(false);
		                	PInfo app = appList.get(currentSelectedTabIndex.intValue());
		                	// Open the app:
		                	Intent launchIntent = getPackageManager().getLaunchIntentForPackage(app.pname);
		                	try {
		                		startActivity(launchIntent);
		                	} catch (Exception e) {
		                		Log.v("We ran into an exception when launching intent: " + app.pname, e.toString());
		                	}
		                	Log.v("We have a tap event which happened at index: ", app.toString() + " " + currentSelectedTabIndex.toString());
		                }
		            });

		for (int i = 0; i < PInfo.getPackages().size(); i++) {
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		Integer pos = tab.getPosition();
		this.currentSelectedTabIndex = pos;
		mViewPager.setCurrentItem(pos);
	}
	
	/**
	 * Prevent accidental dismisses
	 */
	@Override
	public void onBackPressed() {
	    new AlertDialog.Builder(this)
	        .setTitle("Leave Railgun?")
	        .setMessage("Exit Railgun and return to the home screen?")
	        .setNegativeButton(android.R.string.no, null)
	        .setPositiveButton(android.R.string.yes, new OnClickListener() {
	            public void onClick(DialogInterface arg0, int arg1) {
	            	Log.v("Alert Result: ", "DONE");
	            	Railgun.super.onBackPressed();
	            }
	        }).create().show();
	}
	

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public int pageCount = 0;
        
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 20 total pages.
			return this.pageCount;
			
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			case 3:
				return getString(R.string.title_section4).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";
		public ImageView last;
		public static Integer currentAppIndex = 0;
		
		public DummySectionFragment() {
		}

		public static String joinArr(Object o, String j) {
			if (o == null || !o.getClass().isArray()) {
				return String.valueOf(o);

			} else {
				char beg = '[';
				char end = ']';
				final int len = Array.getLength(o);

				StringBuilder sb = new StringBuilder(beg);
				for (int i = 0; i < len; i++)
					sb.append(Array.get(o, i)).append(i + 1 < len ? j : "");

				return sb.append(end).toString();
			}
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			currentAppIndex = getArguments().getInt(ARG_SECTION_NUMBER) - 1;
			ArrayList<PInfo> listOfApps = PInfo.getInstalledApps(false);
			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
					container, false);
			

			Log.v("Got list: ", joinArr(listOfApps, ", "));
			PInfo app = listOfApps.get(currentAppIndex);

			Log.v("Got an app: ", app.toString());

			// Set icon
			Drawable icon = app.icon;
			ImageView image = (ImageView) rootView.findViewById(R.id.app_icon);

			if (image != null) {
				image.setImageDrawable(icon);
			}

			// Set text
			TextView dummyTextView = (TextView) rootView
					.findViewById(R.id.app_name);
			String textForSection = "END";
			textForSection = app.toString();
			dummyTextView.setText(textForSection);
			return rootView;
		}
	}

}
