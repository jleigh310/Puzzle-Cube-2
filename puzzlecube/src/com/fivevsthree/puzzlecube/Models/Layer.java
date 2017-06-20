package com.fivevsthree.puzzlecube.Models;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector3;

/**
 * Encapsulates a layer of cubes for rotation
 * 
 * @author splude@fivevsthree.com
 * 
 */
public class Layer {

	private List<Cube> cubes;
	private Vector3 axis;

	/**
	 * Create a layer out of cubes
	 * 
	 * @param cubes
	 *            cubes in the layer
	 * @param axis
	 *            axis the layer will rotate around
	 */
	public Layer(Cube[] cubes, Vector3 axis) {
		this.cubes = new ArrayList<Cube>();
		this.axis = new Vector3(axis);

		for (Cube cube : cubes) {
			cube.previousRotation.set(cube.rotation);
			this.cubes.add(cube);
		}
	}

	/**
	 * Create a layer to add cubes to
	 * 
	 * @param axis
	 *            axis the layer will rotate around
	 */
	public Layer(Vector3 axis) {
		this.cubes = new ArrayList<Cube>();
		this.axis = new Vector3(axis);
	}

	public void addCube(Cube cube) {
		cube.previousRotation.set(cube.rotation);
		cubes.add(cube);
	}

	/**
	 * Rotate the layer around an axis a specified number of degrees
	 * 
	 * @param degrees
	 *            amount to rotate
	 */
	public void rotate(float degrees) {
		for (Cube cube : cubes) {
			/*
			 * We need to "undo" our previous rotations before we apply our new
			 * rotation. This way we will rotate around the world axis instead
			 * of the local axis of the object. The first rotation applied to an
			 * object uses the world axis because the local axis of the object
			 * is aligned with the world axis. Once rotated, the local axis will
			 * be changed and any further rotations will use the changed local
			 * axis. Once we apply the new rotation, we "redo" our previous
			 * rotations.
			 */
			cube.rotation.idt().rotate(axis, degrees)
					.mul(cube.previousRotation);
		}
	}

	public void explode(float amount) {
		Vector3 translation = new Vector3();

		for (Cube cube : cubes) {
			translation.set(cube.location).scl(amount);

			cube.rotation.idt().rotate(cube.location, amount * 20f)
					.translate(translation).mul(cube.previousRotation);
		}
	}

	public void reverse() {
		for (Cube cube : cubes) {
			cube.previousRotation.set(cube.rotation);
		}
		axis.scl(-1);
	}

	/**
	 * Update the location in the logical cubes after rotation
	 */
	public void updateLocations() {
		Vector3 face = new Vector3();

		// Iterate through the layer and rotate each cube
		for (Cube cube : cubes) {
			/*
			 * We need to keep the location coordinate for the axis we are
			 * rotating around because the cross multiply will only provide the
			 * other two
			 */
			face.set((axis.x != 0f) ? cube.location.x : 0f,
					(axis.y != 0f) ? cube.location.y : 0f,
					(axis.z != 0f) ? cube.location.z : 0f);

			/*
			 * Using the axis, in the opposite direction, we can cross multiply
			 * with the current location to give us the new location after
			 * rotation
			 */
			cube.location.crs(-axis.x, -axis.y, -axis.z).add(face);
		}
	}

}
