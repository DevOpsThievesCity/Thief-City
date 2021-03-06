package com.dot.thievescity;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.dot.thievescity.utils.GpsTracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;
import com.parse.FindCallback;
//mport com.parse.LiveQueryException;
import com.parse.FunctionCallback;
import com.parse.LiveQueryException;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
//import com.parse.ParseLiveQueryClient;
import com.parse.ParseLiveQueryClient;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SubscriptionHandling;
//import com.parse.SubscriptionHandling;
import android.os.Handler;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
//import java.util.logging.Handler;


public class SecondGamePlayActivity extends AppCompatActivity implements OnMapReadyCallback {

    public GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    GpsTracker gpsTracker;
    //public LatLng lastKnownLocation;
    String username = "myUserName";
    List<Gem> myGems = new ArrayList<Gem>(), allGems = new ArrayList<Gem>();
    int gemIndex=0;
   // CountDownTimer myTimer;
    Date date;
    boolean loaded = true;
    List<Gem> grabGems = new ArrayList<Gem>();
    List<Gem> nearGems = new ArrayList<Gem>();
    float minDistance = 10.0f;
    ListView listView;
    NotificationCompat.Builder notification;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    int notificationIndex = 0;
    LinearLayout linearLayout;
    ImageButton imageButton;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<Integer> imageIds;
    List<Polygon> permittedPolygons, restrictedPolygons;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_game_play);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //if(isGameFinished)finishGame();
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar_second);
        setSupportActionBar(myToolbar);
        myToolbar.setTitleTextColor(Color.WHITE);
        initializeRV();
        Log.i("here", 107+"");
        initializeBagRV();
        initializeFab();
       // listView = (ListView)findViewById(R.id.grab_gem_list);
        username  = ParseUser.getCurrentUser().getUsername();
        sharedPreferences = this.getSharedPreferences("com.dot.thievescity", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putInt("activityOrder", 2);
        editor.apply();
        loadMyGems();
        loadAllGems();
        //initializeListView();
        //initializeBag();
        //linearLayout = (LinearLayout)findViewById(R.id.grab_gem_layout);
        //imageButton = (ImageButton)LayoutInflater.from(this).inflate(R.layout.gem_button,null);
        //imageButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        //linearLayout.addView(imageButton);
        //bagDataInitialize();
        //allGems.get(allGems.indexOf("Sanath"));
        //startGemPlacementProcess();

    }

    View bagRV;
    void initializeFab()
    {
        bagRV = findViewById(R.id.bag_view);
        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.fab_button);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(bagRV.getVisibility() == View.VISIBLE)
                    bagRV.setVisibility(View.INVISIBLE);
                else
                    bagRV.setVisibility(View.VISIBLE);
            }
        });
    }


    void initializeRV()
    {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        imageIds = new ArrayList<>();
        //imageIds.add(R.drawable.images_god);
        //imageIds.add(R.drawable.erer);
        mAdapter = new MainAdapter(imageIds);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, mRecyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        // do whatever
                        onGrabGem(position);
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );


    }

    @Override
    public void onBackPressed() {
        // Simply Do noting!
    }


    private RecyclerView bagRecyclerView;
    private RecyclerView.LayoutManager bagLayoutManager;
    private RecyclerView.Adapter bagmAdapter;
    private ArrayList<Integer> bagImageIds;

    void initializeBagRV()
    {
        bagRecyclerView = (RecyclerView) findViewById(R.id.bag_view);
        bagRecyclerView.setHasFixedSize(true);
        bagLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);
        bagRecyclerView.setLayoutManager(bagLayoutManager);
        bagImageIds = new ArrayList<>();
        //bagImageIds.add(R.drawable.images_god);
        //bagImageIds.add(R.drawable.erer);
        bagmAdapter = new MainAdapter(bagImageIds);
        bagRecyclerView.setAdapter(bagmAdapter);
        bagRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, bagRecyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        // do whatever
                        placeGem(bagGems.get(position));
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );

    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                gpsTracker =new GpsTracker(this, mMap, SecondGamePlayActivity.this);
                startLocationService();
                return;
            }
           // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            //gpsTracker.getLocation();
        }
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

       // if(Build.VERSION.SDK_INT < 23)
         //   gpsTracker =new GpsTracker(this, mMap, SecondGamePlayActivity.this);
        resizedMarker();
        resizedMarker();
        checkPermissionAndStart();


    }

    public void checkPermissionAndStart()
    {
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        else{
            gpsTracker =new GpsTracker(this, mMap, SecondGamePlayActivity.this);
            startLocationService();
        }
    }

    void startLocationService()
    {
        Location lastKnownLocation =null;
       // if(gpsTracker.location==null)
         //   gpsTracker.getLocation();
        if(gpsTracker.canGetLocation())
        {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(13.023727,76.102132),18));
        }
        else Toast.makeText(this,"Can't get location",Toast.LENGTH_SHORT).show();
        PolygonClass polygonClass = new PolygonClass(mMap);
        polygonClass.drawPolygons();
        permittedPolygons = polygonClass.permittedPolygons;
        restrictedPolygons = polygonClass.restrictedPolygons;

    }

    public void restartGPS()
    {
        gpsTracker.stopUsingGPS();
        gpsTracker = null;
       // if(Build.VERSION.SDK_INT < 23)
         //   gpsTracker =new GpsTracker(this, mMap, SecondGamePlayActivity.this);
        checkPermissionAndStart();
    }

    List<Gem> liveObjects = new ArrayList<>();
    Handler handlerLoad;
    //Handler liveHandler;
    Runnable r1,r2;
    void startSubscription()
    {
      /*  ParseLiveQueryClient parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient();
        //ParseLiveQueryClient parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient(new URI("ws://myparseinstance.com"));
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Gem");

        SubscriptionHandling<ParseObject> subscriptionHandling = parseLiveQueryClient.subscribe(parseQuery);

        subscriptionHandling.handleEvents(new SubscriptionHandling.HandleEventsCallback<ParseObject>() {
            @Override
            public void onEvents(ParseQuery<ParseObject> query, SubscriptionHandling.Event event, ParseObject object) {
                // HANDLING all events
                Gem gem = parseObjectToGem(object);
                liveObjects.add(gem);
                Log.i("Live","318");
                Toast.makeText(getApplicationContext(),"Username:"+gem.username ,Toast.LENGTH_SHORT).show();

            }
        });

        subscriptionHandling.handleError(new SubscriptionHandling.HandleErrorCallback<ParseObject>() {
            @Override
            public void onError(ParseQuery<ParseObject> query, LiveQueryException exception) {
                Toast.makeText(getApplicationContext(),"Error", Toast.LENGTH_LONG).show();
            }
        });
        //Don't forget to add this handler
        liveHandler = new Handler();
        liveHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(liveObjects.size()==0)
                {
                    liveHandler.postDelayed(this,300);
                    return;
                }
                for(Iterator<Gem> iterator = liveObjects.iterator(); iterator.hasNext();) {
                    //Gem gem = liveObjects.get(0);
                    Gem gem = iterator.next();
                    Gem requiredGem = findGem(gem.id);
                    if(gem.id.equals("myID")) {
                        finishGame();
                        return;
                    }
                    //if(requiredGem == null)
                    //createToast("243");
                    //boolean yours = requiredGem.username.equals(username);
                    if (requiredGem != null && requiredGem.username.equals(username) && !gem.username.equals(username))
                        notifyGemLost(gem);
                    if (requiredGem == null) {
                        requiredGem = new Gem(gem.type, gem.username);
                        allGems.add(requiredGem);
                    }
                    updateGem(requiredGem, gem);
                    if (!requiredGem.username.equals(username) && requiredGem.marker != null) {
                        requiredGem.marker.remove();
                        requiredGem.marker = null;
                    }
                    if (requiredGem.username.equals(username) && requiredGem.marker == null)
                        addMarker(requiredGem);
                    Log.i("Here", gem.username);
                    iterator.remove();
                }
                liveHandler.postDelayed(this,300);
            }
        },300);

*/

       // if(isGameFinished)return;
        handlerLoad = new Handler();
        r1 = new Runnable() {
            @Override
            public void run() {
                int time = 4000;
                if(loaded) {
                    loaded = false;
                    //Log.i("Here", "218");
                    ParseQuery query = new ParseQuery<ParseObject>("Gem");
                    query.orderByAscending("updatedAt");
                    query.whereGreaterThan("updatedAt", date);
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            try {
                                checkGameFinished();
                                for (ParseObject object : objects) {
                                    date = object.getUpdatedAt();
                                    Gem gem = parseObjectToGem(object);
                                    //Toast.makeText(getApplicationContext(),"Username:"+gem.username ,Toast.LENGTH_SHORT).show();
                                    Gem requiredGem = findGem(gem.id);
                                    //if(requiredGem == null)
                                    //  createToast("243");
                                    //boolean yours = requiredGem.username.equals(username);
                                    if (requiredGem != null && requiredGem.username.equals(username) && !gem.username.equals(username))
                                        notifyGemLost(gem);
                                    if (requiredGem == null) {
                                        requiredGem = new Gem(gem.type, gem.username);
                                        allGems.add(requiredGem);
                                    }
                                    updateGem(requiredGem, gem);
                                    if (!requiredGem.username.equals(username) && requiredGem.marker != null) {
                                        requiredGem.marker.remove();
                                        requiredGem.marker = null;
                                    }
                                    if (requiredGem.username.equals(username) && requiredGem.marker == null)
                                        addMarker(requiredGem);
                                    Log.i("Here", "StartSubscription");

                                }

                                loaded = true;
                            }
                            catch (Exception ex) {
                                // createToast("Handler:Done:Query:Background");
                            }
                        }




                    });
                }
                else
                    time = 1000;
                if(!isGameFinished) {
                    handlerLoad.postDelayed(this, time);
                }
            }
        };
        handlerLoad.postDelayed(r1, 4000);


    }
    boolean finishLd = true;
    void checkGameFinished()
    {
        if(!finishLd)
            return;
        finishLd = false;
        ParseQuery parseQuery = new ParseQuery("Finish");
        parseQuery.whereEqualTo("objectId", "UgGe9RTYwG");
        parseQuery.setLimit(1);
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> finished, ParseException e) {
                finishLd= true;
                if(finished.get(0).getBoolean("isFinished"))
                finishGame();
            }
        });
    }

    void notifyGemLost(Gem gem)
    {
        if(gem ==null) {
            createToast("Something Null gem notify");
            return;
        }
        int gemImageId = getGemImageId(gem.type, false);
        String type = gemType(gem.type);
        notification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("Gem Lost")
                .setContentText("You lost a " + type + " gem")
                .setSmallIcon(gemImageId)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setPriority(NotificationManager.IMPORTANCE_HIGH);

        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationIndex,notification.build());
        notificationIndex++;
    }

    boolean isPlaceable(Location location)
    {
        boolean isInPermittedRegion = false, isInRestrictedRegion = false;
        for(Polygon polygon : permittedPolygons)
        {
            if(isLocationInPolygon(polygon,location))
            {
                isInPermittedRegion = true;
                break;
            }
        }
        for(Polygon polygon : restrictedPolygons)
        {
            if(isLocationInPolygon(polygon,location))
            {
                isInRestrictedRegion = true;
                break;
            }
        }

        return isInPermittedRegion && !isInRestrictedRegion;
    }

    public boolean isLocationInPolygon(Polygon polygon, Location location)
    {
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        return PolyUtil.containsLocation(latLng,polygon.getPoints(), false);

    }



    void updateGem(Gem requiredGem, Gem gem)
    {
        if(requiredGem == null || gem ==null) {
           // createToast("Something Null gem or required gem");
            return;
        }
        requiredGem.id = gem.id;
        requiredGem.isPlaced = gem.isPlaced;
        requiredGem.location = gem.location;
        requiredGem.username = gem.username;
        requiredGem.type = gem.type;
    }

    Gem findGem(String objectId)
    {
       for(Gem gem : allGems){
           if(gem.id!=null)
           if(gem.id.equals(objectId))
               return gem;

       }
       //createToast("Returned Null from findGem 324");
       return null;
    }



   /* void startGemPlacementProcess()
    {
        final TextView timerText = (TextView)findViewById(R.id.timer_text);
        int timeForEachGem = 60*1000;
        // Gem myGem = myGems.get(gemIndex);
        myTimer = new CountDownTimer(timeForEachGem, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText(millisUntilFinished / 1000 + "");
                Log.i("Time:", millisUntilFinished / 1000 + "");
            }

            public void onFinish() {
                Toast.makeText(getApplicationContext(), "Automatically placing at current location", Toast.LENGTH_LONG).show();
                placeGem();
            }
        }.start();



        //Log.i("DoneTimer", "Done");

    } */

    Location location2;
    public void placeGem(Gem gem){
        //When user presses place button
        try {
            if(gem.isPlaced)
                return;
            Location cLocation = gpsTracker.location;
            if(!isPlaceable(cLocation))
            {
                createToast("Cannot place here");
                return;
            }
            final int type = gem.type;
            final String objectId = gem.id;
            //GpsTracker gpsTracker = new GpsTracker(this);
            ParseQuery query = new ParseQuery("Gem");
            query.whereEqualTo("objectId", gem.id);
            query.setLimit(1);
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e != null) {
                        Toast.makeText(getApplicationContext(), "Network Error!", Toast.LENGTH_SHORT).show();
                        // return;
                    } else {
                        ParseObject parseObject = objects.get(0);
                        if (gpsTracker.location == null) {
                            Toast.makeText(getApplicationContext(), "PLace failed! Try again", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ParseGeoPoint point = new ParseGeoPoint(gpsTracker.location.getLatitude(), gpsTracker.location.getLongitude());
                        parseObject.put("location", point);
                        parseObject.put("isPlaced", true);
                        location2 = gpsTracker.location;
                        parseObject.put("type", type);
                        parseObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null)
                                    Toast.makeText(getApplicationContext(), "Place failed", Toast.LENGTH_SHORT).show();
                                else {
                                    Toast.makeText(getApplicationContext(), "Place Successful!", Toast.LENGTH_SHORT).show();
                                    Gem gem = findGem(objectId);
                                    if (gem != null) {
                                        gem.isPlaced = true;
                                        gem.location = location2;
                                    }
                                    addMarker(gem);
                                    updateBagUI(gem, false);
                                }

                            }
                        });
                    }

                }
            });
        }
        catch (Exception e)
        {
           // createToast("placeGem exception");
        }


        //Location currentLocation = gpsTracker.location;

    }

    Bitmap forMarker(int type)
    {
        switch (type)
        {
            case 0: return diamond;
            case 1: return ruby;
            case 2: return emerald;
        }
        return emerald;
    }
    Bitmap diamond,ruby,emerald;
    void resizedMarker()
    {
        int height = 80;
        int width = 80;
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.gem_diamondd);
        Bitmap b=bitmapdraw.getBitmap();
        diamond = Bitmap.createScaledBitmap(b, width, height, false);
        BitmapDrawable bitmapdraw2=(BitmapDrawable)getResources().getDrawable(R.drawable.gem_ruby);
        Bitmap b2=bitmapdraw2.getBitmap();
        ruby = Bitmap.createScaledBitmap(b2, width, height, false);
        BitmapDrawable bitmapdraw3=(BitmapDrawable)getResources().getDrawable(R.drawable.gem_em);
        Bitmap b3=bitmapdraw3.getBitmap();
        emerald = Bitmap.createScaledBitmap(b3, width, height, false);

    }


    void loadSecondGamePlayActivity()
    {
        Intent secondGamePlayActivity = new Intent(getApplicationContext(),SecondGamePlayActivity.class);
        startActivity(secondGamePlayActivity);
    }
    // Display layout
    void loginDetails(){
        //Fetching user id and password
    }



    void loadMyGems(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Gem");
        query.whereEqualTo("username", username);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e== null){
                    myGems.clear();
                    for(ParseObject object : objects){
                        Gem gem = parseObjectToGem(object);
                        LatLng latLng = new LatLng(gem.location.getLatitude(), gem.location.getLongitude());
                        //gem.marker = marker;
                        myGems.add(gem);

                    }
                    int totGems = 15;
                    if(myGems.size()< totGems)
                    {
                        notification = new NotificationCompat.Builder(getApplicationContext())
                                .setContentTitle("Gems lost")
                                .setContentText("You lost "+ (totGems-myGems.size()) + " gem(s)")
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                                .setPriority(NotificationManager.IMPORTANCE_HIGH);

                        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                        notificationManager.notify(103,notification.build());
                    }

                }

                else
                    Toast.makeText(getApplicationContext(),"Network Error!", Toast.LENGTH_SHORT).show();
            }

        });

    }

    Gem parseObjectToGem(ParseObject object)
    {
        if( object ==null) {
            //createToast("ParseObjectToGem:object Null");
            return null;
        }
        ParseGeoPoint parseGeoPoint = object.getParseGeoPoint("location");
        Location location = new Location("dummyProvider");
        location.setLatitude(parseGeoPoint.getLatitude());
        location.setLongitude(parseGeoPoint.getLongitude());
        Gem gem = new Gem(object.getObjectId(),object.getInt("type"), object.getString("username"), location, object.getBoolean("isPlaced"));
        return gem;
    }
