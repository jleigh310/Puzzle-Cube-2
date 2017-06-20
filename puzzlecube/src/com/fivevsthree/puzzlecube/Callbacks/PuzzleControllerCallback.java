package com.fivevsthree.puzzlecube.Callbacks;

import com.badlogic.gdx.math.Vector3;

public interface PuzzleControllerCallback {
	public void layerRotation(Vector3 axis, float depth);
}
