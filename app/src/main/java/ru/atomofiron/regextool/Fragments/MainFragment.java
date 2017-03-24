package ru.atomofiron.regextool.Fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ToggleButton;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

import ru.atomofiron.regextool.Adapters.FilesAdapter;
import ru.atomofiron.regextool.Adapters.ListAdapter;
import ru.atomofiron.regextool.Adapters.ViewPagerAdapter;
import ru.atomofiron.regextool.I;
import ru.atomofiron.regextool.MainActivity;
import ru.atomofiron.regextool.R;
import ru.atomofiron.regextool.SearchService;
import ru.atomofiron.regextool.Utils.Permissions;


public class MainFragment extends Fragment {

	private Activity ac;
	private View rootView;

	private EditText regexText;
	private ToggleButton caseToggle;
	private ToggleButton infilesToggle;
	private ToggleButton regexToggle;
	private ListView selectedListView;
	private ListView filesListView;

	private ListAdapter selectedListAdapter;
	private AlertDialog alertDialog;
	private Receiver dirReceiver;
	private SharedPreferences sp;
	private boolean needShowResults = true;
	private ViewPager viewPager;

	private I.SnackListener snackListener = null;
	private OnResultListener onResultListener = null;

	public MainActivity mainActivity;

	public MainFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if (rootView == null)
			rootView = inflater.inflate(R.layout.fragment_main, container, false);

		ac = getActivity();
		sp = I.SP(ac);

		if (sp.getString(I.STORAGE_PATH, null) == null)
			sp.edit().putString(I.STORAGE_PATH,
							Environment.getExternalStorageDirectory().getAbsolutePath()).apply();

