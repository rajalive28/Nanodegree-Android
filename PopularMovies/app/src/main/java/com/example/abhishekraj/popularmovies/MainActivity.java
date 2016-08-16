package com.example.abhishekraj.popularmovies;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    String[] movieId, movieTitle, movieOverview, movieReleaseDate, moviePosterPath, movieVoteAverage;
    MovieAdapter mMovieAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GridView listView = (GridView) findViewById(R.id.gridview_movies);
        mMovieAdapter = new MovieAdapter(getBaseContext(),
                R.layout.activity_main,
                R.id.imageView,
                new ArrayList<String>());
        listView.setAdapter(mMovieAdapter);
        setContentView(R.layout.activity_main);

                 }
        class FetchMovieTask extends AsyncTask<Void, Void, List<String>> {
            private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

            @Override
            protected List<String> doInBackground(Void... params) {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                // Will contain the raw JSON response as a string.
                String movieJsonStr = null;

                try {
                    URL url = new URL("http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=a8f233f49f4b2563f9f16d293d705547");

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {

                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.
                        return null;
                    }
                    movieJsonStr = buffer.toString();

                } catch (IOException e) {

                    Log.e(LOG_TAG, "Error ", e);
                    return null;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e(LOG_TAG, "Error closing stream", e);
                        }
                    }
                }
                try {
                    return getMovieDataFromJson(movieJsonStr);
                } catch (JSONException j) {
                    Log.e(LOG_TAG, "JSON Error", j);
                }
                return null;
            }

            private List<String> getMovieDataFromJson(String forecastJsonStr)
                    throws JSONException {
                JSONObject movieJson = new JSONObject(forecastJsonStr);
                JSONArray movieArray = movieJson.getJSONArray("results");
                List<String> urls = new ArrayList<>();
                for (int i = 0; i < movieArray.length(); i++) {
                    JSONObject movie = movieArray.getJSONObject(i);
                    urls.add("http://image.tmdb.org/t/p/w185" + movie.getString("poster_path"));
                }
                return urls;
            }

            @Override
            protected void onPostExecute(List<String> strings) {

                mMovieAdapter.replace(strings);
            }
        }

        class MovieAdapter extends BaseAdapter {
            private final String LOG_TAG = MovieAdapter.class.getSimpleName();
            private final Context context;
            private final List<String> urls = new ArrayList<String>();

            public MovieAdapter(Context context, int activity_main, int imageView, ArrayList<String> strings) {
                this.context = context;
                Collections.addAll(urls, moviePosterPath);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = new ImageView(context);
                }
                ImageView imageView = (ImageView) convertView;


                String url = getItem(position);

                Log.e(LOG_TAG, " URL " + url);

                Picasso.with(context).load(url).into(imageView);

                return convertView;
            }

            @Override
            public int getCount() {
                return urls.size();
            }

            @Override
            public String getItem(int position) {
                return urls.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            public void replace(List<String> urls) {
                this.urls.clear();
                this.urls.addAll(urls);
                notifyDataSetChanged();
            }
        }

}

