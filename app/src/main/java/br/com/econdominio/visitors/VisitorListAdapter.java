package br.com.econdominio.visitors;


import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.util.List;

import br.com.econdominio.R;
import br.com.econdominio.utils.RoundRectTransformation;

public class VisitorListAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private final List<ParseObject> items;
    private final VisitorsActivity context;

    public VisitorListAdapter(VisitorsActivity context, List<ParseObject> items) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.items = items;
        this.context = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.visitor_list_item, null);
        }
        ParseObject item = (ParseObject) this.getItem(i);

        ImageView photoView = (ImageView) view.findViewById(R.id.visitor_photo);
        TextView nameView = (TextView) view.findViewById(R.id.visitor_name);

        Uri photoUri = Uri.parse(item.getParseFile("photo").getUrl());
        Picasso.with(context)
                .load(photoUri.toString())
                .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                .resize(200, 200)
                .centerCrop()
                .transform(new RoundRectTransformation(15))
                .into(photoView);

        nameView.setText(item.get("name").toString());

        setUpPopupMenu(item, view);

        return view;
    }

    private void setUpPopupMenu(final ParseObject item, final View view) {
        ImageButton btn = (ImageButton) view.findViewById(R.id.visitor_actions);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(context, view);
                popup.setOnMenuItemClickListener(new VisitorMenuItemClickListener(item));
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_visitor_actions, popup.getMenu());
                popup.show();
            }
        });
    }

    private class VisitorMenuItemClickListener
            implements PopupMenu.OnMenuItemClickListener {

        private final ParseObject item;

        public VisitorMenuItemClickListener(ParseObject item) {
            this.item = item;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.action_edit) {
                context.editVisitor(item);
            }
            if (menuItem.getItemId() == R.id.action_delete) {
                context.deleteVisitor(item);
            }
            return true;
        }
    }
}
