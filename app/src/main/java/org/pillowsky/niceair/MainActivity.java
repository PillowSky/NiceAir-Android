package org.pillowsky.niceair;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.fourmob.colorpicker.ColorPickerDialog;
import com.fourmob.colorpicker.ColorPickerSwatch;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button button = (Button) findViewById(R.id.button);
        final RotationRing ring = (RotationRing) findViewById(R.id.rotation_ring);
        assert button != null;
        assert ring != null;

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ring.startRotation();
            }
        });
    }
}
