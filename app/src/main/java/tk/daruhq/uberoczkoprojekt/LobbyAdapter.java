package tk.daruhq.uberoczkoprojekt;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Patryk on 2015-10-27.
 */
public class LobbyAdapter extends BaseAdapter implements OnClickListener {

    private Context context;
    private List<LobbyViewModel> listLobby;

    public LobbyAdapter(Context context, List<LobbyViewModel> listLobby) {
        this.context = context;
        this.listLobby = listLobby;
    }

    @Override
    public int getCount() {
        return listLobby.size();
    }

    @Override
    public Object getItem(int position) {
        return listLobby.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LobbyViewModel entry = listLobby.get(position);
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.lobby_row, null);
        }

        TextView tvNameOfLobby = (TextView) convertView.findViewById(R.id.nameOfLobby);
        tvNameOfLobby.setText(entry.getName());

        TextView tvNameOfOwner = (TextView) convertView.findViewById(R.id.lobbyOwner);
        tvNameOfOwner.setText(entry.getOwnerNickname());

        TextView tvPlayers = (TextView) convertView.findViewById(R.id.memberCount);
        tvPlayers.setText(String.format("%s/%s", entry.getMembersCount(), entry.getMaxMembersCount()));

        Button btnJoin = (Button) convertView.findViewById(R.id.joinLobby);
        btnJoin.setFocusableInTouchMode(false);
        btnJoin.setFocusable(false);
        btnJoin.setTag(entry);
        btnJoin.setOnClickListener(this);

        return convertView;
    }

    @Override
    public void onClick(View v) {
        LobbyViewModel entry = (LobbyViewModel) v.getTag();
        ((LobbyActivity) context).joinToLobby(entry.getName());
    }
}
