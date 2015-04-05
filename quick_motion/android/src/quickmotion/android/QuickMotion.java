package quickmotion.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.view.*;
import android.widget.*;
import quickmotion.shared.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

public class QuickMotion extends Activity implements AbstractView {
    private static final int TIMELINE_SLIDER_TICKS = 300;
    public static final int DESIRED_FPS = 40;   // Target number of updates per second

    private boolean preventSliderChange = false;
    final private List<DrawnLine> lines;
    final private List<Animation> animations;
    private Tool tool;
    private DocumentManager documentManager;
    private QuickMotionView mainView;
    private SurfaceHolder surfaceHolder;
    private TimeLine timeline;
    private boolean exitTime = false;
    private SeekBar timelineSlider;
    private TouchController touchController;
    private long lastTime;
    private ToggleButton animationButton;
    private int backgroundColour;

    public QuickMotion() {
        MatMatrix.injectFactory();

        documentManager = new DocumentManager();
        lines = new ArrayList<DrawnLine>();
        animations = new ArrayList<Animation>();
        Tool.setView(this);
        Tool.setSelectDistance(20f);
        tool = Tool.MARKER;
        timeline = new TimeLine();
        touchController = new TouchController(this);
        backgroundColour = Color.WHITE;
    }


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        mainView = (QuickMotionView)findViewById(R.id.mainView);
        surfaceHolder = mainView.getHolder();


        mainView.setView(this);


        mainView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return touchController.onTouchEvent(event);
            }
        });

        lastTime = System.currentTimeMillis();

        final ImageButton markerButton = (ImageButton) findViewById(R.id.marker_button);
        markerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tool = Tool.MARKER;
                Tool.notifyToolChanged();
                timeline.setPlaying(false);
            }
        });

        final ImageButton eraserButton = (ImageButton) findViewById(R.id.eraser_button);
        eraserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tool = Tool.ERASER;
                Tool.notifyToolChanged();
                timeline.setPlaying(false);
            }
        });

        final ImageButton lassoButton = (ImageButton) findViewById(R.id.lasso_button);
        lassoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tool = Tool.LASSO;
                Tool.notifyToolChanged();
                timeline.setPlaying(false);
            }
        });

        final ImageButton rotateButton = (ImageButton) findViewById(R.id.rotate_button);
        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tool = Tool.ROTATE;
                Tool.notifyToolChanged();
                timeline.setPlaying(false);
            }
        });

        final ImageButton scaleButton = (ImageButton) findViewById(R.id.scale_button);
        scaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tool = Tool.SCALE;
                Tool.notifyToolChanged();
                timeline.setPlaying(false);
            }
        });

        final Button playButton = (Button) findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeline.setPlaying(!timeline.isPlaying());
                playButton.setText(timeline.isPlaying() ? "Pause" : "Play");
                Tool.resetLasso();
            }
        });

        animationButton = (ToggleButton) findViewById(R.id.animate_button);

        final Button settingsButton = (Button) findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QuickMotion.this, Settings.class);
                intent.putExtra(getString(R.string.k_colour), Tool.getColour());
                intent.putExtra(getString(R.string.k_backgroundColour), getBackgroundColour());
                intent.putExtra(getString(R.string.k_framerate), timeline.getStepFactor());
                startActivityForResult(intent, SETTINGS_CODE);
            }
        });

        timelineSlider = (SeekBar) findViewById(R.id.timeline_slider);
        timelineSlider.setMax(TIMELINE_SLIDER_TICKS);
        timelineSlider.setProgress(0);
        timelineSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (preventSliderChange) {
                    preventSliderChange = false;
                    return;
                }
                timeline.setPlaying(false);
                playButton.setText("Play");
                Tool.resetLasso();
                timeline.setTime(sliderToTimeline(progress)); // TODO - check synchronization?
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });




        Thread thread = new Thread() {
            @Override
            public void run() {
                while (!exitTime) {
                    long curTime;
                    Delta delta;
                    curTime = System.currentTimeMillis();
                    delta = new Delta(curTime - lastTime);

                    tick(delta); // Execute one game step

                    if (mainView.isSurfaceReady()) {
                        Canvas canvas = null;
                        try {
                            canvas = surfaceHolder.lockCanvas();
                            if (canvas != null) {
                                synchronized (surfaceHolder) {
                                    mainView.doDraw(canvas);
                                }
                            }
                        } finally {
                            if (canvas != null) {
                                surfaceHolder.unlockCanvasAndPost(canvas);
                            }
                        }
                    }

                    try {
                        long timeTaken = System.currentTimeMillis() - lastTime;
                        long delayTime = 1000 / DESIRED_FPS - timeTaken;

                        if (delayTime > 0) {
                            Thread.sleep(delayTime);
                        }
                    } catch (InterruptedException ignored) {
                    }

                    lastTime = curTime;
                }
            }
        };
        thread.start();
    }

    private void tick(Delta delta) { // Main Loop
        touchController.update(delta);
        timeline.update(delta);
        if (timeline.isPlaying()) {
            notifyTimelineUpdated();
        } else {
//            playPauseButton.setText("Play");
            tool.update(delta, timeline.getTime());
        }
    }

    private void quitProgram() {
        System.exit(0);
    }



    private float getTimelineScale() {
        return TIMELINE_SLIDER_TICKS/timeline.getMaxObservedTime();
    }

    private int timeLineToSlider(float time) {
        return Math.round(time * getTimelineScale());
    }

    private float sliderToTimeline(int time) {
        return time/getTimelineScale();
    }


    @Override
    public Tool getTool() {
        return tool;
    }

    @Override
    public TimeLine getTimeLine() {
        return timeline;
    }

    @Override
    public AbstractController getController() {
        return touchController;
    }

    @Override
    public void notifyTimelineUpdated() {
        preventSliderChange = true;
        timelineSlider.setProgress(timeLineToSlider(timeline.getTime()));
    }

    @Override
    public Vector2d getCanvasOffset() {
        return new Vector2d();
    }

    @Override
    public boolean addAnimation(Animation currentAnimation) {
        return animations.add(currentAnimation);
    }

    @Override
    public List<DrawnLine> getLines() {
        return (List<DrawnLine>) SyncTools.synchronizedCopy(lines);
    }

    @Override
    public boolean addLine(DrawnLine l) {
        synchronized (lines) {
            return lines.add(l);
        }
    }

    @Override
    public boolean shouldAnimate() {
        return animationButton != null && animationButton.isChecked();
    }

    @Override
    public int getBackgroundColour() {
        return backgroundColour;
    }

