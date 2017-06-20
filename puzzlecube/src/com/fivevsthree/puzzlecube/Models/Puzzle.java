package com.fivevsthree.puzzlecube.Models;

import java.util.ArrayList;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquation;
import aurelienribon.tweenengine.TweenEquations;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.fivevsthree.puzzlecube.PuzzleCube;
import com.fivevsthree.puzzlecube.Callbacks.PuzzleCallback;
import com.fivevsthree.puzzlecube.Tweens.LayerTween;
import com.fivevsthree.puzzlecube.Tweens.PuzzleTween;
import com.fivevsthree.puzzlecube.Views.PuzzleCamera;

public class Puzzle implements Json.Serializable {

	private Cube cubes[];
	private TweenManager tweenManager;
	private PuzzleCallback callback;

	private PuzzleCamera camera;
	private PuzzleMesh mesh;

	private Preferences preferences;

	private Timer timer;
	private long secondsElapsed, moveCounter;

	private boolean isLayerRotating;

	private float[] colors;

	private ArrayList<Layer> rotations;

	private Quaternion cubeRotation;

	private Vector3 forwardVector, upVector, rightVector;

	public static enum RotationAnimation {
		Bounce, Standard, NoAnimation
	};

	public static enum SolvedAnimation {
		NoAnimation, Explode
	};

	private RotationAnimation rotationAnimation;

	public Color color;

	private int[] centers = { 10, 15, 12, 13, 4, 21 };

	private int[][] faces = {
			// Bottom
			{ 0, 1, 2, 9, 11, 17, 18, 19 },

			// Top
			{ 6, 7, 8, 14, 16, 23, 24, 25 },

			// Back
			{ 0, 3, 6, 9, 14, 17, 20, 23 },

			// Front
			{ 2, 5, 8, 11, 16, 19, 22, 25 },

			// Left
			{ 0, 1, 2, 3, 5, 6, 7, 8 },

			// Right
			{ 17, 18, 19, 20, 22, 23, 24, 25 } };

	/**
	 * All of our logical cubes
	 * 
	 * @return array of cubes
	 */
	public Cube[] getCubes() {
		return cubes;
	}

	public long getSeconds() {
		return secondsElapsed;
	}

	public void setSeconds(long seconds) {
		this.secondsElapsed = seconds;
	}

	public long getMoves() {
		return moveCounter;
	}

	public RotationAnimation getRotationAnimation() {
		return rotationAnimation;
	}

	public PuzzleCamera getCamera() {
		return camera;
	}

	public PuzzleMesh getMesh() {
		return mesh;
	}

	public void setCallback(PuzzleCallback callback) {
		this.callback = callback;
	}

	public boolean isRotationsEmpty() {
		return rotations.isEmpty();
	}

	public Puzzle() {
		this(false);
	}

	/**
	 * Create a puzzle with new cubes
	 * 
	 * @param game
	 *            instance of PuzzleCube game
	 */
	public Puzzle(boolean useDefaults) {
		preferences = Gdx.app.getPreferences(PuzzleCube.PREFERENCES_FILE);

		rotationAnimation = RotationAnimation.values()[(useDefaults) ? 0
				: preferences.getInteger("rotation", 0)];

		colors = new float[18];
		for (int i = 0; i < 6; i++) {
			colors[i * 3] = (useDefaults) ? PuzzleCube.DEFAULT_COLOR[i].r
					: preferences.getFloat(String.format("color-%d-r", i),
							PuzzleCube.DEFAULT_COLOR[i].r);
			colors[i * 3 + 1] = (useDefaults) ? PuzzleCube.DEFAULT_COLOR[i].g
					: preferences.getFloat(String.format("color-%d-g", i),
							PuzzleCube.DEFAULT_COLOR[i].g);
			colors[i * 3 + 2] = (useDefaults) ? PuzzleCube.DEFAULT_COLOR[i].b
					: preferences.getFloat(String.format("color-%d-b", i),
							PuzzleCube.DEFAULT_COLOR[i].b);
		}

		mesh = new PuzzleMesh(colors, 1f);

		// Create the logical cubes
		createCubes();

		rotations = new ArrayList<Layer>();

		cubeRotation = new Quaternion();
		forwardVector = new Vector3();
		upVector = new Vector3();
		rightVector = new Vector3();

		color = new Color(1f, 1f, 1f, 1f);

		camera = new PuzzleCamera();

		tweenManager = new TweenManager();

		resetTimer();

		// We do not want to start the timer when the puzzle is created
		timer.stop();

		isLayerRotating = false;

		secondsElapsed = PuzzleCube.PUZZLE_STARTING_SECONDS;
		moveCounter = 0;
	}

