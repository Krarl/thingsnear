package se.karllundstig.thingsnear;

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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public ViewHolder(CardView cardView) {
            super(cardView);
            this.cardView = cardView;
        }
    }

    public FeedAdapter(ArrayList<Post> data) {
        this.data = data;
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
        int seconds = (int)TimeUnit.SECONDS.convert(diff, TimeUnit.MILLISECONDS);
        int minutes = (int)TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS);
        int hours = (int)TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS);
        int days = (int)TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        int months = days / 30;
        int years = months / 12;

        String elapsed;
        if (years > 0)
            elapsed = years + " years";
        else if (months > 0)
            elapsed = months + " months";
        else if (days > 0)
            elapsed = days + " days";
        else if (hours > 0)
            elapsed = hours + " hours";
        else if (minutes > 0)
            elapsed = minutes + " minutes";
        else
            elapsed = seconds + " seconds";

        ((TextView) holder.cardView.findViewById(R.id.title)).setText(post.creator + " wrote this " + elapsed + " ago");
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
