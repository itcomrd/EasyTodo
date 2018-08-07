package com.itcomrd.easytodo;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class MainActivity extends Activity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{
	private final static String[] PERMISSIONS = {Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR};
	private final static int REQUEST_PERMISSIONS = 2;
	private Calendar m_St;
	private long		m_Stmsec;
	private TimeZone m_TimeZone;
	private int		m_Timeoffset;
	private Button	 m_Button;
	private Spinner m_Spinner;
	private int	m_Year;
	private int	m_Month;
	private int	m_Day;
	private Spinner m_Spinner2;

	public class CalendarInfo{
		long id;
		String name;
	}
	private ArrayList<CalendarInfo> m_CalendarInfoArray = new ArrayList<CalendarInfo>();

	// プロジェクション配列。
	// 取得したいプロパティの一覧を指定する。
	private static final String[] CALENDAR_PROJECTION = new String[] {
			CalendarContract.Calendars._ID,
			CalendarContract.Calendars.NAME,
			CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
	};
	private static final int CALENDAR_PROJECTION_IDX_ID = 0;
	private static final int CALENDAR_PROJECTION_IDX_NAME = 1;
	private static final int CALENDAR_PROJECTION_IDX_ACCESS_LEVEL = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//レイアウトの生成
		setContentView(R.layout.main);

		m_Spinner = (Spinner) findViewById(R.id.spinner);
		// ArrayAdapter を、string-array とデフォルトのレイアウトを引数にして生成
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.time_array, android.R.layout.simple_spinner_item);
		// 選択肢が表示された時に使用するレイアウトを指定
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// スピナーにアダプターを設定
		m_Spinner.setAdapter(adapter);
		m_Button = (Button) findViewById(R.id.button);
		m_Button.setOnClickListener(this);

		//ユーザーの利用許可のチェック
		if(checkPermissions()) {
			makeCalendarInfo();
		}

		m_TimeZone = TimeZone.getDefault ();
		m_Timeoffset = m_TimeZone.getRawOffset();

		// 日付情報の初期設定
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR); // 年
		int monthOfYear = calendar.get(Calendar.MONTH); // 月
		int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH); // 日
		// 日付設定ダイアログの作成・リスナの登録
		DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, year, monthOfYear, dayOfMonth);
		datePickerDialog.setMessage(getString(R.string.DatepickerTitle));
		datePickerDialog.setCancelable(false);
		// Dialog の Negative Button を設定
		datePickerDialog.setButton(
				DialogInterface.BUTTON_NEGATIVE,
				getString(R.string.Cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}
		);
		// 日付設定ダイアログの表示
		datePickerDialog.show();
	}

	private void makeCalendarInfo() {
		// クエリ条件を設定する
		Uri uri = CalendarContract.Calendars.CONTENT_URI;
		String[] projection = CALENDAR_PROJECTION;
		String selection = null;
		String[] selectionArgs = null;
		String sortOrder = null;

		// クエリを発行してカーソルを取得する
		ContentResolver cr = getContentResolver();
		Cursor cur = cr.query(uri, projection, selection, selectionArgs, sortOrder);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		while(cur.moveToNext()) {
			CalendarInfo info = new CalendarInfo();
			info.id = cur.getLong(CALENDAR_PROJECTION_IDX_ID);
			info.name = cur.getString(CALENDAR_PROJECTION_IDX_NAME);
			long level = cur.getLong(CALENDAR_PROJECTION_IDX_ACCESS_LEVEL);
			if(level != 200) {
				m_CalendarInfoArray.add(info);
				adapter.add(info.name);
			}
		}
		m_Spinner2 = (Spinner) findViewById(R.id.spinner2);
		m_Spinner2.setAdapter(adapter);
	}

	//ユーザーの利用許可のチェック
	private boolean checkPermissions() {
		//未許可
		boolean ret = isGranted();
		if (!ret) {
			//許可ダイアログの表示
			ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSIONS);
		}
		return ret;
	}

	//ユーザーの利用許可が済かどうかの取得
	private boolean isGranted() {
		for (int i  = 0; i < PERMISSIONS.length; i++) {
			if (PermissionChecker.checkSelfPermission(MainActivity.this, PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}

	//許可ダイアログ選択時に呼ばれる
	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] results) {
		if (requestCode == REQUEST_PERMISSIONS) {
			//未許可
			if (!isGranted()) {
				toast(getString(R.string.no_permition));
			}else{
				makeCalendarInfo();
			}
		} else {
			super.onRequestPermissionsResult(requestCode, permissions, results);
		}
	}

	@Override
	public void onClick(View view) {
		String title, loc, description;
		EditText	edit;
		long		duraionMsec;
		Uri uri = CalendarContract.Calendars.CONTENT_URI;

		duraionMsec = (m_Spinner.getSelectedItemPosition() + 1) * 1800000;

		edit = findViewById(R.id.EditText1);
		title = edit.getText().toString();
		edit = findViewById(R.id.EditText2);
		loc = edit.getText().toString();
		edit = findViewById(R.id.EditText3);
		description = edit.getText().toString();
		int i = m_Spinner2.getSelectedItemPosition();
		CalendarInfo info = m_CalendarInfoArray.get(i);

		ContentResolver cr = getContentResolver();

		ContentValues values = new ContentValues();
		values.put(CalendarContract.Events.CALENDAR_ID, info.id);
		values.put(CalendarContract.Events.TITLE, title);
		values.put(CalendarContract.Events.DESCRIPTION, description);
		values.put(CalendarContract.Events.EVENT_LOCATION, loc);
		values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
		values.put(CalendarContract.Events.DTSTART, m_Stmsec);
		values.put(CalendarContract.Events.DTEND, m_Stmsec + duraionMsec);
		uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
		// long eventId = Long.parseLong(uri.getLastPathSegment());
		finish();
	}

	@Override
	public void onDateSet(android.widget.DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
		m_Year = year;
		m_Month = monthOfYear;
		m_Day = dayOfMonth;

		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		TimePickerDialog dialog = new TimePickerDialog(MainActivity.this, this, 	hour, minute,true);
		dialog.setMessage(getString(R.string.TimepickerTitle));
		dialog.setCancelable(false);
		dialog.setButton(
				DialogInterface.BUTTON_NEGATIVE,
				getString(R.string.Cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}
		);
		dialog.show();
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

		m_St = Calendar.getInstance();
		m_St.set(m_Year, m_Month, m_Day, hourOfDay, minute);

		m_Stmsec = m_St.getTimeInMillis();

//		m_Stmsec -= m_Timeoffset;
	}

	//トーストの表示
	private void toast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}
}
