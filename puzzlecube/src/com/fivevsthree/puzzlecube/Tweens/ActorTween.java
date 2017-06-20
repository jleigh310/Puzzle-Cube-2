package com.fivevsthree.puzzlecube.Tweens;

import aurelienribon.tweenengine.TweenAccessor;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class ActorTween implements TweenAccessor<Actor> {

	public static final int FADE = 1;
	public static final int POSITION = 2;
	public static final int ROTATE = 3;
	public static final int SCALE = 4;
	public static final int SCALE_XY = 5;
	public static final int COLOR = 6;

	@Override
	public int getValues(Actor target, int tweenType, float[] returnValues) {
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
			if (target.getClass().equals(Label.class)) {
				returnValues[0] = ((Label) target).getFontScaleX();
			} else {
				returnValues[0] = target.getScaleX();
			}
			return 1;

		case SCALE_XY:
			returnValues[0] = target.getScaleX();
			returnValues[1] = target.getScaleY();
			return 2;

		case COLOR:
			returnValues[0] = target.getColor().r;
			returnValues[1] = target.getColor().g;
			returnValues[2] = target.getColor().b;
			return 3;

		default:
			return 0;
		}
	}

	@Override
	public void setValues(Actor target, int tweenType, float[] newValues) {
		switch (tweenType) {
		case FADE:
			target.getColor().a = newValues[0];
			break;

		case POSITION:
			target.setPosition(newValues[0], newValues[1]);
			break;

		case ROTATE:
			target.setRotation(newValues[0]);
			break;

		case SCALE:
			if (target.getClass().equals(Label.class)) {
				((Label) target).setFontScale(newValues[0]);
			} else {
				target.setScale(newValues[0]);
			}
			break;

		case SCALE_XY:
			target.setScale(newValues[0], newValues[1]);
			break;

		case COLOR:
			target.setColor(newValues[0], newValues[1], newValues[2],
					target.getColor().a);
			break;
		}
	}
}
