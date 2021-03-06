package arnold.cja.cah;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import java.util.ArrayList;

/**
 * This activity shows the current black card, the Card Czar,
 * and a list of all other Players.  The user can tap a player name
 * to begin selecting the white cards to complete the combo for that player (SelectWhiteActivity)
 */
public class SelectNextPlayerActivity extends ListActivity {

    private static final String TAG = "SelectNxtPlayerActivity";

    private ArrayAdapter<Player> mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "SelectNextPlayer::onCreate");

        if (!Util.constructGameManagerIfNecessary(this)) {
            return;
        }

        GameManager gm = LaunchActivity.gm;

        // submit random white cards for the ai players:
        for (Player p : gm.getPlayers()) {
            if (p.isAI()) {
                if (!p.hasPickedWhite()) {
                    LaunchActivity.gm.setActivePlayer(p);
                    p.satisfyComboAI();
                }
            }

            if (p.isCardCzar()) {
                p.setHasPickedWhite(true);
            }
        }

        this.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        LaunchActivity.gm.updateLeaders();

        ArrayList<Player> players = new ArrayList<Player>(gm.getPlayers());
        players.remove(LaunchActivity.gm.getCardCzar());

        mAdapter = new PlayerSelectArrayAdapter(this, players);
        setListAdapter(mAdapter);
        setContentView(R.layout.select_next_player);
        mAdapter.notifyDataSetChanged();

        updateComboText();
        updateCardCzarText();

        Util.assertGameState(this, "SelectNextPlayerActivity::onCreate");
    }

    void updateComboText() {
        TextView t = (TextView) findViewById(R.id.whiteforblackpreview);

        Card c = LaunchActivity.gm.getDeck().getCurrentBlack();
        Combo tmp = new Combo(c, null);
        Log.i(TAG, "Setting up combo in updateComboText (selectplayer) for black card " + c.getText());

        t.setText(tmp.getStyledStatement(), BufferType.SPANNABLE);
    }

    void updateCardCzarText() {
        TextView t = (TextView) findViewById(R.id.cardczar);
        Player cz = LaunchActivity.gm.getCardCzar();
        if (cz.isLeader()) {
            Spannable s = Util.getInlineImageText(this, "CardCzar: " + cz.getName() + " :*");
            t.setText(s, BufferType.SPANNABLE);
        } else {
            t.setText("CardCzar: " + cz.getName());
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Player player = (Player) getListAdapter().getItem(position);

        if (player == LaunchActivity.gm.getCardCzar()) {
            Util.toast(this, "Card Czar does not need to select white cards");
        } else if (player.hasPickedWhite()) {
            Util.toast(this, player.getName() + " has already selected");
        } else {
            Intent intent = new Intent(this, SelectWhiteActivity.class);
            LaunchActivity.gm.setActivePlayer(player);
            Util.startActivityForResult(this, intent, 1);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 0) {
            // they pressed back button.  We need to stay in this activity
            return;
        }

        mAdapter.notifyDataSetChanged();

        if (LaunchActivity.gm.allPlayersSubmitted()) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "SelectNextPlayer::onStart");
        // The activity is about to become visible.
    }

    @Override
    protected void onResume() {
        super.onResume();
        Util.assertGameState(this, "SelectNextPlayerActivity::onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "SelectNextPlayerActivity::onPause");
        Util.saveStateIfLeavingApp(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "SelectNextPlayer::onStop");
        // The activity is no longer visible (it is now "stopped")
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "SelectNextPlayer::onDestroy");
        // The activity is about to be destroyed.
    }

    @Override
    public void onBackPressed() {
        LaunchActivity.gm.setLeavingActivity();
        super.onBackPressed();
    }
}
