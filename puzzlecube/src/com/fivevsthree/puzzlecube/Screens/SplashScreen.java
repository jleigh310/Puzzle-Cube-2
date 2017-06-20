package com.fivevsthree.puzzlecube.Screens;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.fivevsthree.puzzlecube.PuzzleCube;
import com.fivevsthree.puzzlecube.Tweens.StageTween;

public class SplashScreen implements Screen {

	private PuzzleCube game;

	private Pixmap pixmap;
	private Texture backgroundTexture, progressBackTexture, progressTexture;
	private Stage stage;
	private Slider progressBar;

	private TweenManager tweenManager;
	private Tween outTween;

	public SplashScreen(PuzzleCube game) {
		this.game = game;

		// Get screen dimensions
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();

		// Load the splash image
		backgroundTexture = new Texture("data/textures/splash.png");
		backgroundTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		Image backgroundImage = new Image(backgroundTexture);
		backgroundImage.setFillParent(true);
		backgroundImage.setScaling(Scaling.fillY);

		createProgressBar();

		// Add splash image and progress bar to the screen
		stage = new Stage(width, height, true);
		stage.addActor(backgroundImage);
		// stage.addActor(progressBar);

		Tween.registerAccessor(Stage.class, new StageTween());
		tweenManager = new TweenManager();
	}

	private void createProgressBar() {
		float width = Gdx.graphics.getWidth();

		// Generate a colored texture for progress bar
		pixmap = new Pixmap(1, 4, Format.RGBA8888);
		pixmap.setColor(1f, 1f, 1f, 1f);
		pixmap.fill();

		// Use a white texture for the progress background
		progressBackTexture = new Texture(pixmap);
		TextureRegionDrawable progressBackDrawable = new TextureRegionDrawable(
				new TextureRegion(progressBackTexture));

		// Change the texture color
		pixmap.setColor(0f, 0.35f, 0.75f, 1f);
		pixmap.fill();

		// Use a blue texture for the progress foreground
		progressTexture = new Texture(pixmap);
		TextureRegionDrawable progressDrawable = new TextureRegionDrawable(
				new TextureRegion(progressTexture));

		// Style the slider
		SliderStyle style = new SliderStyle();
		style.background = progressBackDrawable;
		style.knobBefore = progressDrawable;

		// Use a slider for the progress bar
		progressBar = new Slider(0f, 1f, 0.01f, false, style);
		progressBar.setAnimateDuration(0.5f);
		progressBar.setPosition(0, 0);
		progressBar.setWidth(width);
		progressBar.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Slider slider = (Slider) actor;
				if (slider.getValue() == 1f) {
					game.createScreens();
					outTween.start(tweenManager);
				}
			}
		});
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		// Update progress
		if (game.getAssets().update() && game.getAssets().getProgress() != 1f) {
			progressBar.setValue(1f);
		}

		if (progressBar.getValue() != 1f) {
			progressBar.setValue(game.getAssets().getProgress());
		}

		// Update screen transition
		tweenManager.update(delta);

		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void show() {
		// Let us know when the transition out is complete
		TweenCallback outCallback = new TweenCallback() {
			@Override
			public void onEvent(int type, BaseTween<?> source) {
				game.showMainMenu();
			}
		};

		// Set up our outbound screen transition
		outTween = Tween.to(stage, StageTween.FADE, 2).target(0).delay(1)
				.ease(TweenEquations.easeInOutQuad).setCallback(outCallback)
				.setCallbackTriggers(TweenCallback.COMPLETE);
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		if (backgroundTexture != null) {
			backgroundTexture.dispose();
			backgroundTexture = null;
		}
		if (progressBackTexture != null) {
			progressBackTexture.dispose();
			progressBackTexture = null;
		}
		if (progressTexture != null) {
			progressTexture.dispose();
			progressTexture = null;
		}
		if (pixmap != null) {
			pixmap.dispose();
			pixmap = null;
		}
		if (stage != null) {
			stage.dispose();
			stage = null;
		}
	}

}
