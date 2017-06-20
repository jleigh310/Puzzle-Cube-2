package com.fivevsthree.puzzlecube.Tweens;

import com.badlogic.gdx.scenes.scene2d.Stage;

import aurelienribon.tweenengine.TweenAccessor;

public class StageTween implements TweenAccessor<Stage> {

	public static final int FADE = 1;

	@Override
	public int getValues(Stage target, int tweenType, float[] returnValues) {
		switch (tweenType) {
		case FADE:
			returnValues[0] = target.getRoot().getColor().a;
			return 1;

		default:
			return 0;
		}
	}

	@Override
	public void setValues(Stage target, int tweenType, float[] newValues) {
		switch (tweenType) {
		case FADE:
			target.getRoot().setColor(0f, 0f, 0f, newValues[0]);
			break;
		}
	}

}
