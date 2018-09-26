package com.onebot.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.paginate.Paginate;
import com.paginate.recycler.LoadingListItemCreator;
import com.paginate.recycler.LoadingListItemSpanLookup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "WaldoApp";
    private static String MY_URL = "https://core-graphql.dev.waldo.photos/gql";
    private static int offset = 0;
    private static int limit = 21;
    private static String QUERY = "query {\n" +
            "  album(id: \"dTRydwXhGQgthi1r2cKFmg\") {\n" +
            "    id,\n" +
            "    name,\n" +
            "    photos(slice: { limit: " + limit + ", offset: + " + offset + " }) {\n" +
            "      count\n" +
            "      records {\n" +
            "        urls {\n" +
            "          size_code\n" +
            "          url\n" +
            "          width\n" +
            "          height\n" +
            "          quality\n" +
            "          mime\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private JSONArray results = new JSONArray();

    private RecyclerView rv;
    private MyAdapter myAdapter;

    private static int SPAN_COUNT = 3;

    private String albumName = "";
    private static Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ctx = getApplicationContext();

        rv = findViewById(R.id.recyclerView);

        // authorize the app, tbd - hide the credentials
        postAuth(this);

        // used to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        rv.setHasFixedSize(true);

        // user grid layout
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), SPAN_COUNT);
        rv.setLayoutManager(layoutManager);

        final RecyclerView.LayoutManager mLayoutManager = layoutManager;

    }

    private String getQuery() {
        QUERY =  "query {\n" +
                "  album(id: \"dTRydwXhGQgthi1r2cKFmg\") {\n" +
                "    id,\n" +
                "    name,\n" +
                "    photos(slice: { limit: " + limit + ", offset: + " + offset + " }) {\n" +
                "      count\n" +
                "      records {\n" +
                "        urls {\n" +
                "          size_code\n" +
                "          url\n" +
                "          width\n" +
                "          height\n" +
                "          quality\n" +
                "          mime\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        return QUERY;
    }

    /**
     * execute the query and process the results
     */
    private void lookup() {

        try {
            RequestQueue queue = Volley.newRequestQueue(this);

            loadingInProgress = true;

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, MY_URL + "?query=" + getQuery(),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // parse json
                            try {
                                JSONObject jsonObject = new JSONObject(response.toString());

                                // get the album name
                                JSONObject data = (JSONObject)jsonObject.get("data");
                                data = (JSONObject)data.get("album");
                                albumName = data.getString("name");

                                getSupportActionBar().setTitle("Waldo Album: " + albumName);

                                JSONArray urls = data.getJSONObject("photos").getJSONArray("records");

                                // check for more items
                                if (urls.length() == 0) {
                                    hasLoadedAllItems = true;
                                    loadingInProgress = false;
                                    return;
                                }

                                // create a simple list of URLs
                                for(int i=0;i<urls.length(); i++) {
                                    JSONObject urlJson = urls.getJSONObject(i);
                                    JSONArray urlArray = urlJson.getJSONArray("urls");
                                    urlJson = urlArray.getJSONObject(0);
                                    results.put(urlJson);
                                }

                                displayResults(results);

                                loadingInProgress = false;

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //mTextView.setText("That didn't work!");
                }
            });

            // Add the request to the RequestQueue.
            queue.add(stringRequest);

        } catch(Exception e)  {
            Log.e("caught: " + e, e.getMessage());

        }
    }

    private void displayResults(JSONArray results) {

        // set up adapter
        myAdapter = new MyAdapter(results);

        rv.setAdapter(myAdapter);

        // paginate
        Paginate.with(rv, callbacks)
                .setLoadingTriggerThreshold(2)
                .addLoadingListItem(true)
                .setLoadingListItemCreator(new CustomLoadingListItemCreator())
                .setLoadingListItemSpanSizeLookup(new LoadingListItemSpanLookup() {
                    @Override
                    public int getSpanSize() {
                        return SPAN_COUNT;
                    }
                })
                .build();

    }

    private class CustomLoadingListItemCreator implements LoadingListItemCreator {
        @Override
        /*public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_layout, parent, false);
            return new RecyclerView.ViewHolder(view);
        }*/
        public MyAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
            ImageView v = (ImageView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_layout, parent, false);
            MyAdapter.ViewHolder vh = new MyAdapter.ViewHolder(v);

            return vh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            // Bind custom loading row if needed
        }
    }


    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private JSONArray mDataset;

        // a reference to the views for each data item
        public static class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public ImageView mImageView;
            public ViewHolder(ImageView v) {
                super(v);
                mImageView = v;
            }
        }

        // construct with the data set
        public MyAdapter(JSONArray myDataset) {
            mDataset = myDataset;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
            ImageView v = (ImageView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_layout, parent, false);
            ViewHolder vh = new ViewHolder(v);

            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            try {
                // get element from your dataset at this position
                JSONObject obj = (JSONObject) mDataset.get(position);
                final String url = obj.getString("url");
                // use Glide to load the image
                GlideApp.with(ctx)
                        .load(url)
                        .override(400, 300)
                        .centerCrop()
                        .into(holder.mImageView);


                holder.mImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent fullScreenIntent = new Intent(ctx, FullscreenActivity.class);
                        fullScreenIntent.putExtra("URL", url);
                        ctx.startActivity(fullScreenIntent);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Return the size of the url list
        @Override
        public int getItemCount() {
            return mDataset.length();
        }
    }

    /**
     * Authenticate the client and set the cookie(s)
     * @param context
     */
    public void postAuth(Context context){

        // use cookies to save the auth cookie
        CookieManager manager = new CookieManager();
        CookieHandler.setDefault( manager  );

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest sr = new StringRequest(Request.Method.POST,"https://auth.dev.waldo.photos/",
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "response: " + response);
                lookup();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "volley error: " + error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("username", "waldo-android");
                params.put("password", "1234");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(sr);
    }

    private boolean loadingInProgress = false, hasLoadedAllItems = false;

    Paginate.Callbacks callbacks = new Paginate.Callbacks() {
        @Override
        public void onLoadMore() {
            offset += limit;
            lookup();
        }

        @Override
        public boolean isLoading() {
            // Indicate whether new page loading is in progress or not
            return loadingInProgress;
        }

        @Override
        public boolean hasLoadedAllItems() {
            // Indicate whether all data (pages) are loaded or not
            return hasLoadedAllItems;
        }
    };

}
