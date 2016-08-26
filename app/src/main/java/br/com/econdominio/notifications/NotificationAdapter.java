package br.com.econdominio.notifications;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import br.com.econdominio.R;

public class NotificationAdapter extends BaseAdapter {

    private static final String DATE_FORMAT = "dd/MM/yy HH:mm";
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(
            DATE_FORMAT, Locale.getDefault());
    private final LayoutInflater inflater;
    private final List<ParseObject> notifications;
    private final Context context;

    public NotificationAdapter(Context context, List<ParseObject> notifications) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.notifications = notifications;
        this.context = context;
    }

    @Override
    public int getCount() {
        return notifications.size();
    }

    @Override
    public Object getItem(int i) {
        return notifications.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.notification_list_item, null);
        }
        ParseObject notification = (ParseObject) this.getItem(i);

        ImageView imageView = (ImageView) view.findViewById(R.id.notification_icon);
        TextView textView = (TextView) view.findViewById(R.id.notification_text);
        TextView dateView = (TextView) view.findViewById(R.id.notification_date);
        TextView commentView = (TextView) view.findViewById(R.id.notification_comment);

        setupNotificationIcon(notification, imageView);
        textView.setText(getNotificationTitle(notification));
        dateView.setText(DATE_FORMATTER.format(notification.getCreatedAt()));
        commentView.setText((String) notification.get("comment"));
        return view;
    }

    private void setupNotificationIcon(ParseObject notification, ImageView imageView) {
        if (notification.get("type").equals("mail")) {
            imageView.setImageResource(R.drawable.ic_mail_outline);
            imageView.setColorFilter(ContextCompat.getColor(context, R.color.yellow));
        }
        if (notification.get("type").equals("visitor_arrived")) {
            imageView.setImageResource(R.drawable.ic_person_black_24dp);
            imageView.setColorFilter(ContextCompat.getColor(context, R.color.green));
        }
        if (notification.get("type").equals("visitor_left")) {
            imageView.setImageResource(R.drawable.ic_person_black_24dp);
            imageView.setColorFilter(ContextCompat.getColor(context, R.color.red));
        }
        if (notification.get("type").equals("condo_notice")) {
            imageView.setImageResource(R.drawable.ic_location_city);
            imageView.setColorFilter(ContextCompat.getColor(context, R.color.blue));
        }
    }

    private String getNotificationTitle(ParseObject notification) {
        if (notification.get("type").equals("mail")) {
            return "Nova correspondência";
        }
        if (notification.get("type").equals("visitor_arrived")) {
            return "Chegada de visitante";
        }
        if (notification.get("type").equals("visitor_left")) {
            return "Saída de visitante";
        }
        if (notification.get("type").equals("condo_notice")) {
            return "Aviso do condomínio";
        }
        return "NOTIFICATION";
    }
}