		regexText = (EditText)rootView.findViewById(R.id.regex_text);
		regexText.addTextChangedListener(new RegexWatcher());
		caseToggle = (ToggleButton)rootView.findViewById(R.id.case_senc);
		infilesToggle = (ToggleButton)rootView.findViewById(R.id.in_files);
		regexToggle = (ToggleButton)rootView.findViewById(R.id.simple_search);
		regexToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				checkPatternValid();
			}
		});

		ButtonListener listener = new ButtonListener();
		rootView.findViewById(R.id.go).setOnClickListener(listener);
		rootView.findViewById(R.id.slash).setOnClickListener(listener);
		rootView.findViewById(R.id.box).setOnClickListener(listener);
		rootView.findViewById(R.id.nobox).setOnClickListener(listener);
		rootView.findViewById(R.id.dot).setOnClickListener(listener);
		rootView.findViewById(R.id.star).setOnClickListener(listener);
		rootView.findViewById(R.id.dash).setOnClickListener(listener);
		rootView.findViewById(R.id.roof).setOnClickListener(listener);
		rootView.findViewById(R.id.buck).setOnClickListener(listener);
		dirReceiver = new Receiver();
		ac.registerReceiver(dirReceiver, new IntentFilter(I.toMainActivity));

		viewPager = (ViewPager)rootView.findViewById(R.id.view_pager);
		ArrayList<View> viewList = new ArrayList<>();

		selectedListView = new ListView(ac);
		selectedListAdapter = new ListAdapter(ac);
		selectedListAdapter.checkable = true;
		selectedListAdapter.update();
		selectedListView.setAdapter(selectedListAdapter);
		selectedListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				selectedListAdapter.remove(position);
				return false;
			}
		});
		selectedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				selectedListAdapter.onItemClick(position);
			}
		});

		filesListView = new ListView(ac);
		final FilesAdapter filesListAdapter = new FilesAdapter(ac, filesListView);
		filesListView.setAdapter(filesListAdapter);
		filesListAdapter.update(new File(sp.getString(I.STORAGE_PATH, "/")));

		viewList.add(selectedListView);
		viewList.add(filesListView);
		ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(ac, viewList);
		viewPager.setAdapter(pagerAdapter);

		((TabLayout) rootView.findViewById(R.id.tab_layout))
				.setupWithViewPager(viewPager);
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
			public void onPageSelected(int position) {
				if (position == 0)
					selectedListAdapter.update();
				else
					filesListAdapter.updateSelected();
			}
			public void onPageScrollStateChanged(int state) {}
		});

		return rootView;
	}

	public void checkListForSearch() {
		if (selectedListAdapter.getCheckedCount() == 0) {
			Snack(R.string.no_checked);
		} else
			search();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
			if (requestCode == I.REQUEST_FOR_SEARCH)
				checkListForSearch();
	}

	public void search() {
		if (alertDialog == null)
			alertDialog = new AlertDialog.Builder(ac)
					.setView(R.layout.layout_searching)
					.setCancelable(false)
					.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							needShowResults=false;
							ac.stopService(new Intent(ac, SearchService.class));
						}
					})
					.setNegativeButton(R.string.stop, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ac.stopService(new Intent(ac, SearchService.class));
						}
					})
					.create();
		alertDialog.show();
		ac.startService(new Intent(ac, SearchService.class)
				.putExtra(I.CASE_SENSE, caseToggle.isChecked())
				.putExtra(I.SEARCH_LIST, selectedListAdapter.getPathArray())
				.putExtra(I.REGEX, regexText.getText().toString())
				.putExtra(I.SEARCH_IN_FILES, infilesToggle.isChecked())
				.putExtra(I.SEARCH_REGEX, regexToggle.isChecked()));
	}

	private void checkPatternValid() {
		if (regexToggle.isChecked())
			try {
				Pattern.compile(regexText.getText().toString());
				regexText.getBackground().clearColorFilter();
			} catch (Exception ignored) {
				regexText.getBackground().setColorFilter(
						getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
			}
		else
			regexText.getBackground().clearColorFilter();
	}

	public void setSnackListener(I.SnackListener listener) {
		snackListener = listener;
	}
	public void setOnResultListener(OnResultListener listener) {
		onResultListener = listener;
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		ac.unregisterReceiver(dirReceiver);
		ac.stopService(new Intent(ac, SearchService.class));
	}

	private void Snack(int id) {
		if (snackListener != null)
			snackListener.Snack(getString(id));
	}

	private void Snack(String str) {
		if (snackListener != null)
			snackListener.Snack(str);
	}




// -------------------------------------------------------------

	public interface OnResultListener {
		public void onResult(Bundle bundle);
	}

	private class ButtonListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			I.Log("onClick()");
			String symbol;
			switch (v.getId()) {
				case R.id.go:
					needShowResults = true;
					String regex = regexText.getText().toString();
					if (!regexToggle.isChecked())
						try { Pattern.compile(regex);
						} catch (Exception ignored) {
							Snack(R.string.bad_ex);
							return;
						}
					if (regex.length() > 0 && Permissions.checkPerm(mainActivity, I.REQUEST_FOR_SEARCH))
						checkListForSearch();
					return;
				case R.id.box:
					symbol = "[]";
					break;
				case R.id.nobox:
					symbol = "{}";
					break;
				default:
					symbol = ((Button)v).getText().toString();
					break;
			}
			int start = regexText.getSelectionStart();
			regexText.getText().insert(start, symbol);
			regexText.setSelection(start, start);
		}
	}

	class Receiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			int i = intent.getIntExtra(I.SEARCH_CODE, I.SEARCH_ERROR);
			I.Log("onReceive(): "+i);
			alertDialog.cancel();
			switch (i) {
				case I.SEARCH_ERROR:
					Snack(R.string.error);
					break;
				case I.SEARCH_NOTHING:
					if (needShowResults)
						Snack(R.string.nothing);
					break;
				default:
					if (needShowResults) {
						Snack(getString(R.string.results, i));
						if (onResultListener != null)
							onResultListener.onResult(intent.getExtras());
					}
					break;
			}
		}
	}

	private class RegexWatcher implements TextWatcher {

		private boolean need = true;
		private int start = 0;
		private int before = 0;
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			this.start = start;
			this.before = before;
		}
		@Override
		public void afterTextChanged(Editable s) {
			if (!need)
				return;
			if (before == 1 && s.length() > start &&
					(s.charAt(start) == ']' || s.charAt(start) == '}')) {
				need = false;
				s.replace(start, start+1, "");
				need = true;
			}

			checkPatternValid();
		}
	}
}
