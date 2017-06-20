package com.fivevsthree.puzzlecube.Tweens;

import aurelienribon.tweenengine.TweenAccessor;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class SpriteTween implements TweenAccessor<Sprite> {

	public static final int FADE = 1;
	public static final int POSITION = 2;
	public static final int ROTATE = 3;
	public static final int SCALE = 4;
	public static final int SCALE_XY = 5;

	@Override
	public int getValues(Sprite target, int tweenType, float[] returnValues) {
		switch (tweenType) {
		case FADE:
			returnValues[0] = target.getColor().a;
			return 1;

		case POSITION:
			returnValues[0] = target.getX();
			returnValues[1] = target.getY();
			return 2;

		case ROTATE:
			returnValues[0] = target.getRotation();
			return 1;

		case SCALE:
			returnValues[0] = target.getScaleX();
			return 1;

		case SCALE_XY:
			returnValues[0] = target.getScaleX();
			returnValues[1] = target.getScaleY();
			return 2;

		default:
			return 0;
		}
	}

	@Override
	public void setValues(Sprite target, int tweenType, float[] newValues) {
		switch (tweenType) {
		case FADE:
			target.setColor(target.getColor().r, target.getColor().g,
					target.getColor().b, newValues[0]);
			break;

		case POSITION:
			target.setPosition(newValues[0], newValues[1]);
			break;

		case ROTATE:
			target.setRotation(newValues[0]);
			break;

		case SCALE:
			target.setScale(newValues[0]);
			break;

		case SCALE_XY:
			target.setScale(newValues[0], newValues[1]);
			break;
		}
	}

}
