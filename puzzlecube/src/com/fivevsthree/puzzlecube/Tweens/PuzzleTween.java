package com.fivevsthree.puzzlecube.Tweens;

import aurelienribon.tweenengine.TweenAccessor;

import com.fivevsthree.puzzlecube.Models.Puzzle;

public class PuzzleTween implements TweenAccessor<Puzzle> {

	public static final int FADE = 1;
	public static final int ROTATE = 2;

	@Override
	public int getValues(Puzzle target, int tweenType, float[] returnValues) {
		switch (tweenType) {
		case FADE:
			returnValues[0] = target.color.r;
			returnValues[1] = target.color.g;
			returnValues[2] = target.color.b;
			return 3;

		case ROTATE:
			returnValues[0] = 0;
			returnValues[1] = 0;
			returnValues[2] = 0;
			return 3;

		default:
			return 0;
		}
	}

	@Override
	public void setValues(Puzzle target, int tweenType, float[] newValues) {
		switch (tweenType) {
		case FADE:
			target.color.set(newValues[0], newValues[1], newValues[2], 1f);
			break;

		case ROTATE:
			target.getCamera().revolveCamera(newValues[0], newValues[1],
					newValues[2]);
			break;
		}
	}

}