	/**
	 * Start or resume puzzle timer
	 */
	public void startTimer() {
		timer.start();
	}

	/**
	 * Stop puzzle timer (does not reset)
	 */
	public void stopTimer() {
		timer.stop();
	}

	public void resetTimer() {
		// Count up every second and notify the callback if there is one
		if (timer == null) {
			timer = new Timer();
		}

		timer.clear();
		timer.scheduleTask(new Task() {
			@Override
			public void run() {
				secondsElapsed++;
				if (callback != null) {
					callback.timerChanged(secondsElapsed);
				}
			}
		}, 1f, 1f);
	}

	/**
	 * Create an array of cubes
	 */
	private void createCubes() {
		// y = up and down
		// x = right and left
		// z = closer and further
		cubes = new Cube[26];

		// x = -1 : left, x = 0 : center, x = 1 : right
		for (int i = 0, x = -1; x < 2; x++) {
			// y = -1 : bottom, y = 0 : center, y = 1 : top
			for (int y = -1; y < 2; y++) {
				// z = -1 : back, z = 0 center, z = 1 : front
				for (int z = -1; z < 2; z++) {
					// 0, 0, 0 is center cube which is not created
					if (x == 0 && y == 0 && z == 0) {
						continue;
					}

					cubes[i] = new Cube(i, new Vector3(x, y, z));

					// Move on to the next cube
					i++;
				}
			}
		}
	}

	/**
	 * Find a layer of cubes along a specific axis
	 * 
	 * @param axis
	 *            the axis of the layer
	 * @param depth
	 *            the depth the layer is along the axis
	 * @return layer of cubes
	 */
	private Layer getLayer(Vector3 axis, float depth) {
		Layer layer = new Layer(axis);

		for (Cube cube : cubes) {
			// Compare our layer to the coordinate of the cube along our axis
			if (depth == ((axis.x != 0f) ? cube.location.x
					: (axis.y != 0f) ? cube.location.y
							: (axis.z != 0f) ? cube.location.z : 0)) {
				// We found a cube along our axis so add it to our layer
				layer.addCube(cube);
			}
		}

		return layer;
	}

	/**
	 * Animate the rotation of a layer
	 * 
	 * @param axis
	 *            axis the layer will rotate around
	 * @param depth
	 *            depth of the layer in relation to the center of the puzzle
	 * @param duration
	 *            how long the animation should last
	 */
	public void rotateLayerAnimation(Vector3 axis, float depth, float duration,
			boolean allowUndo) {
		// We don't want to rotate in the middle of another rotation
		if (!isLayerRotating) {
			// Get the layer to rotate
			Layer layer = getLayer(axis, depth);

			// We want the locations after rotation
			layer.updateLocations();

			if (allowUndo) {
				try {
					rotations.add(layer);
				} catch (Exception e) {
					rotations.clear();
				}
			}

			moveCounter++;
			if (callback != null) {
				callback.moveCounterChanged(moveCounter);
			}

			if (rotationAnimation != RotationAnimation.NoAnimation) {
				TweenCallback animationDone = new TweenCallback() {
					@Override
					public void onEvent(int type, BaseTween<?> source) {
						isLayerRotating = false;
						if (callback != null) {
							callback.rotationComplete(isSolved());
						}
					}
				};

				TweenEquation equation = (rotationAnimation == RotationAnimation.Bounce) ? TweenEquations.easeOutBack
						: TweenEquations.easeInOutQuad;

				// Start the rotation animation
				Tween.to(layer, LayerTween.ROTATE, duration).target(90f)
						.ease(equation).setCallback(animationDone)
						.setCallbackTriggers(TweenCallback.COMPLETE)
						.start(tweenManager);

				if (callback != null) {
					callback.rotationStarted();
				}

				isLayerRotating = true;

			} else {
				if (callback != null) {
					callback.rotationStarted();
				}
				layer.rotate(90f);
				if (callback != null) {
					callback.rotationComplete(isSolved());
				}
			}
		}
	}

