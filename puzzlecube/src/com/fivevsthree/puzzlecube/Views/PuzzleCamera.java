package com.fivevsthree.puzzlecube.Views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Perspective camera that revolves around a puzzle
 * 
 * @author splude@fivevsthree.com
 * 
 */
public class PuzzleCamera implements Json.Serializable {

	private PerspectiveCamera camera;

	private float screenDensity;

	private Vector3 position, direction, up;

	private Quaternion q, q1, q2;

	/**
	 * An intersecting ray between the camera and the provided point
	 * 
	 * @param x
	 *            x coordinate of intersection point
	 * @param y
	 *            y coordinate of intersection point
	 * @return ray from camera
	 */
	public Ray getPickRay(float x, float y) {
		return camera.getPickRay(x, y);
	}

	/**
	 * Combined matrix for world view
	 * 
	 * @return combined projection and view matrix
	 */
	public Matrix4 getCombinedView() {
		return camera.combined;
	}

	/**
	 * Initialize the camera
	 */
	public PuzzleCamera() {
		// Set up our camera, position it, and point it at our target
		camera = new PerspectiveCamera(30f, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());
		positionCamera(0f, 0f, 20f);

		screenDensity = Gdx.graphics.getDensity();

		position = new Vector3();
		direction = new Vector3();
		up = new Vector3();

		q = new Quaternion();
		q1 = new Quaternion();
		q2 = new Quaternion();
	}

	/**
	 * Update the camera with any rotation changes
	 */
	public void update() {
		camera.update();
	}

	/**
	 * Position the camera and point it at the puzzle
	 * 
	 * @param x
	 *            x coordinate in world space
	 * @param y
	 *            y coordinate in world space
	 * @param z
	 *            z coordinate in world space
	 */
	public void positionCamera(float x, float y, float z) {
		camera.position.set(x, y, z);
		camera.direction.set(Vector3.Zero).sub(camera.position).nor();
	}

	/**
	 * Move the camera from the current position but do not point it at the
	 * puzzle
	 * 
	 * @param x
	 *            x coordinate in world space
	 * @param y
	 *            y coordinate in world space
	 * @param z
	 *            z coordinate in world space
	 */
	public void moveCamera(float x, float y, float z) {
		camera.translate(x, y, z);
	}

	public void rotateCamera(float deltaX, float deltaY, float previousDeltaX,
			float previousDeltaY) {
		// Determine our rotated up position
		q.set(camera.direction, (float) (Math.toDegrees(Math.atan2(
				previousDeltaY, previousDeltaX)) - Math.toDegrees(Math.atan2(
				deltaY, deltaX))));
		q.transform(camera.up.set(camera.up));
	}

	/**
	 * Revolve the camera around the puzzle using x and y axis. This is
	 * primarily used for 2D touch/mouse input on the screen
	 * 
	 * @param deltaX
	 *            amount to move around the x axis
	 * @param deltaY
	 *            amount to move around the y axis
	 */
	public void revolveCamera(float deltaX, float deltaY) {
		// Determine our rotated up position
		q1.set(camera.direction,
				(float) Math.toDegrees(Math.atan2(deltaY, deltaX)));

		// Our actual rotation
		q2.set(q1.transform(camera.up.cpy()),
				(float) (Math.sqrt(deltaX * deltaX + deltaY * deltaY) / screenDensity));

		// Move the camera to the new position
		q2.transform(camera.position.set(camera.position));

		// Point the camera back at our target
		q2.transform(camera.direction.set(camera.direction));

		// Update the cameras up position
		q2.transform(camera.up.set(camera.up));
	}

	public void rememberCurrentRotation() {
		position.set(camera.position);
		direction.set(camera.direction);
		up.set(camera.up);
	}

	public void revolveCamera(float x, float y, float z) {
		// Undo last rotation otherwise puzzle will spin way too fast
		camera.position.set(position);
		camera.direction.set(direction);
		camera.up.set(up);

		// Apply new rotation
		camera.rotateAround(Vector3.Zero, Vector3.X, x);
		camera.rotateAround(Vector3.Zero, Vector3.Y, y);
		camera.rotateAround(Vector3.Zero, Vector3.Z, z);
	}

	/**
	 * Write out the values needed to save the camera
	 */
	@Override
	public void write(Json json) {
		json.writeValue("Position", camera.position, Vector3.class);
		json.writeValue("Direction", camera.direction, Vector3.class);
		json.writeValue("Up", camera.up, Vector3.class);
	}

	/**
	 * Read in the values for a saved camera
	 */
	@Override
	public void read(Json json, JsonValue jsonData) {
		camera.position
				.set(json.readValue("Position", Vector3.class, jsonData));

		camera.direction.set(json.readValue("Direction", Vector3.class,
				jsonData));

		camera.up.set(json.readValue("Up", Vector3.class, jsonData));
	}

}
