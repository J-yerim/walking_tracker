package com.example.b2001140_dbmap;

import static android.widget.Toast.makeText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    double currentLat, currentLog;
    Handler handler = new Handler();

    myDBHelper myHelper;
    SQLiteDatabase sqlDB;

    ListView listView;
    Button btnInsert, btnView, btnAll,btnNow;
    TextView textView;
    ListViewAdapter adapter;

    GoogleMap gMap;
    SupportMapFragment mapFragment;

    ArrayList<LatLng> arrayList;
    LocationManager locationManager;
    EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager =(LocationManager) getSystemService(Context.LOCATION_SERVICE);

        ActivityResultLauncher<String[]> permissionResult = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            Boolean fineLocationGranted = result.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false);
            Boolean coarseLocationGranted = result.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false);

            if(fineLocationGranted != null && fineLocationGranted){
                makeText(getApplicationContext(), "자세한 위치 권한이 허용됨", Toast.LENGTH_SHORT).show();
            } else if (coarseLocationGranted != null && coarseLocationGranted){
                makeText(getApplicationContext(), "대략적인 위치 권한이 허용됨", Toast.LENGTH_SHORT).show();
            }
            else {
                makeText(getApplicationContext(),"위치 권한이 허용 되지 않음", Toast.LENGTH_SHORT).show();
            }
        });


        if(ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            permissionResult.launch(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});

        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        arrayList = new ArrayList<LatLng>();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);//여기에 맵 싱크해라>온 맵 레디 실행된겨

        listView = (ListView) findViewById(R.id.listview);
        adapter = new ListViewAdapter();
        listView.setAdapter(adapter);
        myHelper = new myDBHelper(this);

        btnView=(Button)findViewById(R.id.button);
        btnInsert=(Button)findViewById(R.id.button2);
        editText =(EditText)findViewById(R.id.editTextText);
        textView =(TextView)findViewById(R.id.textView);
        btnAll=(Button)findViewById(R.id.button3);
        btnNow = (Button)findViewById(R.id.button4);


        myHelper = new myDBHelper(this);

        //init

        sqlDB = myHelper.getReadableDatabase();
        String sql ="select * from locationtable";
        Log.d("SQL",sql);
        //하나하나 가져오기 위해서 cursor
        Cursor cursor = sqlDB.rawQuery(sql,null); //커서로 데이터 넘어왔음

        while(cursor.moveToNext()){
            adapter.addItem(cursor.getInt(0),cursor.getString(1),cursor.getDouble(2),cursor.getDouble(3),cursor.getString(4));
        }

        adapter.notifyDataSetChanged(); //데이터 추가될때마다 업뎃해라

        cursor.close();
        sqlDB.close();



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                ListViewItem item = adapter.getItem(position);
                editText.setText(item.getDate());
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ListViewItem item = adapter.getItem(position);
                //item에서 adapter 통해서 아이템 번호랑 이름 가져올수있음
                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                dlg.setTitle("데이터 삭제");
                dlg.setMessage(item.getDate()+" 를 삭제 하시겠습니까?");
                dlg.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sqlDB = myHelper.getWritableDatabase();
                        String sql ="delete from locationtable  where no ="+item.getNo();
                        Log.d("SQL",sql);
                        sqlDB.execSQL(sql);
                        sqlDB.close();

                        final String urlStr = "https://tkddkq19.iwinv.net/lo_delete_ok.php?"+"no="+item.getNo();;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                requestUrl(urlStr);

                            }
                        }).start();

                        adapter.deleteItem(position);
                        adapter.notifyDataSetChanged();
                    }
                });
                dlg.setNegativeButton("취소",null);
                dlg.show();
                return false;
            }
        });

        btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
                gMap.clear();
                adapter.clearItems();
                adapter.notifyDataSetChanged();
                sqlDB = myHelper.getReadableDatabase();
                String sql ="select * from locationtable";
                Log.d("SQL",sql);
                //하나하나 가져오기 위해서 cursor
                Cursor cursor = sqlDB.rawQuery(sql,null);
                while(cursor.moveToNext()){
                    adapter.addItem(cursor.getInt(0),cursor.getString(1),cursor.getDouble(2),cursor.getDouble(3),cursor.getString(4));
                }
                adapter.notifyDataSetChanged();
                cursor.close();
                sqlDB.close();
            }
        });



        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gMap.clear();
                adapter.clearItems();
                adapter.notifyDataSetChanged();

                // locationtable에서 선택한 날짜의 정보 불러오기
                String selectedDate = editText.getText().toString();
                sqlDB = myHelper.getReadableDatabase();
                String sql = "select * from locationtable where date = '" + selectedDate + "'";
                Log.d("SQL", sql);
                // 하나하나 가져오기 위해서 cursor
                Cursor cursor = sqlDB.rawQuery(sql, null);


                List<LatLng> latLngList = new ArrayList<>();

                while (cursor.moveToNext()) {
                    adapter.addItem(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3), cursor.getString(4));
                    double lat=cursor.getDouble(2);
                    double log=cursor.getDouble(3);
                    LatLng latLng = new LatLng(lat, log);
                    latLngList.add(latLng);
                }

                adapter.notifyDataSetChanged(); // 데이터 추가될 때마다 업뎃해라

                if(latLngList.size()>0){
                    PolylineOptions polylineOptions1=new PolylineOptions();
                    polylineOptions1.addAll(latLngList);
                    polylineOptions1.color(Color.RED);
                    polylineOptions1.width(5);
                    gMap.addPolyline(polylineOptions1);
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngList.get(0),16));
                }

                cursor.close();
                sqlDB.close();
            }
        });

        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqlDB  = myHelper.getWritableDatabase();
                String sql = "insert into locationtable (date, lat, log, time)values ('"+g_date()+"','"+currentLat+"','"+currentLog+"','"+g_time()+"')";
                Log.d("SQL",sql);
                sqlDB.execSQL(sql);
                sqlDB.close();

               final String urlStr="https://tkddkq19.iwinv.net/lo_insert_ok.php?"+"date="+g_date().toString()+"&lat="+currentLat+"&log="+currentLog+"&time="+g_time().toString();
               new Thread(new Runnable() {
                   @Override
                   public void run() {
                       requestUrl(urlStr);
                   }
               }).start();

                sqlDB = myHelper.getReadableDatabase();
                sql ="select * from locationtable";
                Log.d("SQL",sql);
                //하나하나 가져오기 위해서 cursor
                Cursor cursor = sqlDB.rawQuery(sql,null);
                Log.d("cursor",cursor.toString());
                cursor.moveToLast();
                adapter.addItem(cursor.getInt(0),cursor.getString(1),cursor.getDouble(2),cursor.getDouble(3),cursor.getString(4));

                cursor.close();
                sqlDB.close();
                Toast.makeText(getApplicationContext(),"기록 완료",Toast.LENGTH_SHORT).show();
            }
        });

        btnNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLat,currentLog),16));

            }
        });


    }

    public String g_date(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String g_date = dateFormat.format(date);
        return g_date;
    }
    public String g_time(){
        long now=System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("kk:mm:ss");
        String g_time = dateFormat.format(date);
        return g_time;
    }

    public class myDBHelper extends SQLiteOpenHelper {

        public myDBHelper(@Nullable Context context) {
            super(context, "locationDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            String sql = "create table locationtable ("+"no integer primary key autoincrement,"+ "date char(20), lat double, log double, time char(20))";
            Log.d("SQL",sql);
            sqLiteDatabase.execSQL(sql); //sql 출력
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

            String sql = "drop table if exists locationtable";
            Log.d("SQL",sql);
            sqLiteDatabase.execSQL(sql); //sql 출력
            onCreate(sqLiteDatabase);
        }
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.945,126.683),16));


    }


    final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            //location 값이 바뀌었을때 인지하고 값 바꿔줌
            String provider = location.getProvider(); //제공자(gps 값)
            currentLat = location.getLatitude(); //위도
            currentLog = location.getLongitude(); //경도

            LatLng latLng=new LatLng(currentLat,currentLog);

            textView.setText(g_date()+", 위도: "+currentLat+", 경도: "+currentLog+", "+g_time());

            gMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));


        }
    };


    public void requestUrl(String urlStr){
        StringBuilder output = new StringBuilder();
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if(connection !=null){
                connection.setConnectTimeout(10000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);

                int resCode = connection.getResponseCode();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String line = null;
                while(true){
                    line = bufferedReader.readLine();
                    if(line==null) break;
                    output.append(line+"\n");
                }
                bufferedReader.close();
                connection.disconnect();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        println(output.toString());
    }

    public void println(String data){
        handler.post(new Runnable() {
            @Override
            public void run() {
                jsonParsing(data);
            }
        });
    }


    private void jsonParsing(String json){
        try{
            JSONObject jsonObject = new JSONObject(json);
            JSONArray locationArray = jsonObject.getJSONArray("Location");

            sqlDB = myHelper.getWritableDatabase();

            for(int i=0;i< locationArray.length();i++){
                JSONObject memberObject = locationArray.getJSONObject(i);

                Integer no = memberObject.getInt("no");
                String date =  memberObject.getString("date");
                double lat = memberObject.getDouble("lat");
                double log = memberObject.getDouble("log");
                String time = memberObject.getString("time");

                adapter.addItem(no, date, lat,log, time);

                String sql ="insert into locationtable values ("+no+ ", '"+date+ "', '"+lat+"', '"+log+"', '"+time+"')";
                Log.d("SQL",sql);
                sqlDB.execSQL(sql);

            }
            adapter.notifyDataSetChanged();
            sqlDB.close();

        }catch (JSONException e){
            e.printStackTrace();
        }
    }
}

