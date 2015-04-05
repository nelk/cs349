
==============================
Quick Motion for Android
by Alex Klen
==============================

Overview
-------------------------------------------------------------------------------------------
This animation tool allows you to easily create complex animations with a few simple tools. Most tools have an 'animation' mode which significantly changes how the tool behaves. An explicit timeline lets you move forwards and backwards through time in your animation and lets you animate objects simultaneously.


Compilation/Running
-------------------------------------------------------------------------------------------
To compile and run the application, open the provided Eclipse project and run the application with an Android device connected. You can also just install the pre-build apk.

Android Version
------------------------------------------------------------------------------------------
The Android version used in this project was 4.2.2.
Development was carried out on a Nexus 7 tablet with a 1280x800 screen resolution.
The application does not perform well on an emulator - it runs best on a tablet device.

Enhancements
-------------------------------------------------------------------------------------------
- Using A3 code
- Ported A3 functionality to the Android, including superimposed translation, rotation, and scaling animations.

Saving/Loading
-----------------------------------------------------------------------------------------
Use the Action Overflow on the application's Title bar to access the Saving and Loading features. You can save and load files in xml format. Files are saved to sdcard_root/quickmotion, and can be moved there using a computer in order to import files created with the desktop version of QuickMotion.



Introduction and Definitions
-------------------------------------------------------------------------------------------

Tools:
- Marker - For adding new drawn objects to the scene.
- Lasso - For selecting existing objects and for translating objects.
- Eraser - For deleting objects from the screen.
- Rotate - For rotating objects.
- Scale - For scaling objects.

Modes:
- Idle - When you have a tool selected but are not clicking and dragging.
- Selection - When you are clicking and dragging with the Lasso tool
- Drawing - When you are clicking and dragging with the Marker tool
- Erasing - When you are clicking and dragging with the Eraser tool
- Translating - When you are clicking and dragging a selected figure with the Lasso tool
- Rotating - When you are clicking and dragging with the Rotate tool
- Scaling - When you are clicking and dragging with the Scale tool

Lines:
- Lines are created with the Marker and have a variable colour.
- Lines have three modes: normal, selected, and highlighted.
- Lines are selected with the lasso tool's selection mode, and selected lines are highlighted when the mouse is hovered over them.

Timeline:
- Play - Press this to make the animation play back from the current position.
- Pause - Press this (only appears while playing) to pause the playing animation at it's current position.

Animation:
- Many tools have an animated sub-mode that can be activated by pressing the 'Animate' toggle button. This will cause the drawing or transformation to not be applied immediately on the object at a fixed point in time but to be recorded as part of the animation. The timeline will advance, and playing it back will make the transformation happen at the same speed as you performed it.


Modes
-----------------------------------------------------------------------------------------
Marker:
- Click and drag while using the marker tool to draw a smooth continuous line.
- You can choose the colour of the line by selecting it from the colour chooser in the Settings page.
- Animated Draw: With the 'Animate' button depressed, drawing will cause the drawing to be animated. The timeline will advance (in real time), and the animation will look like the line is being drawn.

Eraser:
- Click and drag while using the eraser to delete lines.

Lasso, clicking and dragging NOT on selected lines:
- Click and drag while using the lasso to create a path of "walking ants" that shows your selection area.
- When you let go, all of the lines in the lasso will become selected.

Lasso, clicking and dragging a selected (and highlighted) line:
- After some lines are selected, hovering the lasso over a selected line will cause the selected lines to be highlighted (they will look thicker).
- While hovering over a selected line, click and drag the mouse to enter translation mode.
- Translation mode lets you move all of the selected lines.
- Animated Translate: If the 'Animate' button is active, translating will cause the translation to be animated.

Rotate:
- First use the lasso tool to select some number of lines or figures.
- Now select the rotate tool.
- Click and drag to rotate all of the selected lines.
- The lines will rotate around the center of the bounding box around the selection (it is shown as a red dot while rotating).
- Animated Rotate: If the 'Animate' button is active, rotating will cause the rotation to be animated.

Scale:
- First use the lasso tool to select some number of lines or figures.
- Now select the scale tool.
- Click and drag to scale all of the selected lines.
- The lines will scale based off of mouse drags relative to the center of the bounding box around the selection (it is shown as a red dot while rotating).
- Animated Scale: If the 'Animate' button is active, scaling will cause the scale to be animated.


Settings
---------------------------------------------------------------------------------------------
Press the 'Settings' button to view the settings page, which is in a different Activity.
Here you can set the marker colour, the background colour, and the FPS rate of playback (and recording).


Advanced Animation
-------------------------------------------------------------------------------------------
Superposition:
- There are three types of animations: translation, rotation, and scale.
- The three types of animations can be superimposed!
- After applying one animated transformation to an object, you can move the timeline backwards, select the same object, and apply a different type of animated transformation. This will cause both types of animations to be applied at the same time.
- Note that if you translate an object, then scale or rotate it, the object will move away, but for simplicity the reference point (red dot) will remain in the same place, so you have more control over the next animated transformation while a previous one is playing back.
- You cannot superimpose two of the same types of transformations. If you apply another animated transformation at a point in time where another transformation of the same type was animated, the new one will overwrite the old one, starting at the start time of the new one (it should behaves intuitively).

Removing animation:
- To remove an animation from an object, simply use the property that you cannot superimpose two of the same types of animations. For example, if you wanted to remove an animated translation, with the lasso tool select the object and then hold CTRL and click and release on the selected object. This will overwrite the existing animation with an empty translation animation.


Sharing/Getting Content
----------------------------------------------------------------------------------------
You can use the 'Share' feature to send the xml file to another application, such as a file manager or Dropbox. You can also use the 'Get Content' feature to use another application to provide an xml file for opening, such as a file manager.