	/**
	 * Rotate a layer 90 degrees
	 * 
	 * @param axis
	 *            axis the layer will rotate around
	 * @param depth
	 *            depth of the layer in relation to the center of the puzzle
	 */
	public void rotateLayer(Vector3 axis, float depth) {
		Layer layer = getLayer(axis, depth);
		layer.updateLocations();
		layer.rotate(90f);
	}

	public void undoRotation(float duration) {
		if (!rotations.isEmpty() && !isLayerRotating) {
			Layer layer = rotations.remove(rotations.size() - 1);
			layer.reverse();
			layer.updateLocations();

			if (rotationAnimation != RotationAnimation.NoAnimation) {
				TweenCallback animationDone = new TweenCallback() {
					@Override
					public void onEvent(int type, BaseTween<?> source) {
						isLayerRotating = false;
						if (callback != null) {
							callback.rotationComplete(isSolved());
						}
					}
				};

				TweenEquation equation = (rotationAnimation == RotationAnimation.Bounce) ? TweenEquations.easeOutBack
						: TweenEquations.easeInOutQuad;

				// Start the rotation animation
				Tween.to(layer, LayerTween.ROTATE, duration).target(90f)
						.ease(equation).setCallback(animationDone)
						.setCallbackTriggers(TweenCallback.COMPLETE)
						.start(tweenManager);

				if (callback != null) {
					callback.rotationStarted();
				}

				isLayerRotating = true;

			} else {
				if (callback != null) {
					callback.rotationStarted();
				}
				layer.rotate(90f);
				if (callback != null) {
					callback.rotationComplete(isSolved());
				}
			}
		}
	}

	/**
	 * Randomly scrambles the puzzle
	 * 
	 * @param rotations
	 *            number of times to rotate random layers
	 */
	public void scramble(int rotations) {
		int lastAxis = 0;
		for (int i = 0; i < rotations; i++) {
			// Randomly pick the X, Y, or Z axis
			int randomAxis = MathUtils.random(1, 3);

			// Make sure we don't rotate the same axis twice in a row
			while (randomAxis == lastAxis) {
				randomAxis = MathUtils.random(1, 3);
			}

			lastAxis = randomAxis;

			// Randomly pick whether to rotate forward or backward
			int randomDirection = (MathUtils.random(1, 2) == 1) ? -1 : 1;

			// Randomly pick a depth along the axis
			int randomDepth = MathUtils.random(-1, 1);

			Vector3 axis = new Vector3(
					(randomAxis == 1) ? randomDirection : 0f,
					(randomAxis == 2) ? randomDirection : 0f,
					(randomAxis == 3) ? randomDirection : 0f);

			// Rotate the layer
			rotateLayer(axis, randomDepth);
		}
	}

	/**
	 * Check if the puzzle is solved.
	 * 
	 * @return true if the puzzle is solved
	 */
	public boolean isSolved() {
		// Loop through all 6 faces
		for (int i = 0; i < 6; i++) {
			// Get rotation of first cube on the current face
			cubeRotation.setFromMatrix(cubes[faces[i][0]].rotation);

			// Get the rotation vectors of the first cube
			forwardVector = getForwardVector(cubeRotation);
			upVector = getUpVector(cubeRotation);
			rightVector = getRightVector(cubeRotation);

			// Loop through all corner and edge cubes on current face
			for (int j = 0; j < 8; j++) {
				// Make sure correct cubes are surrounding the center cube
				if (cubes[centers[i]].location.dst(cubes[faces[i][j]].location) > 2f) {
					return false;
				}

				// Get rotation of the current cube
				cubeRotation.setFromMatrix(cubes[faces[i][j]].rotation);

				// Make sure cubes are all oriented the same way
				if (!forwardVector.idt(getForwardVector(cubeRotation))
						|| !upVector.idt(getUpVector(cubeRotation))
						|| !rightVector.idt(getRightVector(cubeRotation))) {
					return false;
				}
			}
		}

		return true;
	}

