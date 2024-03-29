package dk.anderswind.notebook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    private static final String PACKAGE = "dk.anderswind.notebook.";
    public static final String NOTE_ID_EXTRA = PACKAGE + "Identifier";
    public static final String NOTE_TITLE_EXTRA = PACKAGE + "Title";
    public static final String NOTE_MESSAGE_EXTRA = PACKAGE + "Message";
    public static final String NOTE_CATEGORY_EXTRA = PACKAGE + "Category";
    public static final String NOTE_VIEW_FRAGMENT_LAUNCH = PACKAGE + "FragmentToLaunch";
    public enum FragmentToLaunch{ VIEW, EDIT, CREATE }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        loadPreferences();

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, AppPreferences.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_add_note)
        {
            Intent intent = new Intent(this, NoteDetailActivity.class);
            intent.putExtra(MainActivity.NOTE_VIEW_FRAGMENT_LAUNCH, FragmentToLaunch.CREATE);

            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadPreferences()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isBackgroundDark = sharedPreferences.getBoolean("background_color", false);
        if (isBackgroundDark)
        {
            LinearLayout mainLayout = (LinearLayout) findViewById(R.id.mainActivityLayout);
            mainLayout.setBackgroundColor(Color.parseColor("#3c3f41"));
        }
        String notebookTitle = sharedPreferences.getString("title", "Notebook");
        setTitle(notebookTitle);
    }
}
