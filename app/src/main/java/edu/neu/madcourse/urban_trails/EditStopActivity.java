package edu.neu.madcourse.urban_trails;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import edu.neu.madcourse.urban_trails.models.Stop;

public class EditStopActivity extends AppCompatActivity {

    private Stop stop;
    private EditText stopNameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_stop);

        this.stop = (Stop) getIntent().getBundleExtra("bundle").getSerializable("stop");

        this.stopNameView = findViewById(R.id.stop_name);
        stopNameView.setText(stop.getTitle());
    }

    public void onClick(View view) {
        if (view.getId() == R.id.saveStopButton) {
            Intent intent = new Intent();
            Bundle b = new Bundle();
            this.stop.setTitle(stopNameView.getText().toString());
            b.putSerializable("stop", this.stop);
            intent.putExtra("bundle", b);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}