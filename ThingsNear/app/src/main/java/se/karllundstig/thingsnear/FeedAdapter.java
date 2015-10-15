package se.karllundstig.thingsnear;

import android.content.Context;
import android.location.Location;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {
    private ArrayList<Post> data;
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

    public FeedAdapter(ArrayList<Post> data, Context context, Location location) {
        this.data = data;
        this.context = context;
        setLocation(location);
    }

    public void setLocation(Location location) {
        this.haveLocation = true;
        this.location = location;
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

        long diff = Calendar.getInstance().getTime().getTime() - post.date.getTime();
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

        ((TextView) holder.cardView.findViewById(R.id.title)).setText(post.creator + " wrote this " + elapsed + " ago" + distance);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
