package com.nam.vngnearbyplaces;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nam on 28/12/2015.
 */
public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> {

    private static List<HashMap<String, String>> listPlaces;
    private static List<String> listDistance;
    private static String currentPlaceType;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView placeAvatar;
        public TextView placeName;
        public TextView placeType;
        public TextView placeDistance;

        public ViewHolder(View itemView) {
            super(itemView);

            placeAvatar = (ImageView) itemView.findViewById(R.id.placeAvatar);
            placeName = (TextView) itemView.findViewById(R.id.placeName);
            placeType = (TextView) itemView.findViewById(R.id.placeType);
            placeDistance = (TextView) itemView.findViewById(R.id.placeDistance);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (!MainActivity.isOnline)
                return;

            int position = getLayoutPosition();
            HashMap<String, String> place = listPlaces.get(position);
            Intent intent = new Intent(view.getContext(), PlaceDetailsActivity.class);
            String reference = place.get("reference");
            intent.putExtra("reference", reference);

            // Starting the Place Details Activity
            view.getContext().startActivity(intent);
        }
    }

    public PlaceAdapter(List<HashMap<String, String>> places, List<String> distances, String type) {
        this.listPlaces = places;
        this.listDistance = distances;
        this.currentPlaceType = type;
    }

    public PlaceAdapter() {
        super();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View placeDetailsView = inflater.inflate(R.layout.place_row, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(placeDetailsView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HashMap<String, String> place = listPlaces.get(position);
        String distance = listDistance.get(position);

//        String lat = place.get("lat");
//        String lng = place.get("lng");

        ImageView ivAvatar = holder.placeAvatar;
        TextView tvName = holder.placeName;
        TextView tvType = holder.placeType;
        TextView tvDistance = holder.placeDistance;

        String placeName = place.get("place_name");
        placeName = "Place's name: " + placeName;
        tvName.setText(placeName);
        String placeType = "Type: " + currentPlaceType;
        tvType.setText(placeType);
        distance = "Distance from here: " + distance;
        tvDistance.setText(distance);

        DownloadImageTask downloadImageTask = new DownloadImageTask(ivAvatar);
        downloadImageTask.execute(place.get("icon"));
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    @Override
    public int getItemCount() {
        return listPlaces.size();
    }
}