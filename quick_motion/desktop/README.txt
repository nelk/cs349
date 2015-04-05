
==============================
Quick Motion - animation tool
by Alex Klen
==============================

Overview
-------------------------------------------------------------------------------------------
This animation tool allows you to easily create complex animations with a few simple tools. Most tools have a modifier which significantly changes how the tool behaves. An explicit timeline lets you move forwards and backwards through time in your animation and lets you animate objects simultaneously.


Compilation/Running
-------------------------------------------------------------------------------------------
To compile and run the application, simply call "make" or "make run" in the project directory.
You can also call "make compile" to only compile the code but not run it.


Enhancements
-------------------------------------------------------------------------------------------
- Rotation and Scaling tools - complete with animation and the ability to superimpose all three types of animated transformations
- Exporting as animated gif
- Animated drawing and partial selection of figures


Introduction and Definitions
-------------------------------------------------------------------------------------------

Tools:
- Marker - For adding new drawn objects to the scene.
- Lasso - For selecting existing objects and for translating objects.
- Eraser - For deleting objects from the screen.
- Add Time - For inserting time in the animation.
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

Figures:
- A figure is an series of connected lines that were drawn with one continuous click and drag of the mouse.
- A figure splits into more figures if parts of a figure are selected or animated seperately.

Lines:
- Lines are created with the Marker and have a fixed colour and thickness.
- Lines have three modes: normal, selected, and highlighted.
- Lines are selected with the lasso tool's selection mode, and selected lines are highlighted when the mouse is hovered over them.

Timeline:
- Play - Press this to make the animation play back from the current position.
- Pause - Press this (only appears while playing) to pause the playing animation at it's current position.
- Stop - Press this to stop a playing animation and to move to the very beginning of the animation. You can also press this while paused to just move to the beginning.
- Step left/right - Move by a small increment along the timeline.
- Note: The timeline's total span of time will increase as you animate. The scale of the timeline will change, but step left/right will still move the same amount.

Animation:
- Many tools have an animated sub-mode triggered by holding down CTRL. This will cause the drawing or transformation to not be applied immediately on the object at a fixed point in time but to be recorded as part of the animation. The timeline will advance, and playing it back will make the transformation happen at the same speed as you performed it.


Modes
-----------------------------------------------------------------------------------------
Marker:
- Click and drag while using the marker tool to draw a smooth continuous line.
- You can choose the colour of the line by selecting it from the colour chooser.
- Animated Draw: Hold down CTRL while drawing to cause the drawing to be animated. The timeline will advance (in real time), and the animation will look like the line is being drawn.

Eraser:
- Click and drag while using the eraser to delete lines.
- Used regularly, the eraser will erase single lines at a time.
- Hold CTRL to erase entire figures.

Lasso, clicking and dragging NOT on selected lines:
- Click and drag while using the lasso to create a path of "walking ants" that shows your selection area.
- When you let go, all of the lines in the lasso will become selected.
- Hold CTRL to only select entire figures - if any of the lines of a figure are outside of the lasso area when you hold CTRL, none of the lines will be selected.
- Note: Selecting only parts of a figure will cause the figure to split into two figures, one being the newly selected portion, and the other being the rest of the original figure.

Lasso, clicking and dragging a selected (and highlighted) line:
- After some lines are selected, hovering the lasso over a selected line will cause the selected lines to be highlighted (they will look thicker).
- While hovering over a selected line, click and drag the mouse to enter translation mode.
- Translation mode lets you move all of the selected lines.
- Animated Translate: Hold down CTRL while translating to cause the translation to be animated.

Add Time:
- Click on the Add Time tool to bring up a popup with an input box.
- Enter the amount of time (in seconds) you wish to insert.
- This amount of time will be inserted into the timeline at the current position.
- The timeline will remain at the same position.
- Note: The time inserted will look the same as the scene at the time you inserted it. This means if you were smoothly animated a figure right, if you insert 1 second of time in the middle of the animation, the figure will move right halfway, pause for 1 second, and then continue.

Rotate:
- First use the lasso tool to select some number of lines or figures.
- Now select the rotate tool.
- Click and drag to rotate all of the selected lines.
- The lines will rotate around the center of the bounding box around the selection (it is shown as a red dot while rotating).
- Animated Rotate: Hold down CTRL while rotating to cause the rotation to be animated.

Scale:
- First use the lasso tool to select some number of lines or figures.
- Now select the scale tool.
- Click and drag to scale all of the selected lines.
- The lines will scale based off of mouse drags relative to the center of the bounding box around the selection (it is shown as a red dot while rotating).
- Animated Scale: Hold down CTRL while scaling to cause the scale to be animated.



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

Using off-screen:
- If you resize the window, you'll notice that there is an off-screen area on the right. You can draw/move objects in and out of this region to keep track of them but move them off screen in your animation.


Keyboard Shortcuts
-------------------------------------------------------------------------------------------
Keyboard shortcuts can be checked by looking at the menu. A list is included here.

Marker (M)
Lasso (L)
Eraser (E)
Rotate (R)
Scale (S)
Add Time (T)
Select All (CTRL+A)
Quit (Q)


Exporting to Animated Gif
------------------------------------------------------------------------------------------
Click "File->Export as animated gif..." to save the animation as an animated .gif file. After choosing the option, specify a file name/location for your gif file. Generating the animated gif takes some time, and a modal dialog will appear while it's exporting. You won't be able to use the program while exporting an animated gif. It should take less than a minute for a gif of reasonable length of time.

Saving/Loading
-----------------------------------------------------------------------------------------
You can save and load files in xml format. These files can be copied to an Android device and used with the QuickMotion Android application. Simply copy them to the sd card in a folder called 'quickmotion' in the root directory.




