package quickmotion.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 *
 */
public class Settings extends Activity {
//    private SeekBar colourBar;
//    private SeekBar backgroundColourBar;
    private SeekBar framerateBar;
    private int colour;
    private int backgroundColour;
    private int framerate;
    private Intent intent;
    private TextView colourText;
    private TextView backgroundText;
    private TextView framerateText;

    private ColorPickerView colourPicker;
    private ColorPickerView backgroundColourPicker;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings);

        intent = getIntent();
        colourText = (TextView) findViewById(R.id.colourText);
        backgroundText = (TextView) findViewById(R.id.backgroundText);
        framerateText = (TextView) findViewById(R.id.framerateText);

        /*
        colourBar = (SeekBar) findViewById(R.id.markerColourBar);
        colourBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                colour = colourBar.getProgress() | 0xFF000000;
                colourText.setBackgroundColor(colour);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        backgroundColourBar = (SeekBar) findViewById(R.id.backgroundColourBar);
        backgroundColourBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                backgroundColour = backgroundColourBar.getProgress() | 0xFF000000;
                backgroundText.setBackgroundColor(backgroundColour);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        */

        framerateBar = (SeekBar) findViewById(R.id.framerateBar);
        framerateBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                framerate = progress;
                framerateText.setText(String.valueOf(Math.round(sliderToRateFactor(progress)*40)) + " FPS");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        colour = intent.getIntExtra(getString(R.string.k_colour), 0);
        backgroundColour = intent.getIntExtra(getString(R.string.k_backgroundColour), 0);
        float step_size = intent.getFloatExtra(getString(R.string.k_framerate), 1f);
        framerate = rateFactorToSlider(step_size);

//        colourText.setBackgroundColor(colour);
//        backgroundText.setBackgroundColor(backgroundColour);

//        colourBar.setProgress(colour & 0xFFFFFF);
//        backgroundColourBar.setProgress(backgroundColour & 0xFFFFFF);
        framerateBar.setProgress(rateFactorToSlider(step_size));
        framerateText.setText(String.valueOf(Math.round(step_size*40)) + " FPS");

        colourPicker = new ColorPickerView(getApplicationContext(), new ColorPickerView.OnColorChangedListener() {
            @Override
            public void colorChanged(int color) {
                colour = color | 0xFF000000;
//                colourText.setBackgroundColor(colour);
            }
        }, colour);

        backgroundColourPicker = new ColorPickerView(getApplicationContext(), new ColorPickerView.OnColorChangedListener() {
            @Override
            public void colorChanged(int color) {
                backgroundColour = color | 0xFF000000;
//                backgroundText.setBackgroundColor(colour);
            }
        }, backgroundColour);

        LinearLayout ll = (LinearLayout)findViewById(R.id.colourPicker);
        ll.addView(colourPicker);
        ll = (LinearLayout)findViewById(R.id.backgroundColourPicker);
        ll.addView(backgroundColourPicker);
    }

    private static float sliderToRateFactor(int t) {
        return (float)Math.pow(10, t/50f - 1f);
//        return t*(10f-0.1f)/100f + 0.1f;
    }

    private static int rateFactorToSlider(float t) {
        return (int)Math.round((Math.log10(t) + 1f) * 50f);
//        return Math.round((t - 0.1f)*100f/(10f-0.1f));
    }

    @Override
    public void onBackPressed() {
        intent.putExtra(getString(R.string.k_colour), colour);
        intent.putExtra(getString(R.string.k_backgroundColour), backgroundColour);
        float f = sliderToRateFactor(framerate);
        intent.putExtra(getString(R.string.k_framerate), f);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