//    private static final int OPEN_FILE_CODE = 0;
//    private static final int SAVE_FILE_CODE = 1;
    private static final int SETTINGS_CODE = 2;
    private static final int SHARE_CODE = 3;
    private static final int GET_CONTENT_CODE = 4;

    private void showFileIntent(int code, Uri uri, String message) {
        Intent intent = new Intent(code == SHARE_CODE ? Intent.ACTION_SEND : Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        if (code == SHARE_CODE) {
            intent.putExtra(Intent.EXTRA_STREAM, uri);
        }

        try {
            startActivityForResult(Intent.createChooser(intent, message), code);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case GET_CONTENT_CODE:
                Uri uri = data.getData();
                if (uri == null) return;
                synchronized (lines) {
                    synchronized (animations) {
                        lines.clear();
                        animations.clear();
                        boolean successful = documentManager.load(new File(uri.getPath()), lines, animations);
                    }
                }
                // TODO - popup if not successful
                break;
//            case SAVE_FILE_CODE:
//                Toast.makeText(this, "File Saved Successfully", Toast.LENGTH_SHORT).show();
//                break;
            case SHARE_CODE:
                Toast.makeText(this, "File Shared Successfully", Toast.LENGTH_SHORT).show();
                break;
            case SETTINGS_CODE:
                String colourString = getString(R.string.k_colour);
                String backgroundColourString = getString(R.string.k_backgroundColour);
                String framerateString = getString(R.string.k_framerate);
                int colour = data.getIntExtra(colourString, Tool.getColour());
                Tool.setColour(colour);
                backgroundColour = data.getIntExtra(backgroundColourString, Color.WHITE);
                float step_size = Math.max(data.getFloatExtra(framerateString, timeline.getStepFactor()), 0.1f);
                timeline.setStepFactor(step_size);
                break;
        }
    }

    private void share() {
        try {
            File outputDir = getCacheDir(); // context being the Activity pointer
            File outputFile = File.createTempFile("tempsavedfile", ".xml", outputDir);
            outputFile.setReadable(true, false);
            documentManager.save(outputFile, SyncTools.synchronizedCopy(lines));
            showFileIntent(SHARE_CODE, Uri.fromFile(outputFile), "Save file.");
        } catch (IOException e) {
        }
    }

    private void getContentIntent() {
        showFileIntent(GET_CONTENT_CODE, null, "Open a file.");
    }

    static interface Callback<T> {
        public void call(T v);
    }

    private void promptForFilename(final Callback<String> callback) {
        final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
            .setTitle("Save")
            .setView(input)
            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Editable value = input.getText();
                    callback.call(value.toString());
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Do nothing.
                }
            }).show();
    }

    private void promptForFileFromList(final File[] files, final Callback<File> callback) {
        String[] fileNames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            fileNames[i] = files[i].getName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Open File")
                .setItems(fileNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.call(files[which]);
                    }
                }).show();
    }

    private void saveToFile() {
        promptForFilename(
            new Callback<String>() {
                @Override
                public void call(String s) {
                    File root = Environment.getExternalStorageDirectory();
                    File outputDir = new File(root.getPath() + "/quickmotion");
                    outputDir.mkdir();
                    File outputFile = new File(outputDir.getPath(), s + ".xml");
                    outputFile.setReadable(true, false);
                    documentManager.save(outputFile, SyncTools.synchronizedCopy(lines));
                }
        });
    }

    private void loadFromFile() {
        File root = Environment.getExternalStorageDirectory();
        File outputDir = new File(root.getPath() + "/quickmotion");
        outputDir.mkdir();
        File[] files = outputDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".xml");
            }
        });
        promptForFileFromList(files, new Callback<File>() {
            @Override
            public void call(File f) {
                synchronized (lines) {
                    synchronized (animations) {
                        lines.clear();
                        animations.clear();
                        boolean successful = documentManager.load(f, lines, animations);
                        if (!successful) {
                            Toast.makeText(QuickMotion.this, "File Failed To Load", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.open:
                loadFromFile();
                return true;
            case R.id.save:
                saveToFile();
                return true;
            case R.id.share:
                share();
                return true;
            case R.id.getContent:
                getContentIntent();
                return true;
            case R.id.newAnim:
                synchronized (lines) {
                    lines.clear();
                }
                synchronized (animations) {
                    animations.clear();
                }
                Tool.resetLasso();
                timeline.reset();
                notifyTimelineUpdated();
                return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Tool.resetLasso();
    }
}
