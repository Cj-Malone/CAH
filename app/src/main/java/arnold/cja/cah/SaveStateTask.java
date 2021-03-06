package arnold.cja.cah;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * A background thread to serialize and save the game state to file
 */
public class SaveStateTask extends AsyncTask<GameManagerAndContext, Void, Long> {

    private static final String TAG = "SaveStateTask";

    protected Long doInBackground(GameManagerAndContext... input) {
        return Util.saveState(input[0].mGm, input[0].mContext);
    }

    protected void onPostExecute(Long duration) {
        Log.i(TAG, "Saved CAH data (" + duration + " ms)");
    }
}
