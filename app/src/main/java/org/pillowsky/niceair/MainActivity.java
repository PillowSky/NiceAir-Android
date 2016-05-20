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

        SeekBar seekBarProgress, seekBarThickness;
        seekBarProgress = (SeekBar) findViewById(R.id.seekBar_progress);
        seekBarThickness = (SeekBar) findViewById(R.id.seekBar_thickness);
        final Button button = (Button) findViewById(R.id.button);
        final CircleProgressBar circleProgressBar = (CircleProgressBar) findViewById(R.id.custom_progressBar);
        //Using ColorPickerLibrary to pick color for our CustomProgressbar
        final ColorPickerDialog colorPickerDialog = new ColorPickerDialog();

        colorPickerDialog.initialize(R.string.select_color, new int[] {Color.CYAN, Color.DKGRAY, Color.BLACK, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.RED, Color.GRAY, Color.YELLOW}, Color.DKGRAY, 3, 2);
        colorPickerDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                assert circleProgressBar != null;
                circleProgressBar.setColor(color);
            }
        });

        assert button != null;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorPickerDialog.show(getSupportFragmentManager(), "color_picker");
            }
        });

        seekBarProgress.setProgress((int) circleProgressBar.getProgress());
        seekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    circleProgressBar.setProgressWithAnimation(i);
                }
                else {
                    circleProgressBar.setProgress(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarThickness.setProgress((int) circleProgressBar.getStrokeWidth());
        seekBarThickness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                circleProgressBar.setStrokeWidth(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