	private Vector3 getForwardVector(Quaternion q) {
		Vector3 v = new Vector3(2 * (q.x * q.z + q.w * q.y),
				2 * (q.y * q.x - q.w * q.x), 1 - 2 * (q.x * q.x + q.y * q.y));
		v.nor();
		v.x = (Math.abs(v.x) != 1) ? 0 : v.x;
		v.y = (Math.abs(v.y) != 1) ? 0 : v.y;
		v.z = (Math.abs(v.z) != 1) ? 0 : v.z;

		return v;
	}

	private Vector3 getUpVector(Quaternion q) {
		Vector3 v = new Vector3(2 * (q.x * q.y - q.w * q.z),
				1 - 2 * (q.x * q.x + q.z * q.z), 2 * (q.y * q.z + q.w * q.x));
		v.nor();
		v.x = (Math.abs(v.x) != 1) ? 0 : v.x;
		v.y = (Math.abs(v.y) != 1) ? 0 : v.y;
		v.z = (Math.abs(v.z) != 1) ? 0 : v.z;

		return v;
	}

	private Vector3 getRightVector(Quaternion q) {
		Vector3 v = new Vector3(1 - 2 * (q.y * q.y + q.z * q.z),
				2 * (q.x * q.y + q.w * q.z), 2 * (q.x * q.z - q.w * q.y));
		v.nor();
		v.x = (Math.abs(v.x) != 1) ? 0 : v.x;
		v.y = (Math.abs(v.y) != 1) ? 0 : v.y;
		v.z = (Math.abs(v.z) != 1) ? 0 : v.z;

		return v;
	}

	public void explode() {
		Timeline timeline = Timeline.createParallel();

		Layer[] layers = new Layer[3];
		layers[0] = getLayer(new Vector3(1, 0, 0), -1);
		layers[1] = getLayer(new Vector3(1, 0, 0), 0);
		layers[2] = getLayer(new Vector3(1, 0, 0), 1);

		for (Layer layer : layers) {
			timeline.push(Tween.to(layer, LayerTween.EXPLODE, 3f).target(40f)
					.ease(TweenEquations.easeNone));
		}

		timeline.push(Tween.to(this, PuzzleTween.FADE, 1f).target(0f, 0f, 0f)
				.delay(0.25f).ease(TweenEquations.easeInOutQuad));

		timeline.start(tweenManager);
	}

	/**
	 * Update rotation animation and bind the texture
	 * 
	 * @param delta
	 *            time in seconds since last update
	 */
	public void update(float delta) {
		camera.update();
		tweenManager.update(delta);
	}

	/**
	 * Serialize the values that we want to save
	 */
	@Override
	public void write(Json json) {
		json.writeValue("Seconds", secondsElapsed);
		json.writeValue("Moves", moveCounter);
		json.writeValue("Cubes", cubes, Cube[].class);
		json.writeValue("Camera", camera, PuzzleCamera.class);
		json.writeValue("Colors", colors, float[].class);
		json.writeValue("Rotation", rotationAnimation.ordinal(), int.class);
	}

	/**
	 * Deserialize the values that we want to load
	 */
	@Override
	public void read(Json json, JsonValue jsonData) {
		secondsElapsed = json.readValue("Seconds", long.class, jsonData);

		moveCounter = json.readValue("Moves", long.class, jsonData);

		Cube[] cubes = json.readValue("Cubes", Cube[].class, jsonData);
		if (cubes != null) {
			this.cubes = cubes;
		}

		PuzzleCamera camera = json.readValue("Camera", PuzzleCamera.class,
				jsonData);
		if (camera != null) {
			this.camera = camera;
		}

		colors = json.readValue("Colors", float[].class, jsonData);
		if (mesh != null) {
			mesh.dispose();
		}
		mesh = new PuzzleMesh(colors, 1f);

		rotationAnimation = RotationAnimation.values()[json.readValue(
				"Rotation", int.class, jsonData)];
	}

	public void dispose() {
		if (mesh != null) {
			mesh.dispose();
			mesh = null;
		}
	}

}
