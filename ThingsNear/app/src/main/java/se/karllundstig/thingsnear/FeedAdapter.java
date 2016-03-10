package se.karllundstig.thingsnear;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {
    private ArrayList<Post> data;
    private HashMap<String, Bitmap> images;
    private Context context;
    private boolean haveLocation;
    private Location location;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public ViewHolder(CardView cardView) {
            super(cardView);
            this.cardView = cardView;
        }
    }

    public FeedAdapter(ArrayList<Post> data, HashMap<String, Bitmap> images, Context context, Location location) {
        this.data = data;
        this.images = images;
        this.context = context;
        setLocation(location);
    }

    public void setLocation(Location location) {
        this.haveLocation = true;
        this.location = location;
    }

    public void setPosts(ArrayList<Post> posts) {
        this.data = posts;
    }
    public ArrayList<Post> getPosts() {
        return data;
    }

    public HashMap<String, Bitmap> getImages() {
        return images;
    }

    @Override
    public FeedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView v = (CardView)LayoutInflater.from(parent.getContext()).inflate(R.layout.post, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Post post = data.get(position);
        ((TextView)holder.cardView.findViewById(R.id.content)).setText(post.content);

        long diff = Calendar.getInstance().getTime().getTime() - post.date.getTime() + NetQueue.getInstance(context).getServerDelta();
        String elapsed = Util.GetTimeLength(diff, context);

        String distance = "";
        if (haveLocation) {
            double dist = Util.CalculateDistance(location.getLatitude(), location.getLongitude(), post.latitude, post.longitude);
            distance = ", ";
            if (dist < 20) {
                distance += "right here";
            }
            else {
                if (dist >= 20 && dist < 100)
                    distance += (int)Math.round(dist / 10.0) * 10 + " m";
                else if (dist >= 100 && dist < 1000)
                    distance += (int)Math.round(dist / 100.0) * 100 + " m";
                else
                    distance += (int)Math.round(dist / 1000.0) + " km";
                distance += " from here";
            }
        }

        //har posten en bild, ladda ner den och visa
        if (!post.image.equals("")) {
            ImageView imageView = (ImageView) holder.cardView.findViewById(R.id.image);
            if (images.containsKey(post.image)) {
                //ladda bilden från cachen istället för en seg hämtning
                imageView.setImageBitmap(images.get(post.image));
            } else {
                //inte sparad, vi måste ladda ner den
                new DownloadImageTask(imageView).execute(post.image);
            }
        }

        ((TextView) holder.cardView.findViewById(R.id.title)).setText(post.creator + " wrote this " + elapsed + " ago" + distance);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;
        String id;
        public DownloadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            this.id = urls[0];
            String url = context.getString(R.string.server) + "/images/" + id;
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            images.put(id, result);
            imageView.setImageBitmap(result);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
