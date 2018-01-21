package com.recyclegrid.adapters;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.recyclegrid.app.R;
import com.recyclegrid.app.Toast;
import com.recyclegrid.core.City;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class CitiesListAdapter extends BaseAdapter implements Filterable {
    private Context _context;
    private LayoutInflater _inflater;
    private Toast _toast;

    private List<City> _cities;

    public CitiesListAdapter(Context context) {
        _context = context;
        _inflater = LayoutInflater.from(context);
        _toast = new Toast((AppCompatActivity)context);
    }

    @Override
    public int getCount() {
        return _cities.size();
    }

    @Override
    public Object getItem(int position) {
        return _cities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return _cities.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = _inflater.inflate(R.layout.list_item_cities, parent, false);
        }

        City city = (City)getItem(position);

        TextView cityName = convertView.findViewById(R.id.text_city_name);

        cityName.setText(city.getName());

        return convertView;
    }

    @Override
    public Filter getFilter(){
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                if (constraint != null) {
                    List<City> searchResult = searchCities(constraint);

                    results.values = searchResult;
                    results.count = searchResult.size();
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    _cities = (List<City>) results.values;
                }
            }
        };

        return  filter;
    }

    private List<City> searchCities(CharSequence searchTerm) {
        _cities = new ArrayList<>();

        _cities.add(new City(0, "Loading..."));

        RequestQueue requests = Volley.newRequestQueue(_context);

        String url = _context.getString(R.string.base_api_url) + "/geolocation/getcities?term=" + searchTerm;

        JsonArrayRequest getCitiesRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                _cities = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        _cities.add(new City(response.getJSONObject(i).getLong("Id"), response.getJSONObject(i).getString("Name")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                _toast.showError(R.string.error_response_default);
            }
        });

        requests.add(getCitiesRequest);

        return _cities;
    }
}
