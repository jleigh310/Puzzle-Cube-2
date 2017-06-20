package com.fivevsthree.puzzlecube.Tweens;

import com.fivevsthree.puzzlecube.Models.Layer;

import aurelienribon.tweenengine.TweenAccessor;

public class LayerTween implements TweenAccessor<Layer> {

	public static final int ROTATE = 1;
	public static final int EXPLODE = 2;

	@Override
	public int getValues(Layer target, int tweenType, float[] returnValues) {
		switch (tweenType) {
		case ROTATE:
			returnValues[0] = 0;
			return 1;

		case EXPLODE:
			returnValues[0] = 0;
			return 1;

		default:
			return 0;
		}
	}

	@Override
	public void setValues(Layer target, int tweenType, float[] newValues) {
		switch (tweenType) {
		case ROTATE:
			target.rotate(newValues[0]);
			break;

		case EXPLODE:
			target.explode(newValues[0]);
			break;
		}
	}

}
