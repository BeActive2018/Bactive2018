package org.swanseacharm.bactive.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.swanseacharm.bactive.R;
import org.swanseacharm.bactive.databinding.ActivityHistoryBinding;

public class History extends AppCompatActivity {

    ActivityHistoryBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
    }
}
