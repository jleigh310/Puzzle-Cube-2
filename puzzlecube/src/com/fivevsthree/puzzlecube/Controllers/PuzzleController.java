package com.fivevsthree.puzzlecube.Controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.fivevsthree.puzzlecube.PuzzleCube;
import com.fivevsthree.puzzlecube.Callbacks.PuzzleControllerCallback;
import com.fivevsthree.puzzlecube.Models.Puzzle;

/**
 * Process user input for puzzle and camera
 * 
 * @author splude@fivevsthree.com
 * 
 */
public class PuzzleController implements GestureListener {

	private Puzzle puzzle;
	private Vector3 fromLocation, fromAxis, toLocation, toAxis, rotationAxis;
	private boolean rotateCamera, checkingCubeRotation, hitCheckPaused,
			verticalRotationPaused, horizontalRotationPaused;
	private PuzzleControllerCallback callback;
	private Vector2 previousPointer1, previousPointer2;

	private Preferences preferences;
	private float touchSensitivity;

	public void setCallback(PuzzleControllerCallback callback) {
		this.callback = callback;
	}

	public void pauseHitCheck() {
		hitCheckPaused = true;
	}

	public void resumeHitCheck() {
		hitCheckPaused = false;
	}

	public void pauseVerticalRotation() {
		verticalRotationPaused = true;
	}

	public void resumeVerticalRotation() {
		verticalRotationPaused = false;
	}

	public void pauseHorizontalRotation() {
		horizontalRotationPaused = true;
	}

	public void resumeHotizontalRotation() {
		horizontalRotationPaused = false;
	}

	public PuzzleController(Puzzle puzzle) {
		this.puzzle = puzzle;
		fromLocation = new Vector3();
		fromAxis = new Vector3();
		toLocation = new Vector3();
		toAxis = new Vector3();
		rotationAxis = new Vector3();

		previousPointer1 = new Vector2();
		previousPointer2 = new Vector2();

		hitCheckPaused = false;
		verticalRotationPaused = false;
		horizontalRotationPaused = false;

		preferences = Gdx.app.getPreferences(PuzzleCube.PREFERENCES_FILE);
		touchSensitivity = preferences.getFloat("touch",
				PuzzleCube.DEFAULT_TOUCH_SENSITIVITY);
	}

	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		if (puzzle != null) {
			if (pointer == 0) {
				float[] location = new float[3];
				float[] axis = new float[3];

				// Get the ray between the camera and the point touched
				Ray ray = puzzle.getCamera().getPickRay(x, y);

				/*
				 * Get the logical location of the cube pressed from the mesh.
				 * Keep in mind that this comes from the original mesh so any
				 * model modifications any part of the mesh will be ignored.
				 */
				rotateCamera = !puzzle.getMesh().getPickRayHit(ray, location,
						axis);

				if (!rotateCamera && !hitCheckPaused) {
					fromLocation.set(location);
					fromAxis.set(axis);

					// Start checking for cube rotations
					checkingCubeRotation = true;
				}

				previousPointer1.set(x, y);
			} else if (pointer == 1) {
				previousPointer2.set(x, y);
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean tap(float x, float y, int count, int button) {
		return false;
	}

	@Override
	public boolean longPress(float x, float y) {
		return false;
	}

	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		return false;
	}

	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		if (puzzle != null) {
			if (rotateCamera) {
				if (verticalRotationPaused) {
					deltaY = 0;
				}
				if (horizontalRotationPaused) {
					deltaX = 0;
				}

				// The touch started off from the puzzle so rotate the camera
				puzzle.getCamera().revolveCamera(
						-MathUtils.clamp(deltaX, -touchSensitivity,
								touchSensitivity),
						-MathUtils.clamp(deltaY, -touchSensitivity,
								touchSensitivity));

				// Update both pointers to prevent jumping with multi-touch
				previousPointer1.set(x, y);
				previousPointer2.set(x, y);
			} else if (checkingCubeRotation) {
				// The touch started on a cube so check for cube rotations
				checkCubeRotation(x, y);
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean zoom(float initialDistance, float distance) {
		return false;
	}

	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2,
			Vector2 pointer1, Vector2 pointer2) {

		if (puzzle != null && rotateCamera
				&& !(verticalRotationPaused && horizontalRotationPaused)) {
			puzzle.getCamera().rotateCamera(pointer1.x - pointer2.x,
					pointer1.y - pointer2.y,
					previousPointer1.x - previousPointer2.x,
					previousPointer1.y - previousPointer2.y);

			previousPointer1.set(pointer1);
			previousPointer2.set(pointer2);

			return true;
		}

		return false;
	}

	private void checkCubeRotation(float x, float y) {
		if (puzzle != null) {
			float[] location = new float[3];
			float[] axis = new float[3];

			// Get current cube being touched
			Ray ray = puzzle.getCamera().getPickRay(x, y);
			puzzle.getMesh().getPickRayHit(ray, location, axis);

			toLocation.set(location);
			toAxis.set(axis);
			rotationAxis.set(Vector3.Zero);

			/*
			 * Make sure that we are touching a cube and that it is the same or
			 * sharing the same side as the start cube
			 */
			if (!toLocation.idt(Vector3.Zero)
					&& fromLocation.dst(toLocation) <= 1) {
				if (toLocation.idt(fromLocation) && !toAxis.idt(fromAxis)) {
					/*
					 * Same cube so calculate rotation axis using the different
					 * faces of the cube
					 */
					rotationAxis.set(fromAxis).crs(toAxis);
				} else if (!toLocation.idt(fromLocation)
						&& toAxis.idt(fromAxis)) {
					/*
					 * Different cubes so calculate the rotation axis using the
					 * different locations and face of the starting cube
					 */
					rotationAxis.set(fromLocation).sub(toLocation)
							.crs(fromAxis);
				}

				if (!rotationAxis.isZero()) {
					// We have our rotation axis so stop checking for now
					checkingCubeRotation = false;

					// Get the layer and rotate it
					if (callback != null) {
						callback.layerRotation(
								rotationAxis,
								(rotationAxis.x != 0f) ? fromLocation.x
										: (rotationAxis.y != 0f) ? fromLocation.y
												: (rotationAxis.z != 0f) ? fromLocation.z
														: 0f);
					}
				}
			}
		}
	}
}
