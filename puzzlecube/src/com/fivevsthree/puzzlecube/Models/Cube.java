package com.fivevsthree.puzzlecube.Models;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

/**
 * Logical cube that can be modified so the puzzle mesh can remain static
 * 
 * @author splude@fivevsthree.com
 * 
 */
public class Cube {

	/**
	 * Offset of the cube in the puzzle mesh
	 */
	public int index;

	/**
	 * Location of the cube relative to the center of the puzzle
	 * 
	 */
	public Vector3 location;

	/**
	 * The current rotation of the cube
	 * 
	 */
	public Matrix4 rotation;

	public Matrix4 previousRotation;

	public Cube() {
		index = 0;
		location = new Vector3();
		rotation = new Matrix4();
		previousRotation = new Matrix4();
	}

	/**
	 * Create our logical cube
	 * 
	 * @param index
	 *            offset of the cube in mesh
	 * @param location
	 *            location of the cube in puzzle
	 */
	public Cube(int index, Vector3 location) {
		this.index = index;
		this.location = location.cpy();
		rotation = new Matrix4();
		previousRotation = new Matrix4();
	}

}