//TODO: Call this periodically
    void loadAllGems()
    {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Gem");
        //query.whereNotEqualTo("username", username);
        query.orderByAscending("updatedAt");
        //query.whereEqualTo("isPlaced", true);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e== null){
                    allGems = new ArrayList<Gem>();
                    for(ParseObject object : objects){
                        Gem gem = parseObjectToGem(object);
                        if(gem.username.equals(username) && gem.isPlaced)
                        addMarker(gem);
                        if(gem.username.equals(username) && !gem.isPlaced)
                        {
                            if(grabGemObjectIds.indexOf(gem.id)==-1) {
                                totalGemsInBag++;
                                updateBagUI(gem, true);
                            }
                        }
                        allGems.add(gem);
                        date = object.getUpdatedAt();
                    }
                    startSubscription();
                    findGemsAtCurrentLocation();
                }

                else
                    Toast.makeText(getApplicationContext(),"Data loading failed", Toast.LENGTH_SHORT).show();
            }

        });



    }

    void addMarker(Gem gem)
    {
        if( gem ==null) {
           // createToast("addMarker gem null");
            return;
        }
        Location gemLocation = gem.location;
        LatLng latLng = new LatLng(gemLocation.getLatitude(), gemLocation.getLongitude());
        gem.marker = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(forMarker(gem.type))));
    }


    public void loadGemsNearBy()
    {
        float distanceWithin = 50.0f;
        nearGems.clear();
        //
        Location location = gpsTracker.location;
        for(Gem gem : allGems)
        {
            if(location.distanceTo(gem.location) <= distanceWithin)
                nearGems.add(gem);
        }

    }
    float distanceToGrab = 5.3f;
    ArrayList<Gem> takeGems = new ArrayList<>();
    Handler handlerFindGem;
    void findGemsAtCurrentLocation()
    {
        try {
            minDistance = 10.0f;

           // if(isGameFinished)return;
            final Location location = gpsTracker.location;
            handlerFindGem = new Handler();
            r2 = new Runnable() {
                @Override
                public void run() {
                    // grabGems.clear();
                    ArrayList<Gem> temp = new ArrayList<Gem>(nearGems);
                    for (Gem gem : temp) {
                        float distance = location.distanceTo(gem.location);
                        if (distance <= minDistance) {
                            if (findGrabGem(gem.id) == null && !gem.username.equals(username)) {
                                grabGems.add(gem);
                                if(distance > distanceToGrab)
                                    updateGrabGemUI(gem, true, false);
                            }
                        } else {
                            if (findGrabGem(gem.id) != null) {
                                grabGems.remove(gem);
                                takeGems.remove(gem);
                                updateGrabGemUI(gem, false, false);
                            }
                        }

                    }

                    for(Gem gem : grabGems)
                    {
                        float distance = location.distanceTo(gem.location);
                        if(distance <= distanceToGrab)
                        {
                            if(takeGems.indexOf(gem)==-1){
                                takeGems.add(gem);
                                updateGrabGemUI(gem,true,true);
                            }
                        }
                        else
                        {
                            if(takeGems.indexOf(gem)!=-1) {
                                updateGrabGemUI(gem, true, false);
                                takeGems.remove(gem);
                            }
                        }
                    }
                    if(!isGameFinished)
                        handlerFindGem.postDelayed(this, 200);
                    else
                    {
                        h2 = true;
                        handlerFindGem.removeCallbacksAndMessages(null);
                    }
                }
            };
            handlerFindGem.postDelayed(r2, 200);
        }
        catch (Exception e)
        {
            //createToast("Exception:findGemsAtCurrentLocation");
        }
    }
    static boolean h1= false,h2 = false;

    Gem findGrabGem(String objectId)
    {
        for(Gem gem : grabGems){
            if(gem!=null && gem.id != null)
            if(gem.id.equals(objectId))
                return gem;

        }
        return null;
    }

    Gem findTakeGem(String objectId)
    {
        for(Gem gem : takeGems){
            if(gem != null && gem.id != null)
                if(gem.id.equals(objectId))
                    return gem;

        }
        return null;
    }





    List<String> grabGemObjectIds = new ArrayList<String>();

    void updateGrabGemUI(Gem gem , boolean add, boolean takable)
    {
        if(gem == null) {
           // createToast("updateGramGemUI: gem null");
            return;
        }
        int gemImage = getGemImageId(gem.type, true);
        String gemName = gemType(gem.type);
        if(takable)
            gemImage = getGemImageId(gem.type,false);
        if(add){
           if(grabGemObjectIds.indexOf(gem.id)==-1) {
               grabGemObjectIds.add(gem.id);
               //ids.add(gemName);
               imageIds.add(gemImage);
               mAdapter.notifyItemInserted(imageIds.size()-1);
           }
           else
           {
               int position = grabGemObjectIds.indexOf(gem.id);
              // ids.set(position,gemName);
               imageIds.set(position, gemImage);
               mAdapter.notifyItemChanged(position);

           }
           //adapter.notifyDataSetChanged();

        }
        else
        {
            try {
                int position = grabGemObjectIds.indexOf(gem.id);
                //ids.remove(position);
                imageIds.remove(position);
                //adapter.notifyDataSetChanged();
                mAdapter.notifyItemRemoved(position);
                grabGemObjectIds.remove(gem.id);
                Log.i("Here", "566");
            }
            catch (Exception e)
            {
                Log.i("MyException", e.toString());
            }


        }
    }

    int getGemImageId (int type, boolean dark)
    {
        switch (type)
        {
            case 0: if(!dark)return  R.drawable.gem_diamondd;else return R.drawable.gem_diamondd_dark;
            case 1: if(!dark) return R.drawable.gem_ruby;else return R.drawable.gem_ruby_dark;
            case 2: if(!dark) return R.drawable.gem_em; else return R.drawable.gem_em_dark;
         }
         return 0;
    }

    ArrayList<String> ids;
    ArrayAdapter adapter;

    void initializeListView()
    {
       // listView = (ListView)findViewById(R.id.grab_gem_list);
        ids = new ArrayList<String>();
        ids.add("first");
        adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, ids);
        listView.setAdapter(adapter);
        adapter.remove("first");
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

               // Toast.makeText(getApplicationContext(), "Clicked!", Toast.LENGTH_SHORT).show();
                onGrabGem(i);

            }
        });
    }
    ListView bagListView;
    List<String> bagIds;
    ArrayAdapter bagAdapter;

    void initializeBag()
    {
       // bagListView = (ListView)findViewById(R.id.bag_list_view);
        bagIds = new ArrayList<String>();
        bagIds.add("first");
        bagAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, bagIds);
        bagListView.setAdapter(bagAdapter);
        bagAdapter.remove("first");
        bagListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

               // Toast.makeText(getApplicationContext(), "Clicked!", Toast.LENGTH_SHORT).show();
                placeGem(bagGems.get(i));


            }
        });
    }

    ArrayList<Gem> bagGems = new ArrayList<>();
    ArrayList<String> bagGemObjectIds = new ArrayList<>();
    void updateBagUI(Gem gem , boolean add)
    {
        if( gem ==null) {
            //createToast("gem null UpdateBagUI");
            return;
        }
        String gemName = gemType(gem.type);
        int gemImage = getGemImageId(gem.type, false);

        if(add){
            ///bagAdapter.add(gemName);
            bagImageIds.add(gemImage);
            bagmAdapter.notifyItemInserted(bagImageIds.size()-1);
            bagGemObjectIds.add(gem.id);
            bagGems.add(gem);
        }
        else
        {
            try {
                int position = bagGemObjectIds.indexOf(gem.id);
               // bagAdapter.remove(position);
                ///bagIds.remove(position);
                bagImageIds.remove(position);
                ///bagAdapter.notifyDataSetChanged();
                bagmAdapter.notifyItemRemoved(position);
                bagGemObjectIds.remove(gem.id);
                boolean isThere = bagGems.remove(gem);
                if(!isThere)
                {
                    createToast("NO ELEMENT!!!");
                }
                else
                    totalGemsInBag--;
            }
            catch (Exception e)
            {
                Log.i("Not found", "Element not found");
            }

        }
    }

    void createToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    String gemType(int type){
        if(type == 0)
            return "Diamond";
        else if(type == 1)
            return "Ruby";
        else
            return "Emerald";
    }

    int bagCapacity = 3, totalGemsInBag = 0;
    public void onGrabGem(int position){

        try {
            String id = grabGemObjectIds.get(position);
            Gem gem = findGem(id);
            if(takeGems.indexOf(gem) == -1)
            {
                createToast("Cannot Grab!");
                return;
            }
            if (totalGemsInBag >= bagCapacity) {
                Toast.makeText(getApplicationContext(), "Bag Full!", Toast.LENGTH_SHORT).show();
                return;
            }
            totalGemsInBag++;

            updateGrabGemUI(gem, false, false);
            //parseCloudGrab(gem);
            //from here
            ParseQuery query = ParseQuery.getQuery("Gem");
            query.whereEqualTo("objectId", id);
            query.setLimit(1);
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> objects, ParseException e) {
                    if (objects.size() != 0 && objects.get(0).getBoolean("isPlaced"))
                        grabGem(objects.get(0));
                    else {
                        Toast.makeText(getApplicationContext(), "Someone grabbed it already!", Toast.LENGTH_SHORT).show();
                        totalGemsInBag--;
                    }

                }
            });
            //to here

        }
        catch (Exception e)
        {
           // createToast("Here is the bug! 752");
        }


       // loadAllGems();
    }

    void grabGem(ParseObject object)
    {
        try {
            final String objectId = object.getObjectId();
            object.put("isPlaced", false);
            object.put("location", new ParseGeoPoint(0, 0));
            object.put("username", username);
            object.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Toast.makeText(getApplicationContext(), "Successfully grabbed!", Toast.LENGTH_SHORT).show();
                        Gem gem = findGem(objectId);
                        updateGrabGemUI(gem, false, false);
                        gem.isPlaced = false;
                        gem.username = username;
                        updateBagUI(gem, true);
                    } else {
                        Toast.makeText(getApplicationContext(), "Network Error!", Toast.LENGTH_SHORT).show();
                        Gem gem = findGrabGem(objectId);
                        updateGrabGemUI(gem, true, false);
                        totalGemsInBag--;
                    }
                }
            });
        }
        catch (Exception e)
        {
           // createToast("Exception:grabGem");
        }
    }

    void parseCloudGrab(Gem gem)
    {
        HashMap<String, Object> params = new HashMap<String, Object>();
        final String gemId = gem.id;
        params.put("parseObjectId",gem.id);
        params.put("username", username);
        ParseCloud.callFunctionInBackground("grabGem", params, new FunctionCallback<Integer>() {
            public void done(Integer result, ParseException e) {
                if (e == null) {
                    if(result == 1)
                    {
                        Toast.makeText(getApplicationContext(), "Successfully grabbed!", Toast.LENGTH_SHORT).show();
                        Gem gem = findGem(gemId);
                        updateGrabGemUI(gem, false, false);
                        gem.isPlaced = false;
                        gem.username = username;
                        updateBagUI(gem, true);
                    }
                    else
                        if(result == -2)
                        {
                            Toast.makeText(getApplicationContext(), "Someone grabbed it already!", Toast.LENGTH_SHORT).show();
                            totalGemsInBag--;
                        }
                        else
                            if(result == 0 || result == -1)
                            {
                                Toast.makeText(getApplicationContext(), "Network Error!", Toast.LENGTH_SHORT).show();
                                Gem gem = findGrabGem(gemId);
                                updateGrabGemUI(gem, true, false);
                                totalGemsInBag--;
                            }


                }
                else {

                    Toast.makeText(getApplicationContext(), "Network Error!", Toast.LENGTH_SHORT).show();
                    Gem gem = findGrabGem(gemId);
                    updateGrabGemUI(gem, true, false);
                    totalGemsInBag--;

                }
            }
        });
    }

    void saveBagData()
    {
        editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String bagGemsJson = gson.toJson(bagGems);
            String bagOId = gson.toJson(bagGemObjectIds);
            editor.putString("bagGems", bagGemsJson);
            editor.putString("bagGemObjectIds",bagOId);
            editor.apply();

    }

    void bagDataInitialize()
    {
        ArrayList<Gem> bagGemsT = new ArrayList<>();
        try {
           String s = sharedPreferences.getString("bagGems", "noData");
            if(s.equals("noData"))
                return;
            Gson gson = new Gson();
            Type listOfGems = new TypeToken<List<Gem>>(){}.getType();
            bagGemsT = (ArrayList) gson.fromJson(s,listOfGems);
            for(Gem gem : bagGemsT)
            {
                updateBagUI(gem,true);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public static boolean isGameFinished = false;
    boolean resultLoaded = false;
    void finishGame()
    {
        if(!resultLoaded)
        {
            isGameFinished = true;
            if(gpsTracker!=null)
            gpsTracker.stopUsingGPS();
            if(handlerFindGem!=null&& handlerLoad!=null) {
                handlerFindGem.removeCallbacks(r2);
                handlerLoad.removeCallbacks(r1);
            }
            createToast("Game finished");
            //liveHandler.removeCallbacksAndMessages(null);
            Intent resultActivity = new Intent(getApplicationContext(),ResultActivity.class);
            startActivity(resultActivity);
            resultLoaded = true;
            finish();
        }
    }



}

